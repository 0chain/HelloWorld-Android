package org.zus.bolt.helloworld.ui.vult

import android.app.Activity
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
import android.widget.ProgressBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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

                        CoroutineScope(Dispatchers.IO).launch {
                            isRefresh(true)
                            vultViewModel.uploadFile(
                                requireContext().filesDir.absolutePath,
                                fileName,
                                filePath,
                                ""
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

                    CoroutineScope(Dispatchers.IO).launch {
                        isRefresh(true)
                        vultViewModel.uploadFile(
                            requireContext().filesDir.absolutePath,
                            fileName,
                            filePath,
                            ""
                        )
                        isRefresh(false)
                    }
                }
            }


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
            CoroutineScope(Dispatchers.IO).launch {
                vultViewModel.listFiles("/")
                isRefresh(false)
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
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
                requireActivity().runOnUiThread {
                    binding.allocationProgressView.progress = 0
                    binding.tvAllocationDate.text = getString(R.string.no_allocation)
                    binding.tvStorageUsed.text = getString(R.string.no_allocation)
                }

            } else {
                vultViewModel.getAllocation().let { allocation ->
                    if (allocation?.id == null) {
                        throw Exception("Allocation id is null")
                    } else {
                        val statsModel = vultViewModel.getStats(allocation.stats)
                        requireActivity().runOnUiThread {
                            binding.allocationProgressView.progress =
                                ((statsModel.used_size / allocation.size) * 100).toInt()
                            binding.tvAllocationDate.text =
                                allocation.expiration.getConvertedDateTime()
                            binding.tvStorageUsed.text = getString(
                                R.string.storage_used,
                                statsModel.used_size.getConvertedSize(),
                                allocation.size.getConvertedSize()
                            )
                        }
                    }
                }
            }
            vultViewModel.getAllocation()
            vultViewModel.listFiles("/")
            isRefresh(false)
        }

        return binding.root
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
                    requireContext().contentResolver.query(it,
                        filePathColumn,
                        null,
                        null,
                        null)
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
        requireActivity().runOnUiThread {
            binding.swipeRefreshLayout.isRefreshing = bool
        }
    }

    override fun onDownloadFileClick(filePosition: Int) {
        runBlocking {
            val downloadProgressBar = binding.rvAllFiles.getChildAt(filePosition)
                .findViewById<ProgressBar>(R.id.uploadsProgressBar)
            downloadProgressBar.visibility = View.VISIBLE
            Log.i(
                TAG_VULT,
                "File clicked: ${vultViewModel.files.value!![filePosition].name}"
            )
            //Create new folder in external directory.
            CoroutineScope(Dispatchers.IO).launch {
                vultViewModel.downloadFile(
                    vultViewModel.files.value!![filePosition].name,
                    downloadPath,
                )
                CoroutineScope(Dispatchers.Main).launch {
                    downloadProgressBar.visibility = View.GONE
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
        }
    }

    override fun onFileClick(filePosition: Int) {
        val file = File(
            downloadPath,
            vultViewModel.files.value!![filePosition].name
        )
        Log.i(TAG_VULT, "File clicked: ${file.absolutePath}")
        MediaScannerConnection.scanFile(requireContext(), arrayOf(file.absolutePath), null
        ) { _, uri ->
            if (uri == null) {
                Snackbar.make(binding.root,
                    "No file found Please Download first",
                    Snackbar.LENGTH_SHORT).show()
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
}
