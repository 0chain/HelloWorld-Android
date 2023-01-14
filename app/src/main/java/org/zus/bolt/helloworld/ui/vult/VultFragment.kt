package org.zus.bolt.helloworld.ui.vult

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.zus.bolt.helloworld.R
import org.zus.bolt.helloworld.databinding.VultFragmentBinding
import org.zus.bolt.helloworld.ui.mainactivity.MainViewModel
import org.zus.bolt.helloworld.utils.Utils
import zcncore.Zcncore
import java.io.File
import java.util.*

const val TAG_VULT = "VultFragment"

class VultFragment : Fragment() {
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
        val fileOpener =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
                if (result != null) {
                    /* Uploading files. */
                    val uri = result
                    Log.i(TAG_VULT, "file path uri ${uri.path}")
//                    val fileName = Utils(requireContext()).getFileName(uri!!)
                    val filePath = File(uri.path).absolutePath
//                    Log.i(TAG_VULT, "File path: $fileName")
                    Log.i(TAG_VULT, "File path: $filePath")
                }
            }
        val documentPicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val result: Intent? = result.data
                    if (result != null) {
                        /* Uploading files. */
                        val uri = result.data
                        Log.i(TAG_VULT, "file path uri ${uri!!.path}")
                        val projections = arrayOf(
                            MediaStore.Files.FileColumns.DISPLAY_NAME,
                            MediaStore.Files.FileColumns.SIZE,
                            MediaStore.Files.FileColumns.MIME_TYPE,
                            MediaStore.Files.FileColumns.DOCUMENT_ID,
                        )
                        requireContext().contentResolver.query(uri, projections, null, null, null)
                            .use { cursor ->
                                cursor?.moveToFirst()
                                val name = cursor?.getString(0)
                                val size = cursor?.getString(1)
                                val type = cursor?.getString(2)
                                val path = cursor?.getString(3)
                                Log.i(TAG_VULT, "File name: $name")
                                Log.i(TAG_VULT, "File size: $size")
                                Log.i(TAG_VULT, "File type: $type")
                                Log.i(TAG_VULT, "File path: $path")

                                /* Uploading files. */
                                CoroutineScope(Dispatchers.IO).launch {
                                    isRefresh(true)
                                    vultViewModel.uploadFile(
                                        workDir = requireContext().filesDir.absolutePath,
                                        fileName = name ?: "no name found",
                                        filePathURI = path,
                                        fileAttr = type
                                    )
                                    isRefresh(false)
                                }
                            }
                    }
                }
            }
        val getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                /* Uploading files. */
                Log.i(TAG_VULT, "file path uri ${uri!!.path}")
//                val fileName = Utils(requireContext()).getFileName(uri!!)
                val filePath = File(uri.path).absolutePath
                Log.i(TAG_VULT, "File path: $filePath")

                val projection = arrayOf(
                    MediaStore.Files.FileColumns.DATA,
                    MediaStore.Files.FileColumns.DISPLAY_NAME,
                    MediaStore.Files.FileColumns.SIZE,
                    MediaStore.Files.FileColumns.MIME_TYPE
                )
                requireContext().contentResolver.query(uri, projection, null, null, null).use {
                    if (it != null && it.moveToFirst()) {
                        val path = it.getString(0)
                        val name = it.getString(1)
                        val size = it.getLong(2)
                        val type = it.getString(3)
                        Log.i(TAG_VULT, "File path: $path")
                        Log.i(TAG_VULT, "File name: $name")
                        Log.i(TAG_VULT, "File size: $size")
                        Log.i(TAG_VULT, "File type: $type")
                        /* Uploading files. */
                        CoroutineScope(Dispatchers.IO).launch {
                            isRefresh(true)
                            vultViewModel.uploadFile(
                                workDir = requireContext().filesDir.absolutePath,
                                fileName = name ?: "no name found",
                                filePathURI = path,
                                fileAttr = type
                            )
                            isRefresh(false)
                        }
                    }
                }
            }
        val photoPicker =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    Log.i(TAG_VULT, "file path: uri: ${uri.path}")
                    val fileName = Utils(requireContext()).getFileName(uri)
//                    val filePath = requireContext().contentResolver.openFile(uri, "r", null)
                    val filePath = File(uri.path).absolutePath
                    Log.i(TAG_VULT, "file name: $fileName")
                    Log.i(TAG_VULT, "file path: $filePath")

                    val projection = arrayOf(
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.SIZE,
                        MediaStore.Images.Media.MIME_TYPE,
                    )

                    requireContext().contentResolver.query(uri, projection, null, null, null)
                        .use { cursor ->
                            cursor?.moveToFirst()
                            val path = cursor?.getString(0)
                            val name = cursor?.getString(1)
                            val size = cursor?.getString(2)
                            val type = cursor?.getString(3)
                            Log.i(TAG_VULT, "file path: $path")
                            Log.i(TAG_VULT, "file name: $name")
                            Log.i(TAG_VULT, "file size: $size")
                            Log.i(TAG_VULT, "file type: $type")
                            /* Uploading files. */
                            CoroutineScope(Dispatchers.IO).launch {
                                isRefresh(true)
                                vultViewModel.uploadFile(
                                    workDir = requireContext().filesDir.absolutePath,
                                    fileName = name ?: "no name found",
                                    filePathURI = path,
                                    fileAttr = type
                                )
                                isRefresh(false)
                            }

                        }


                }
            }

        /*binding.tvStorageUsed.text = getString(
            R.string.storage_used,
            if (mainViewModel.isAllocationInitialized()) Utils.getStorage(mainViewModel.allocation[0].stats.used_size) else "0",
            if (mainViewModel.isAllocationInitialized()) Utils.getStorage(mainViewModel.allocation[0].size) else "0"
        )*/

        val openFolderForDownloads =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    Log.i(TAG_VULT, "file path: uri: ${uri.path}")
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    val projections = arrayOf(
                        MediaStore.Files.FileColumns.DATA,
                        MediaStore.Files.FileColumns.DISPLAY_NAME,
                        MediaStore.Files.FileColumns.SIZE,
                        MediaStore.Files.FileColumns.MIME_TYPE,
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        requireContext().contentResolver.query(uri, projections, null, null).use {
                            if (it != null && it.moveToFirst()) {
                                val path = it.getString(0)
                                val name = it.getString(1)
                                val size = it.getLong(2)
                                val type = it.getString(3)
                                Log.i(TAG_VULT, "File path: $path")
                                Log.i(TAG_VULT, "File name: $name")
                                Log.i(TAG_VULT, "File size: $size")
                                Log.i(TAG_VULT, "File type: $type")
                                downloadPath = path
                            }
                        }
                    }
                }
            }
        val filesAdapter = FilesAdapter(mutableListOf(), object : FileClickListener {
            override fun onFileClick(filePosition: Int) {
                runBlocking {
                    downloadPath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
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
        })
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
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }
        binding.cvUploadDocument.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            documentPicker.launch(intent)
//            getContent.launch("*/*")
//            documentPicker.launch(intent)
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
//            Log.i(TAG_VULT, "Blobbers list : ${vultViewModel.storageSDK.blobbersList}")

            if (vultViewModel.getAllocation() == null) {
                vultViewModel.createAllocationWithBlobber(
                    allocationName = "test allocation",
                    dataShards = 2,
                    parityShards = 2,
                    allocationSize = 2147483648,
                    expirationNanoSeconds = Date().time + 30000,
                    lockTokens = Zcncore.convertToValue(1.0),
                    "",
                    "c66bfb5bccef53a2faf9acc66d1399a95da5e184b9a6560843ff9c9242cf292b,2f540a6749ac2c6513b1dd00add083635e81c787548aaf9ea49a74917d15264e,786a760f1377e755750f56a0f81ba6924bd11201e695abd1d9341ebba27d2420,ed8ebde6aefcd323ab38a5f4d4553f7dd0dc86bd2aff96a10faa0d4539883147,e02162da3cd257300a2ff0aaf17e829fc8e9c2e0fd88594ca1b4f516799499d5,06b191c97356720310c78e66006acb4fb78022524a78f6500289100cf3310bff"
                )
                requireActivity().runOnUiThread {
                    binding.allocationProgressView.progress = 0
                    binding.tvAllocationDate.text = getString(R.string.no_allocation)
                }

            } else {
                vultViewModel.allocationId = vultViewModel.getAllocation()!!.id
//                binding.allocationProgressView.progress =
//                    ((vultViewModel.getAllocation()!!.stats.used_size / mainViewModel.allocation[0].size) * 100).toInt()
//                binding.tvAllocationDate.text =
//                    Utils.getDateTime(mainViewModel.allocation[0].timeUnit)
            }
//            CoroutineScope(Dispatchers.Main).launch {
            vultViewModel.getAllocation()
            vultViewModel.listFiles("/")
            isRefresh(false)
//            }
        }

        return binding.root
    }

    private fun isRefresh(bool: Boolean) {
        requireActivity().runOnUiThread {
            binding.swipeRefreshLayout.isRefreshing = bool
        }
    }
}
