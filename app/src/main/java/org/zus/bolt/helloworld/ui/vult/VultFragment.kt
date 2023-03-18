package org.zus.bolt.helloworld.ui.vult

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.VultFragmentBinding
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedDateTime
import org.zus.bolt.helloworld.utils.Utils.Companion.getConvertedSize
import zbox.StatusCallbackMocked
import zcncore.Zcncore
import java.io.File
import java.io.FileOutputStream
import java.util.*


const val TAG_VULT = "VultFragment"

class VultFragment : Fragment(), FileClickListener {
    private lateinit var binding: VultFragmentBinding
    private lateinit var vultViewModel: VultViewModel
    private lateinit var mainViewModel: MainViewModel
    var downloadPath = ""

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
                    allocationName = "test allocation",
                    dataShards = 2,
                    parityShards = 2,
                    allocationSize = 2147483648,
                    expirationSeconds = Date().time / 1000 + 30000,
                    lockTokens = Zcncore.convertToValue(1.0),
                )
                updateTotalSizeProgress()

            } else {
                vultViewModel.getAllocation().let { allocation ->
                    if (allocation?.id == null) {
                        throw Exception("Allocation id is null")
                    } else {
                        val statsModel = vultViewModel.getStats(allocation.stats)
                        requireActivity().runOnUiThread {
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
            }

            vultViewModel.getAllocation()
            vultViewModel.listFiles("/")
            isRefresh(false)
        }

        val documentPicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val resultIntent: Intent? = result.data
                    if (resultIntent != null) {
                        /* Uploading files. */
                        val uri = resultIntent.data!!
                        val fileName = Utils(requireContext()).getFileName(uri)
                        val filePath = makeFileCopyInCacheDir(uri)

                        Log.i(TAG_VULT, "Uri: $uri")
                        Log.i(TAG_VULT, "Uri path: ${uri.path}")
                        Log.i(TAG_VULT, "File name: $fileName")
                        Log.i(TAG_VULT, "File path: $filePath")

                        CoroutineScope(Dispatchers.Main).launch {
                            isRefresh(true)
                            vultViewModel.uploadFileWithCallback(
                                workDir = requireContext().filesDir.absolutePath,
                                fileName = fileName,
                                filePathURI = filePath,
                                fileThumbnailPath = "",
                                callback = uploadStatusCallback
                            )
                            isRefresh(false)
                        }
                    }
                }
            }

        val photoPicker =
            registerForActivityResult(PickVisualMedia()) { uri ->
                if (uri != null) {
                    /* Uploading files. */
                    val fileName = Utils(requireContext()).getFileName(uri)
                    val filePath = makeFileCopyInCacheDir(uri)

                    Log.i(TAG_VULT, "Uri: $uri")
                    Log.i(TAG_VULT, "Uri path: ${uri.path}")
                    Log.i(TAG_VULT, "File name: $fileName")
                    Log.i(TAG_VULT, "File path: $filePath")

                    CoroutineScope(Dispatchers.Main).launch {
                        isRefresh(true)
                        vultViewModel.uploadFileWithCallback(
                            workDir = requireContext().filesDir.absolutePath,
                            fileName = fileName,
                            filePathURI = filePath,
                            fileThumbnailPath = "",
                            callback = uploadStatusCallback
                        )
                        isRefresh(false)
                    }
                }
            }

        binding.tvStorageUsed.text =
            getString(R.string.storage_used, 0.getConvertedSize(), 0.getConvertedSize())

        val filesAdapter = FilesAdapter(mutableListOf(), this)
        binding.rvAllFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAllFiles.adapter = filesAdapter



        vultViewModel.files.observe(viewLifecycleOwner) { files ->
            if (files != null)
                filesAdapter.files = files
            else
                filesAdapter.files = mutableListOf()
            filesAdapter.notifyDataSetChanged()
        }

        binding.cvUploadImage.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
        }

        binding.cvUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            documentPicker.launch(intent)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            CoroutineScope(Dispatchers.Main).launch {
                vultViewModel.listFiles("/")
                isRefresh(false)
            }
        }



        return binding.root
    }

    private fun updateTotalSizeProgress() {
        requireActivity().runOnUiThread {
            binding.allocationProgressView.progress = 0
            binding.tvAllocationDate.text = getString(R.string.no_allocation)
            binding.tvStorageUsed.text = getString(R.string.no_allocation)
        }
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
        binding.swipeRefreshLayout.isRefreshing = bool
    }

    override fun onShareLongPressFileClickListener(position: Int) {
        isRefresh(true)
        Snackbar.make(
            requireView(),
            "Generating Auth Ticket for ${vultViewModel.files.value!![position].name}",
            Snackbar.LENGTH_SHORT
        ).show()
        Log.i(
            TAG_VULT,
            "File long clicked: ${vultViewModel.files.value!![position].name}"
        )
        val file = vultViewModel.files.value!![position]
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

    override fun onDownloadFileClickListener(filePosition: Int) {
        isRefresh(true)
        Log.i(
            TAG_VULT,
            "File clicked: ${vultViewModel.files.value!![filePosition].name}"
        )
        //Create new folder in external directory.
        CoroutineScope(Dispatchers.Main).launch {
            vultViewModel.downloadFileWithCallback(
                vultViewModel.files.value!![filePosition].name,
                downloadPath, object : StatusCallbackMocked {
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
                            val intentOpenDownloadedFile = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(
                                    Utils(requireContext()).getUriForFile(
                                        File(
                                            downloadPath,
                                            vultViewModel.files.value!![filePosition].name
                                        )
                                    ),
                                    vultViewModel.files.value!![filePosition].mimetype
                                )
                                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                            }
                            try {
                                startActivity(intentOpenDownloadedFile)
                            } catch (e: Exception) {
                                Log.e(TAG_VULT, "Error: ${e.message}")
                            }
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
                            "Downloading ${vultViewModel.files.value!![filePosition].name}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    override fun onFileClick(filePosition: Int) {
        val file = File(
            downloadPath,
            vultViewModel.files.value!![filePosition].name
        )
        Log.i(TAG_VULT, "File clicked: ${file.absolutePath}")
        MediaScannerConnection.scanFile(
            requireContext(), arrayOf(file.absolutePath), null
        ) { _, uri ->
            if (uri == null) {
                Snackbar.make(
                    binding.root,
                    "No file found Please Downloaing the File...",
                    Snackbar.LENGTH_SHORT
                ).show()
                onDownloadFileClickListener(filePosition)
            } else {
                Log.i("onScanCompleted", uri.path ?: "No file found")
                val intentOpenDownloadedFile = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, vultViewModel.files.value!![filePosition].mimetype)
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                try {
                    startActivity(intentOpenDownloadedFile)
                } catch (e: Exception) {
                    Log.e(TAG_VULT, "Error: ${e.message}")
                }
            }
        }
    }

    private val uploadStatusCallback = object : StatusCallbackMocked {
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
            CoroutineScope(vultViewModel.viewModelScope.coroutineContext).launch {
                vultViewModel.listFiles("/")
            }
            Snackbar.make(
                binding.root,
                "Successfully Uploaded the File.",
                Snackbar.LENGTH_SHORT
            ).show()
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
        }

        override fun inProgress(p0: String?, p1: String?, p2: Long, p3: Long, p4: ByteArray?) {
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
            Log.d(TAG_VULT, "started: ")
            Log.d(TAG_VULT, "started: p0: $p0")
            Log.d(TAG_VULT, "started: p1: $p1")
            Log.d(TAG_VULT, "started: p2: $p2")
            Log.d(TAG_VULT, "started: p3: $p3")
            Snackbar.make(
                binding.root,
                "Uploading file...",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}
