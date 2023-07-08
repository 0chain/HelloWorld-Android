package org.zus.helloworld.ui.vult

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.barteksc.pdfviewer.PDFView
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import org.zus.helloworld.R
import org.zus.helloworld.data.Files
import org.zus.helloworld.databinding.VultFragmentBinding
import org.zus.helloworld.ui.mainactivity.MainViewModel
import org.zus.helloworld.utils.*
import org.zus.helloworld.utils.Utils.Companion.getConvertedDateTime
import org.zus.helloworld.utils.Utils.Companion.getConvertedSize
import org.zus.helloworld.utils.Utils.Companion.getSizeInB
import org.zus.helloworld.utils.Utils.Companion.getTimeInNanoSeconds
import zbox.StatusCallbackMocked
import zcncore.Zcncore
import java.io.File
import java.io.FileOutputStream
import java.util.*


const val TAG_VULT = "VultFragment"

const val ALLOCATION_NAME = "test allocation"
const val DATA_SHARDS = 2L
const val PARITY_SHARDS = 2L
val ALLOCATION_SIZE = 2L.getSizeInB(StorageSizes.GB)
val EXPIRATION_SECONDS =
    (Date().time / 1000) + 721L.getTimeInNanoSeconds(ExpirationTime.Hour) / (1000000000)
val LOCK_TOKENS: String = Zcncore.convertToValue(1.0)
private const val REQUEST_FILES = 1
private const val RESULT_ERROR = 64

class VultFragment : Fragment(), FileClickListener, ThumbnailDownloadCallback {
    private lateinit var binding: VultFragmentBinding
    private lateinit var vultViewModel: VultViewModel
    private lateinit var mainViewModel: MainViewModel
    var downloadPath = ""
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var previewDialog: Dialog? = null
    private var previewLayout: View? = null
    private var currentFilePosition = -1
    private var filesAdapter: FilesAdapter? = null
    private var currentSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        binding = VultFragmentBinding.inflate(inflater, container, false)
        vultViewModel = ViewModelProvider(requireActivity())[VultViewModel::class.java]
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]

        downloadPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.swipeRefreshLayout.isRefreshing) {
                    findNavController().popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        CoroutineScope(Dispatchers.Main).launch {
            isRefresh(true)

            // Storage SDK initialization and wallet initialization.
            vultViewModel.storageSDK =
                VultViewModel.initZboxStorageSDK(
                    Utils(requireContext()).config,
                    Utils(requireContext()).readWalletFromFileJSON()
                )

            if (vultViewModel.getAllocation() == null) {
                vultViewModel.createAllocation(
                    allocationName = ALLOCATION_NAME,
                    dataShards = DATA_SHARDS,
                    parityShards = PARITY_SHARDS,
                    allocationSize = ALLOCATION_SIZE,
                    expirationSeconds = EXPIRATION_SECONDS,
                    lockTokens = LOCK_TOKENS,
                )
                updateTotalSizeProgress()

            } else {
                vultViewModel.getAllocation().let { allocation ->
                    if (allocation?.id == null) {
                        throw Exception("Allocation id is null")
                    } else {
                        val statsModel = vultViewModel.getStats(allocation.stats)
                        binding.allocationProgressView.progress =
                            (100 * statsModel.used_size / allocation.size).toInt()
                        binding.tvAllocationDate.text =
                            allocation.expiration.getConvertedDateTime()
                        binding.tvStorageUsed.text = getString(
                            R.string.storage_used,
                            statsModel.used_size.getConvertedSize(),
                            allocation.size.getConvertedSize()
                        )
                        vultViewModel.totalStorageUsed.observe(viewLifecycleOwner) { totalStorageUsed ->
                            if (totalStorageUsed != null) {
                                binding.tvStorageUsed.text =
                                    getString(
                                        R.string.storage_used,
                                        totalStorageUsed.getConvertedSize(),
                                        allocation.size.getConvertedSize()
                                    )
                                binding.allocationProgressView.progress =
                                    (100 * totalStorageUsed / allocation.size).toInt()
                            }
                        }
                    }
                }
            }

            vultViewModel.getAllocation()
            vultViewModel.listFiles("/")
            isRefresh(false)
        }
        previewDialog =
            Dialog(requireActivity(), android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
        previewLayout = layoutInflater.inflate(R.layout.dialog_file_preview, null)
        previewDialog!!.setContentView(previewLayout!!)
        previewDialog!!.setCancelable(true)

        val documentPicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data?.clipData != null) {
                        val count: Int = data.clipData!!.itemCount
                        val jsonArray = JsonArray()
                        val gson = Gson()
                        for (i in 0 until count) {
                            val fileUri: Uri = data.clipData!!.getItemAt(i).uri
                            val newFilesList = vultViewModel.filesList.value ?: mutableListOf()
                            var fileName = Utils(requireContext()).getFileName(fileUri)
                            val extensionIndex = fileName.lastIndexOf(".")
                            val fileNameWithoutExtension = fileName.substring(0, extensionIndex)
                            val fileExtension = fileName.substring(extensionIndex)
                            var newFileName = fileName
                            var counter = 1
                            while (newFilesList.any { it.name == newFileName }) {
                                newFileName = "$fileNameWithoutExtension($counter)$fileExtension"
                                counter++
                            }
                            fileName = newFileName
                            context?.let {
                                val file = from(it, fileUri, fileName)
                                val uploadingFile: Files = Files.newUploadingFile(
                                    fileName,
                                    "/$fileName",
                                    file.absolutePath,
                                )
                                uploadingFile.mimeType = Utils(requireContext()).getMimeType(file)
                                val androidPath = context?.filesDir?.absolutePath + "/" + fileName
                                uploadingFile.thumbnailPath =
                                    getThumbnail(
                                        it, file,
                                        "/thumbnail/", fileName
                                    )
                                uploadingFile.setAndroidPath(androidPath)
                                vultViewModel.addFileToMutableList(uploadingFile)
                                val jsonObject = JsonObject()
                                jsonObject.addProperty("remotePath", "/")
                                jsonObject.addProperty("fileName", fileName)
                                jsonObject.addProperty("encrypt", false)
                                jsonObject.addProperty("filePath", androidPath)
                                jsonObject.addProperty("thumbnailPath", uploadingFile.thumbnailPath)
                                val size = File(androidPath).length()
                                jsonObject.addProperty(
                                    "chunkNumber",
                                    if (size > 983040) (size / 983040).toInt() else 1
                                )
                                jsonArray.add(jsonObject)
                            }
                            Log.i(TAG_VULT, "Uri: $fileUri")
                            Log.i(TAG_VULT, "Uri path: ${fileUri.path}")
                            Log.i(TAG_VULT, "File name: $fileName")
                        }
                        val jsonString = gson.toJson(jsonArray)
                        isRefresh(true)
                        GlobalScope.launch(Dispatchers.IO) {
                            vultViewModel.uploadMultipleFilesWithCallback(
                                workDir = requireContext().filesDir.absolutePath,
                                multiUploadRequestBody = jsonString,
                                callback = statusCallbackForMultiUpload
                            )
                        }
                        isRefresh(false)
                    } else if (data != null && data.data != null) {
                        val fileUri = data.data
                        val jsonArray = JsonArray()
                        val gson = Gson()
                        val newFilesList = vultViewModel.filesList.value ?: mutableListOf()
                        var fileName = fileUri?.let { Utils(requireContext()).getFileName(it) }
                        val extensionIndex = fileName!!.lastIndexOf(".")
                        val fileNameWithoutExtension = fileName.substring(0, extensionIndex)
                        val fileExtension = fileName.substring(extensionIndex)
                        var newFileName = fileName
                        var counter = 1
                        while (newFilesList.any { it.name == newFileName }) {
                            newFileName = "$fileNameWithoutExtension($counter)$fileExtension"
                            counter++
                        }
                        fileName = newFileName
                        context?.let {
                            val file = from(it, fileUri, fileName)
                            val uploadingFile: Files = Files.newUploadingFile(
                                fileName,
                                "/$fileName",
                                file.absolutePath,
                            )
                            uploadingFile.mimeType = Utils(requireContext()).getMimeType(file)
                            val androidPath = context?.filesDir?.absolutePath + "/" + fileName
                            uploadingFile.thumbnailPath =
                                fileName?.let { it1 ->
                                    getThumbnail(
                                        it, file,
                                        "/thumbnail/", it1
                                    )
                                }
                            uploadingFile.setAndroidPath(androidPath)
                            vultViewModel.addFileToMutableList(uploadingFile)
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("remotePath", "/")
                            jsonObject.addProperty("fileName", fileName)
                            jsonObject.addProperty("encrypt", false)
                            jsonObject.addProperty("filePath", androidPath)
                            jsonObject.addProperty("thumbnailPath", uploadingFile.thumbnailPath)
                            jsonArray.add(jsonObject)
                            Log.i(TAG_VULT, "Uri: $fileUri")
                            Log.i(TAG_VULT, "Uri path: ${fileUri?.path}")
                            Log.i(TAG_VULT, "File name: $fileName")
                        }
                        val jsonString = gson.toJson(jsonArray)

                        isRefresh(true)
                        GlobalScope.launch(Dispatchers.IO) {
                            vultViewModel.uploadMultipleFilesWithCallback(
                                workDir = requireContext().filesDir.absolutePath,
                                multiUploadRequestBody = jsonString,
                                callback = statusCallbackForMultiUpload
                            )
                        }
                        isRefresh(false)
                    }
                } else if (result.resultCode == RESULT_ERROR) {
                    makeToast("Error picking image gallery")
                } else {
                    makeToast("Not selected")
                }
            }

        binding.tvStorageUsed.text =
            getString(R.string.storage_used, 0.getConvertedSize(), 0.getConvertedSize())

        filesAdapter = FilesAdapter(mutableListOf(), this, context)
        filesAdapter!!.setThumbnailDownloadCallback(this)
        filesAdapter!!.setMultiSelect(vultViewModel.isMultiSelectEnabled().value==true)
        binding.rvAllFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllFiles.adapter = filesAdapter
        binding.multiDownload.setOnClickListener {
            vultViewModel.onMultiSelectCheckBoxClicked()
        }

        vultViewModel.isNonEmptyFilesListAvailable.observe(viewLifecycleOwner){isNonEmptyFilesListAvailable->
            if(isNonEmptyFilesListAvailable)binding.multiDownload.visibility = View.VISIBLE
            else binding.multiDownload.visibility = View.GONE
        }
        vultViewModel.isMultiSelectEnabled().observe(viewLifecycleOwner) { isEnabled ->
            filesAdapter!!.setMultiSelect(isEnabled)
            filesAdapter!!.notifyDataSetChanged()
            if (!isEnabled) {
                vultViewModel.disSelectAllFiles()
            }
        }

        vultViewModel.getSelectedFiles().observe(viewLifecycleOwner) { selectedFiles ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                filesAdapter!!.setSelectedFiles(
                    selectedFiles.map { it.name!! }.toSet()
                )
            } else {
                val fileNames: MutableSet<String> =
                    HashSet()
                for (file in selectedFiles) {
                    fileNames.add(file.name!!)
                }
                filesAdapter!!.setSelectedFiles(fileNames)
            }
            if(selectedFiles.isEmpty()){
                binding.confirmMultiDownload.visibility = View.GONE
            }else{
                binding.confirmMultiDownload.visibility = View.VISIBLE
            }
            filesAdapter!!.notifyDataSetChanged()
        }

        vultViewModel.isMultiSelectEnabled().observe(viewLifecycleOwner) { isEnabled ->
            if (isEnabled) {
                binding.cancelMultiDownload.visibility = View.VISIBLE
                binding.multiDownload.alpha = 0F
            } else {
                binding.cancelMultiDownload.visibility = View.GONE
                binding.confirmMultiDownload.visibility = View.GONE
                binding.multiDownload.alpha = 1F
            }
        }
        vultViewModel.notifyDataSetChanged.observe(viewLifecycleOwner) { dataChanged ->
            if (dataChanged) {
                filesAdapter!!.notifyDataSetChanged()
                vultViewModel.makeNotifyDataSetChangedFalse()
            }

        }

        vultViewModel.getFilesList().observe(viewLifecycleOwner) { files ->
            if (files != null)
                filesAdapter!!.files = files
            else
                filesAdapter!!.files = mutableListOf()
            filesAdapter!!.notifyDataSetChanged()
        }

        binding.cvUploadImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            documentPicker.launch(intent)
        }

        binding.cvUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            documentPicker.launch(intent)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                vultViewModel.listFiles("/")
                isRefresh(false)
            }
        }

        binding.confirmMultiDownload.setOnClickListener {
            Log.i(
                TAG_VULT,
                "File(s) multi-download clicked"
            )
            val downloadDirectoryPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            CoroutineScope(Dispatchers.Main).launch {
                val snackbar = Snackbar
                    .make(
                        binding.root,
                        "Downloading selected file(s)",
                        Snackbar.LENGTH_SHORT
                    )
                snackbar.show()
                withContext(Dispatchers.IO) {
                    vultViewModel.multiDownloadFilesWithCallback(
                        downloadDirectoryPath, MultiDownloadCallBack (vultViewModel.getSelectedFiles().value!!, binding.root)
                    )
                }
            }
        }

        return binding.root
    }

    private fun makeToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateTotalSizeProgress() {
        binding.allocationProgressView.progress = 0
        binding.tvAllocationDate.text = getString(R.string.no_allocation)
        binding.tvStorageUsed.text = getString(R.string.no_allocation)
    }

    private fun makeFileCopyInCacheDir(contentUri: Uri): String? {
        try {
            val filePathColumn = arrayOf(
                //Base File
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.TITLE,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.SIZE,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                //Normal File
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.MediaColumns.DISPLAY_NAME
            )
            //val contentUri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", File(mediaUrl))
            val returnCursor =
                contentUri.let {
                    requireContext().contentResolver.query(
                        it,
                        filePathColumn,
                        null,
                        null,
                        null
                    )
                }
            if (returnCursor != null) {
                returnCursor.moveToFirst()
                val nameIndex = returnCursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                val name = returnCursor.getString(nameIndex)
                val file = File(requireContext().cacheDir, name)
                val inputStream = requireContext().contentResolver.openInputStream(contentUri)
                val outputStream = FileOutputStream(file)
                var read = 0
                val maxBufferSize = 1 * 1024 * 1024
                val bytesAvailable = inputStream!!.available()

                //int bufferSize = 1024;
                val bufferSize = Math.min(bytesAvailable, maxBufferSize)
                val buffers = ByteArray(bufferSize)
                while (inputStream.read(buffers).also { read = it } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream.close()
                outputStream.close()
                Log.e("File Path", "Path " + file.path)
                Log.e("File Size", "Size " + file.length())
                return file.absolutePath
            }
        } catch (ex: Exception) {
            Log.e("Exception", ex.message!!)
        }
        return contentUri.let { Utils(requireContext()).getRealPathFromURI(it) }
    }

    private fun isRefresh(bool: Boolean) {
        requireActivity().window.setFlags(
            if (bool) WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE else 0,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        binding.swipeRefreshLayout.isRefreshing = bool
    }

    override fun onShareLongPressFileClickListener(position: Int) {
        isRefresh(true)
        Snackbar.make(
            requireView(),
            "Generating Auth Ticket for ${vultViewModel.filesList.value!![position].name}",
            Snackbar.LENGTH_SHORT
        ).show()
        Log.i(
            TAG_VULT,
            "File long clicked: ${vultViewModel.filesList.value!![position].name}"
        )
        val file = vultViewModel.filesList.value!![position]
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        CoroutineScope(Dispatchers.Main).launch {
            val authShareToken = vultViewModel.getShareAuthToken(
                fileRemotePath = file.path,
                fileName = file.name,
                fileReferenceType = file.type,
                fileRefereeClientId = ""
            )
            if (authShareToken == null) {
                Snackbar.make(
                    requireView(),
                    "Error generating Auth Ticket",
                    Snackbar.LENGTH_SHORT
                ).show()
                Log.e(
                    TAG_VULT,
                    "onShareLongPressFileClickListener: Error generating Auth Ticket"
                )
            } else {
                val clipData = ClipData.newPlainText("authShareToken", authShareToken)
                clipboardManager.setPrimaryClip(clipData)
                Snackbar.make(
                    requireView(),
                    "Auth Ticket copied to clipboard",
                    Snackbar.LENGTH_SHORT
                ).show()
                Log.i(
                    TAG_VULT,
                    "onShareLongPressFileClickListener: Share Auth Token = $authShareToken"
                )
            }
            isRefresh(false)
        }
    }

    private fun clearLocalFile(localPath: String) {
        val file = File(localPath)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onDownloadToOpenFileClickListener(filePosition: Int) {
        isRefresh(true)
        val files = vultViewModel.filesList.value!![filePosition]
        if (files.getAndroidPath() == null || files.getAndroidPath()!!.isEmpty()) {
            files.setAndroidPath(requireContext().filesDir.absolutePath + files.remotePath)
            clearLocalFile(files.getAndroidPath()!!)
            vultViewModel.updateFileWithAndroidPath(files.remotePath, files.getAndroidPath()!!)
        }
        makeDirectories(
            requireContext(),
            files.remotePath.substring(0, files.remotePath.lastIndexOf("/") + 1)
        )

        Log.i(
            TAG_VULT,
            "File clicked: ${files.name}"
        )
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                files.remotePath.let {
                    vultViewModel.downloadFileWithCallback(
                        it,
                        requireContext().filesDir.absolutePath + "/",
                        object : StatusCallbackMocked {
                            override fun commitMetaCompleted(
                                p0: String?,
                                p1: String?,
                                p2: java.lang.Exception?,
                            ) {
                                Log.d(TAG_VULT, "commitMetaCompleted: ")
                                Log.d(TAG_VULT, "commitMetaCompleted: p0: $p0")
                                Log.d(TAG_VULT, "commitMetaCompleted: p1: $p1")
                                Log.d(TAG_VULT, "commitMetaCompleted: p2: $p2")
                            }

                            override fun completed(
                                p0: String?,
                                p1: String?,
                                p2: String?,
                                p3: String?,
                                p4: Long,
                                p5: Long,
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    isRefresh(false)
                                    updateFilePreview(filePosition, files)
                                }
                            }

                            override fun error(
                                p0: String?,
                                p1: String?,
                                p2: Long,
                                p3: java.lang.Exception?,
                            ) {
                                Log.d(TAG_VULT, "error: ")
                                Log.d(TAG_VULT, "error: p0: $p0")
                                Log.d(TAG_VULT, "error: p1: $p1")
                                Log.d(TAG_VULT, "error: p2: $p2")
                                Log.d(TAG_VULT, "error: p3: $p3")
                                CoroutineScope(Dispatchers.Main).launch {
                                    isRefresh(false)
                                    val snackbar = Snackbar
                                        .make(
                                            binding.root,
                                            "failed to download file for viewing",
                                            Snackbar.LENGTH_SHORT
                                        )
                                    snackbar.show()
                                }
                            }

                            override fun inProgress(
                                p0: String?,
                                p1: String?,
                                p2: Long,
                                p3: Long,
                                p4: ByteArray?,
                            ) {
                                Log.d(TAG_VULT, "inProgress: ")
                                Log.d(TAG_VULT, "inProgress: p0: $p0")
                                Log.d(TAG_VULT, "inProgress: p1: $p1")
                                Log.d(TAG_VULT, "inProgress: p2: $p2")
                                Log.d(TAG_VULT, "inProgress: p3: $p3")
                                Log.d(TAG_VULT, "inProgress: p4: $p4")
                            }

                            override fun repairCompleted(p0: Long) {
                                Log.d(TAG_VULT, "repairCompleted: ")
                                Log.d(TAG_VULT, "repairCompleted: p0: $p0")
                            }

                            override fun started(p0: String?, p1: String?, p2: Long, p3: Long) {
                                Snackbar.make(
                                    binding.root,
                                    "Downloading ${vultViewModel.filesList.value!![filePosition].name}",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
            isRefresh(false)
        }
    }

    private fun previewAction(position: Int, next: Boolean) {
        var position = position
        if (position < 0) position =
            filesAdapter!!.itemCount - 1 else if (position == filesAdapter!!.itemCount) position =
            0
        val selected: Files = vultViewModel.filesList.value!![position]
        if (selected.mimeType != null && (selected.mimeType!!.startsWith("image/") || selected.mimeType!!
                .startsWith("video/") || selected.mimeType!! == "application/pdf"
                    )
        ) viewFileAction(position, selected) else {
            if (next) previewAction(position + 1, true) else previewAction(position - 1, false)
        }
    }

    private fun showPreviewDialog(position: Int, files: Files) {
        val titleText = previewLayout!!.findViewById<TextView>(R.id.fileName)
        titleText.text = files.name
        val btnNext = previewLayout!!.findViewById<ImageView>(R.id.btnNext)
        btnNext.setOnClickListener { previewAction(position + 1, true) }
        val btnPrevious = previewLayout!!.findViewById<ImageView>(R.id.btnPrevious)
        btnPrevious.setOnClickListener { previewAction(position - 1, false) }
        // Dismiss the dialog when the back button is clicked
        val backButton = previewLayout!!.findViewById<ImageView>(R.id.headerBack)
        backButton.setOnClickListener { previewDialog!!.dismiss() }
        if (!previewDialog!!.isShowing) previewDialog!!.show()
    }

    private fun openFile(position: Int, files: Files) {
        currentFilePosition = position
        var file: File? = null
        if (files.getAndroidPath() != null) file = File(files.getAndroidPath())
        val mimeType: String? = files.mimeType
        showPreviewDialog(position, files)
        if (file == null || !file.exists()) {
            if (mimeType?.startsWith("image/") == true || mimeType?.startsWith("video/") == true) {
                val thumbnailPath: String? = files.thumbnailPath
                if (thumbnailPath != null) {
                    val thumbnailFile: File? = files.thumbnailPath?.let { File(it) }
                    val filePreview: PhotoView = previewLayout?.findViewById(R.id.filePreview)!!
                    if (thumbnailFile?.exists() == true) filePreview.setImageURI(
                        Uri.fromFile(
                            thumbnailFile
                        )
                    )
                }
            } else if (mimeType.equals("application/pdf")) {
                val filePreview: PhotoView = previewLayout?.findViewById(R.id.filePreview)!!
                filePreview.setImageResource(R.drawable.ic_upload_document)
            }
            val loadingText = previewLayout?.findViewById<TextView>(R.id.loadingText)!!
            loadingText.visibility = View.VISIBLE
            return
        }
    }

    private fun updateFilePreview(position: Int, file: Files) {
        val actualFile = File(file.getAndroidPath())
        if (currentFilePosition == position && actualFile.exists()) {
            val loadingText = previewLayout?.findViewById<TextView>(R.id.loadingText)!!
            loadingText.visibility = View.GONE
            val mimeType: String? = file.mimeType
            val photoPreview: PhotoView = previewLayout!!.findViewById(R.id.filePreview)
            val pdfPreview: PDFView = previewLayout!!.findViewById(R.id.pdfPreview)
            if (mimeType != null && mimeType.startsWith("image/")) {
                photoPreview.visibility = View.VISIBLE
                pdfPreview.visibility = View.INVISIBLE
                photoPreview.setImageURI(Uri.fromFile(actualFile))
            } else if (mimeType.equals("application/pdf")) {
                pdfPreview.visibility = View.VISIBLE
                photoPreview.visibility = View.INVISIBLE
                try {
                    pdfPreview.fromFile(actualFile).load()
                } catch (e: Exception) {
                    e.message?.let { Log.e(TAG_VULT, it) }
                }
            } else {
                var f: File? = null
                if (file.getAndroidPath() != null) f = actualFile
                val fileUri = f?.let {
                    FileProvider.getUriForFile(
                        requireActivity(),
                        requireActivity().applicationContext.packageName + ".files.provider",
                        it
                    )
                }
                if (previewDialog?.isShowing == true) previewDialog!!.dismiss()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.flags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                intent.data = fileUri

                val packageManager: PackageManager = requireActivity().packageManager
                val activities: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

                if (activities.isEmpty()) {
                    val alertDialogBuilder = AlertDialog.Builder(requireContext())
                    alertDialogBuilder.setTitle("No Suitable App")
                        .setMessage("There is no suitable app on your phone to open this file. You can download the file instead.")
                        .setPositiveButton("Download File") { _, _ ->
                            onDownloadFileClickListener(position)
                        }
                        .setNegativeButton("Cancel", null)
                    alertDialogBuilder.show()
                } else if (fileUri.toString().endsWith(".apk")) {
                    val apkDialogBuilder = AlertDialog.Builder(requireContext())
                    apkDialogBuilder.setTitle("APK File")
                        .setMessage("This file is an APK file. Preview not available. You can download it instead")
                        .setPositiveButton("Download File") { _, _ ->
                            onDownloadFileClickListener(position)
                        }
                        .setNegativeButton("Cancel", null)
                    apkDialogBuilder.show()
                } else {
                    val j = Intent.createChooser(intent, "Choose an application to open with:")
                    startActivity(j)
                }

            }
        }
    }

    override fun onFileClick(filePosition: Int) {
        val selectedFile = vultViewModel.filesList.value!![filePosition]
        viewFileAction(filePosition, selectedFile)

    }

    override fun onDownloadFileClickListener(filePosition: Int) {
        isRefresh(true)
        val files = vultViewModel.filesList.value!![filePosition]
        Log.i(
            TAG_VULT,
            "File download clicked: ${files.name}"
        )
        val downloadDirectoryPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                files.remotePath.let {
                    vultViewModel.downloadFileWithCallback(
                        it,
                        downloadDirectoryPath, object : StatusCallbackMocked {
                            override fun commitMetaCompleted(
                                p0: String?,
                                p1: String?,
                                p2: java.lang.Exception?,
                            ) {
                                Log.d(TAG_VULT, "commitMetaCompleted: ")
                                Log.d(TAG_VULT, "commitMetaCompleted: p0: $p0")
                                Log.d(TAG_VULT, "commitMetaCompleted: p1: $p1")
                                Log.d(TAG_VULT, "commitMetaCompleted: p2: $p2")
                            }

                            override fun completed(
                                p0: String?,
                                p1: String?,
                                p2: String?,
                                p3: String?,
                                p4: Long,
                                p5: Long,
                            ) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    isRefresh(false)
                                    val snackbar = Snackbar
                                        .make(
                                            binding.root,
                                            "${files.name} downloaded",
                                            Snackbar.LENGTH_SHORT
                                        )
                                    snackbar.show()
                                }
                            }

                            override fun error(
                                p0: String?,
                                p1: String?,
                                p2: Long,
                                p3: java.lang.Exception?,
                            ) {
                                Log.d(TAG_VULT, "error: ")
                                Log.d(TAG_VULT, "error: p0: $p0")
                                Log.d(TAG_VULT, "error: p1: $p1")
                                Log.d(TAG_VULT, "error: p2: $p2")
                                Log.d(TAG_VULT, "error: p3: $p3")
                                CoroutineScope(Dispatchers.Main).launch {
                                    isRefresh(false)
                                    if (p3?.message?.contains("Local file already exists") == true) {
                                        val snackbar = Snackbar
                                            .make(
                                                binding.root,
                                                "File already downloaded",
                                                Snackbar.LENGTH_SHORT
                                            )
                                        snackbar.show()
                                    } else {
                                        val snackbar = Snackbar
                                            .make(
                                                binding.root,
                                                "Failed to download file",
                                                Snackbar.LENGTH_SHORT
                                            )
                                        snackbar.show()
                                    }
                                }
                            }

                            override fun inProgress(
                                p0: String?,
                                p1: String?,
                                p2: Long,
                                p3: Long,
                                p4: ByteArray?,
                            ) {
                                Log.d(TAG_VULT, "inProgress: ")
                                Log.d(TAG_VULT, "inProgress: p0: $p0")
                                Log.d(TAG_VULT, "inProgress: p1: $p1")
                                Log.d(TAG_VULT, "inProgress: p2: $p2")
                                Log.d(TAG_VULT, "inProgress: p3: $p3")
                                Log.d(TAG_VULT, "inProgress: p4: $p4")
                            }

                            override fun repairCompleted(p0: Long) {
                                Log.d(TAG_VULT, "repairCompleted: ")
                                Log.d(TAG_VULT, "repairCompleted: p0: $p0")
                            }

                            override fun started(p0: String?, p1: String?, p2: Long, p3: Long) {
                                Snackbar.make(
                                    binding.root,
                                    "Downloading ${vultViewModel.filesList.value!![filePosition].name}",
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
            }
            isRefresh(false)
        }
    }

    override fun onFileMultiSelectClick(position: Int) {
        val selected: Files = filesAdapter!!.files[position]
        val fileNames: MutableList<String> = ArrayList()
        val filesList: List<Files> = vultViewModel.getSelectedFiles().value!!
        for (file in filesList) fileNames.add(file.name!!)
        if (fileNames.contains(selected.name)) vultViewModel.removeFileFromSelectedList(
            selected
        ) else vultViewModel.addFileToSelectedList(selected)
    }

    private fun viewFileAction(filePosition: Int, selectedFile: Files) {
        openFile(filePosition, selectedFile)
        if (selectedFile.getAndroidPath()?.let { File(it).exists() } == true) {
            updateFilePreview(filePosition, selectedFile)
            return
        }
        onDownloadToOpenFileClickListener(filePosition)
    }

    private val statusCallbackForMultiUpload = object : StatusCallbackMocked {
        override fun commitMetaCompleted(p0: String?, p1: String?, p2: Exception?) {
            Log.d(TAG_VULT, "commitMetaCompleted: ")
            Log.d(TAG_VULT, "commitMetaCompleted: p0: $p0")
            Log.d(TAG_VULT, "commitMetaCompleted: p1: $p1")
            Log.d(TAG_VULT, "commitMetaCompleted: p2: $p2")
        }

        override fun completed(
            p0: String?,
            p1: String?,
            p2: String?,
            p3: String?,
            p4: Long,
            p5: Long,
        ) {
            Log.d(TAG_VULT, "completed: ")
            Log.d(TAG_VULT, "completed: p0: $p0")
            Log.d(TAG_VULT, "completed: p1: $p1")
            Log.d(TAG_VULT, "completed: p2: $p2")
            Log.d(TAG_VULT, "completed: p3: $p3")
            Log.d(TAG_VULT, "completed: p4: $p4")
            Log.d(TAG_VULT, "completed: p5: $p5")
            vultViewModel.modifyUploadStatusToCompleteForFile(p1)
            CoroutineScope(vultViewModel.viewModelScope.coroutineContext).launch {
                vultViewModel.listFiles("/")
            }
            showSuccessSnackbar()
        }

        override fun error(p0: String?, p1: String?, p2: Long, p3: Exception?) {
            Log.d(TAG_VULT, "error: ")
            Log.d(TAG_VULT, "error: p0: $p0")
            Log.d(TAG_VULT, "error: p1: $p1")
            Log.d(TAG_VULT, "error: p2: $p2")
            Log.d(TAG_VULT, "error: p3: $p3")
            Snackbar.make(
                binding.root,
                "Unable to upload" +
                        "Error: ${p3?.message}",
                Snackbar.LENGTH_SHORT
            ).show()
            CoroutineScope(Dispatchers.Main).launch {
                isRefresh(false)
            }
        }

        override fun inProgress(p0: String?, p1: String?, p2: Long, p3: Long, p4: ByteArray?) {
            Log.d(TAG_VULT, "inProgress: ")
            Log.d(TAG_VULT, "inProgress: p0: $p0")
            Log.d(TAG_VULT, "inProgress: p1: $p1")
            Log.d(TAG_VULT, "inProgress: p2: $p2")
            Log.d(TAG_VULT, "inProgress: p3: $p3")
            Log.d(TAG_VULT, "inProgress: p4: $p4")
            vultViewModel.modifyProgressForFile(p1, p3)
        }

        override fun repairCompleted(p0: Long) {
            Log.d(TAG_VULT, "repairCompleted: ")
            Log.d(TAG_VULT, "repairCompleted: p0: $p0")
        }

        override fun started(p0: String?, p1: String?, p2: Long, p3: Long) {
            Log.d(TAG_VULT, "started: ")
            Log.d(TAG_VULT, "started: p0: $p0")
            Log.d(TAG_VULT, "started: p1: $p1")
            Log.d(TAG_VULT, "started: p2: $p2")
            Log.d(TAG_VULT, "started: p3: $p3")
            vultViewModel.modifyTotalSizeForFile(p1, p3)
            showUploadingSnackbar()
        }
    }

    override fun downloadThumbnail(file: Files, position: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                if (file.thumbnailPath == null || file.thumbnailPath!!.isEmpty()) {
                    file.thumbnailPath =
                        requireContext().filesDir.absolutePath + "/thumbnail" + file.remotePath
                    vultViewModel.updateFileWithThumbNailPath(file.remotePath, file.thumbnailPath!!)
                }
                makeDirectories(
                    requireContext(),
                    "/thumbnail" + file.remotePath
                        .substring(0, file.remotePath.lastIndexOf("/") + 1)
                )
                vultViewModel.downloadThumbnail(file, position, object : StatusCallbackMocked {
                    override fun commitMetaCompleted(
                        p0: String?,
                        p1: String?,
                        p2: java.lang.Exception?
                    ) {

                    }

                    override fun completed(
                        p0: String?,
                        p1: String?,
                        p2: String?,
                        p3: String?,
                        p4: Long,
                        p5: Long
                    ) {
                        filesAdapter!!.notifyDataSetChanged()
                    }

                    override fun error(
                        p0: String?,
                        p1: String?,
                        p2: Long,
                        p3: java.lang.Exception?
                    ) {
                    }

                    override fun inProgress(
                        p0: String?,
                        p1: String?,
                        p2: Long,
                        p3: Long,
                        p4: ByteArray?
                    ) {
                    }

                    override fun repairCompleted(p0: Long) {
                    }

                    override fun started(p0: String?, p1: String?, p2: Long, p3: Long) {
                    }

                })
            }
        }
    }

    private fun showUploadingSnackbar() {
        currentSnackbar?.dismiss()
        currentSnackbar = Snackbar.make(binding.root, "Uploading file...", Snackbar.LENGTH_SHORT)
        currentSnackbar!!.show()
    }

    // Method to show the "Successfully Uploaded the File." Snackbar
    private fun showSuccessSnackbar() {
        currentSnackbar?.dismiss()
        currentSnackbar =
            Snackbar.make(binding.root, "Successfully Uploaded the File.", Snackbar.LENGTH_SHORT)
        currentSnackbar!!.show()
    }
}
