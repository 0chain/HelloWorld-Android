package org.zus.helloworld.ui.vult

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.zus.helloworld.data.Files
import org.zus.helloworld.models.blobber.BlobberNodeModel
import org.zus.helloworld.models.blobber.BlobbersUrlIdModel
import org.zus.helloworld.models.blobber.StatsModel
import org.zus.helloworld.models.vult.AllocationModel
import sdk.Sdk
import sdk.StorageSDK
import zbox.Allocation
import zbox.StatusCallbackMocked

class VultViewModel : ViewModel() {
    lateinit var storageSDK: StorageSDK
    lateinit var allocationId: String
    lateinit var allocation: AllocationModel
    var filesList: MutableLiveData<MutableList<Files>?> = MutableLiveData()
    var totalStorageUsed = MutableLiveData<Long>()
    var notifyDataSetChanged = MutableLiveData<Boolean>()

    init {
        filesList.value = mutableListOf()
    }

    companion object {
        suspend fun initZboxStorageSDK(config: String, walletJSON: String): StorageSDK =
            withContext(Dispatchers.IO) {
                return@withContext try {
                    Sdk.init(config)
                    Log.i(TAG_VULT, "initZboxStorageSDK: sdk initialized successfully")
                    Sdk.initStorageSDK(walletJSON, config)
                } catch (e: Exception) {
                    Log.e(TAG_VULT, "initZboxStorageSDK Exception: ", e)
                    StorageSDK()
                }
            }
    }

    /**
     *  Create a new allocation
     *  @param allocationName Name of the allocation
     *  @param dataShards Number of data shards
     *  @param parityShards Number of parity shards
     *  @param allocationSize Size of the allocation in bytes
     *  @param expirationSeconds Expiration time in seconds (future timestamp for allocation expiration)
     *  @param lockTokens Lock tokens
     */

    suspend fun createAllocation(
        allocationName: String,
        dataShards: Long,
        parityShards: Long,
        allocationSize: Long,
        expirationSeconds: Long,
        lockTokens: String,
    ) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "createAllocation: ")
            Log.i(TAG_VULT, "createAllocation: allocationName: $allocationName")
            Log.i(TAG_VULT, "createAllocation: dataShards: $dataShards")
            Log.i(TAG_VULT, "createAllocation: parityShards: $parityShards")
            Log.i(TAG_VULT, "createAllocation: allocationSize: $allocationSize")
            Log.i(TAG_VULT, "createAllocation: expirationSeconds: $expirationSeconds")
            Log.i(TAG_VULT, "createAllocation: lockTokens: $lockTokens")

            try {
                storageSDK.createAllocation(
                    dataShards,
                    parityShards,
                    allocationSize,
                    expirationSeconds,
                    lockTokens
                )
                Log.i(TAG_VULT, "createAllocation: successfully created allocation")
            } catch (e: Exception) {
                Log.e(TAG_VULT, "createAllocation Exception: ", e)
            }
        }
    }

    /**
     *  Create a new allocation by specifying your own blobbers.
     *  @param allocationName Name of the allocation
     *  @param dataShards Number of data shards
     *  @param parityShards Number of parity shards
     *  @param allocationSize Size of the allocation in bytes
     *  @param expirationSeconds Expiration time in seconds (future timestamp for allocation expiration)
     *  @param lockTokens Lock tokens
     *  @param blobbersUrls Blobbers URLs
     *  @param blobberIds Blobber IDs
     */
    suspend fun createAllocationWithBlobber(
        allocationName: String,
        dataShards: Long,
        parityShards: Long,
        allocationSize: Long,
        expirationSeconds: Long,
        lockTokens: String,
        blobbersUrls: String,
        blobberIds: String,
    ) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "createAllocationWithBlobber: ")
            Log.i(TAG_VULT, "createAllocationWithBlobber: allocationName: $allocationName")
            Log.i(TAG_VULT, "createAllocationWithBlobber: dataShards: $dataShards")
            Log.i(TAG_VULT, "createAllocationWithBlobber: parityShards: $parityShards")
            Log.i(TAG_VULT, "createAllocationWithBlobber: allocationSize: $allocationSize")
            Log.i(
                TAG_VULT,
                "createAllocationWithBlobber: expirationSeconds: $expirationSeconds"
            )
            Log.i(TAG_VULT, "createAllocationWithBlobber: lockTokens: $lockTokens")
            Log.i(TAG_VULT, "createAllocationWithBlobber: blobbers: $blobbersUrls")
            Log.i(TAG_VULT, "createAllocationWithBlobber: blobberIds: $blobberIds")

            try {
                storageSDK.createAllocationWithBlobbers(
                    allocationName,
                    dataShards,
                    parityShards,
                    allocationSize,
                    expirationSeconds,
                    lockTokens,
                    blobbersUrls,
                    blobberIds
                )
                Log.i(TAG_VULT, "createAllocationWithBlobber: successfully created allocation")
            } catch (e: Exception) {
                Log.e(TAG_VULT, "createAllocationWithBlobber Exception: ", e)
            }
        }
    }

    /**
     *  Gets the current allocation if present for a wallet.
     */
    suspend fun getAllocation(): Allocation? {
        return withContext(Dispatchers.IO) {
            try {
                val allocations: AllocationModel?
                if (::allocation.isInitialized) {
                    allocations = allocation
                } else {
                    Log.i(TAG_VULT, "getAllocation: allocations json: ${storageSDK.allocations}")
                    allocations =
                        Gson().fromJson(storageSDK.allocations, AllocationModel::class.java)
                }
                storageSDK.getAllocation(allocations?.get(0)!!.id)

            } catch (e: Exception) {
                Log.e(TAG_VULT, "getAllocation Exception: ", e)
                null
            }
        }
    }

    /**
     *  Gets the current allocation if present for a wallet.
     */
    suspend fun getAllocationModel(): AllocationModel? {
        return withContext(Dispatchers.IO) {
            try {
                val allocations: AllocationModel?
                if (::allocation.isInitialized) {
                    allocations = allocation
                } else {
                    Log.i(TAG_VULT, "getAllocation: allocations json: ${storageSDK.allocations}")
                    allocations =
                        Gson().fromJson(storageSDK.allocations, AllocationModel::class.java)
                }
                return@withContext allocations
            } catch (e: Exception) {
                Log.e(TAG_VULT, "getAllocation Exception: ", e)
                return@withContext null
            }
        }
    }

    /**
     *  Uploads a file to the current allocation.
     *  @param workDir Working directory (temporary directory for gosdk to operate default is filsDir of the app)
     *  @param fileName Name of the file
     *  @param filePathURI File path URI (file location in the current android filesystem)
     *  @param fileThumbnailPath File thumbnail path (file location in the current android filesystem)
     *  @param callback Status callback (to monitor the whole upload process)
     */
    suspend fun uploadFileWithCallback(
        workDir: String,
        fileName: String,
        filePathURI: String?,
        fileThumbnailPath: String?,
        encryptFile: Boolean,
        webStreaming: Boolean,
        callback: StatusCallbackMocked,
    ) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "uploadFile: workDir: $workDir")
            Log.i(TAG_VULT, "uploadFile: fileName: $fileName")
            Log.i(TAG_VULT, "uploadFile: filePathURI: $filePathURI")
            Log.i(TAG_VULT, "uploadFile: fileThumbnailPath: $fileThumbnailPath")
            try {
                getAllocation()?.uploadFile(
                    /*work dir =*/
                    workDir,
                    /* local path =*/
                    filePathURI,
                    /* remote path =*/
                    "/$fileName",
                    /*thumbnail path =*/
                    fileThumbnailPath,
                    /* encrypt file= */
                    encryptFile,
                    /* webStreaming =*/
                    webStreaming,
                    /* StatusCallbackMocked =t*/
                    callback
                )
            } catch (e: Exception) {
                Log.e(TAG_VULT, "uploadFile Exception: ", e)
            }
        }
    }

    /**
     *  Downloads a file from the current allocation.
     *  @param remotePath Name of the file
     *  @param downloadPath Download path (file location in the current android filesystem)
     *  @param callback Status callback (to monitor the whole download process)
     */
    suspend fun downloadFileWithCallback(
        remotePath: String,
        downloadPath: String,
        callback: StatusCallbackMocked,
    ) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "downloadFile: ")
            Log.i(TAG_VULT, "downloadFile: fileName: $remotePath")
            Log.i(TAG_VULT, "downloadFile: downloadPath: $downloadPath")
            try {
                zbox.Zbox.downloadFile(allocationId,
                    /* remote path =*/
                    remotePath,
                    /* file local download path =*/
                    downloadPath,
                    callback
                )
            } catch (e: Exception) {
                Log.e(TAG_VULT, "downloadFile Exception: ", e)
            }
        }
    }

    /**
     *  Share file with another client
     *  @param fileRemotePath - remote path of the file
     *  @param fileName - name of the file
     *  @param fileReferenceType - type of the file reference (if not present, default is "")
     *  @param fileRefereeClientId - client id of the referee (if not present, default is "")
     */
    suspend fun getShareAuthToken(
        fileRemotePath: String?,
        fileName: String?,
        fileReferenceType: String?,
        fileRefereeClientId: String
    ): String? {
        return withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "shareFile: ")
            Log.i(TAG_VULT, "shareFile: fileName: $fileName")
            try {
                return@withContext getAllocation()?.getShareAuthToken(
                    fileRemotePath,
                    fileName,
                    fileReferenceType,
                    fileRefereeClientId
                )
            } catch (e: Exception) {
                Log.e(TAG_VULT, "shareFile Exception: ", e)
                return@withContext null
            }
        }
    }

    @Throws(java.lang.Exception::class)
    private fun bindData(listDir: String): List<Files>? {
        val dataList: MutableList<Files> = ArrayList()
        val jsonObject = JSONObject(listDir)
        if (jsonObject.getString("list") != "null") {
            val filesList = jsonObject.getJSONArray("list")
            for (i in 0 until filesList.length()) {
                val fileObj = filesList[i] as JSONObject
                Log.e("TAG", "Path of Fetching File ==>" + fileObj.getString("path"))
                dataList.add(Files.fromJson(fileObj, allocationId))
            }
        }
        return dataList
    }

    suspend fun listFiles(remotePath: String) {
        allocationId = getAllocation()?.id!!
        return withContext(Dispatchers.IO) {
            getAllocation()?.let { allocation ->
                try {
                    val json = allocation.listDir(remotePath)
                    Log.i(TAG_VULT, "listFiles: json: $json")
                    val fetchedList: List<Files>? = bindData(json)
                    if (fetchedList != null) {
                        for (item in fetchedList) {
                            val exists = filesList.value?.any { file ->
                                file.name == item.name
                            }

                            if (exists != true) {
                                filesList.value?.add(item)
                            }
                        }
                    }
                    var totalStorage = 0L
                    if (fetchedList != null) {
                        for (file in fetchedList) {
                            totalStorage += file.size
                        }
                    }
                    this@VultViewModel.totalStorageUsed.postValue(totalStorage)
                    notifyDataSetChanged.postValue(true)
                } catch (e: Exception) {
                    Log.e(TAG_VULT, "listFiles Exception: ", e)
                }
            }
        }
    }

    fun getStats(json: String): StatsModel {
        try {
            val statsModel = Gson().fromJson(json, StatsModel::class.java)
            Log.i(TAG_VULT, "getStats: stats: $statsModel")
            return statsModel
        } catch (e: Exception) {
            Log.e(TAG_VULT, "getStats Exception: ", e)
            throw Exception("getStats Exception: $e")
        }
    }

    fun getBlobberUrlsAndId(): BlobbersUrlIdModel {
        val blobbers = getBlobbers()
        val blobberUrls = blobbers.map { it.url }
        val blobberIds = blobbers.map { it.id }
        return BlobbersUrlIdModel(
            id = blobberIds.joinToString(","),
            url = blobberUrls.joinToString(",")
        )
    }


    private fun getBlobbers(): BlobberNodeModel {
        try {
            val json = storageSDK.blobbersList
            Log.i(TAG_VULT, "getBlobbers: json: $json")
            return Gson().fromJson(json, BlobberNodeModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG_VULT, "getBlobbers Exception: ", e)
            return BlobberNodeModel()
        }
    }

    suspend fun uploadMultipleFilesWithCallback(
        workDir: String,
        callback: StatusCallbackMocked,
        multiUploadRequestBody: String
    ) {
        return withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "uploadFiles: workDir: $workDir")
            Log.i(TAG_VULT, "uploadFiles: multiUploadRequestBody: $multiUploadRequestBody")
            try {
                zbox.Zbox.multiUpload(allocationId, workDir, multiUploadRequestBody, callback)
            } catch (e: Exception) {
                Log.e(TAG_VULT, "uploadFile Exception: ", e)
            }
        }
    }

    fun addFileToMutableList(uploadingFile: Files) {
        val currentValue = filesList.value
        currentValue?.add(uploadingFile)
        filesList.value = currentValue
    }

    fun modifyProgressForFile(remotePath: String?, uploadedBytes: Long) {
        val file = filesList.value?.find { file -> file.remotePath == remotePath }
        file?.uploadedBytes = uploadedBytes
        file?.setUploadStatus(Files.STATUS_STARTED)
        file?.let { modifiedFile ->
            val index = filesList.value?.indexOf(modifiedFile)
            index?.let {
                filesList.value?.set(it, modifiedFile)
                notifyDataSetChanged.postValue(true)
            }
        }
    }

    fun modifyUploadStatusToCompleteForFile(remotePath: String?) {
        val file = filesList.value?.find { file -> file.remotePath == remotePath }
        file?.setUploadStatus(Files.STATUS_COMPLETED)
        file?.let { modifiedFile ->
            val index = filesList.value?.indexOf(modifiedFile)
            index?.let {
                filesList.value?.set(it, modifiedFile)
                notifyDataSetChanged.postValue(true)
            }
        }
    }

    fun modifyTotalSizeForFile(remotePath: String?, totalSize: Long) {
        val file = filesList.value?.find { file -> file.remotePath == remotePath }
        file?.totalSize = totalSize
        file?.let { modifiedFile ->
            val index = filesList.value?.indexOf(modifiedFile)
            index?.let { filesList.value?.set(it, modifiedFile) }
        }
    }

    fun makeNotifyDataSetChangedFalse() {
        notifyDataSetChanged.postValue(false)
    }

    suspend fun downloadThumbnail(file: Files, position: Int, callback: StatusCallbackMocked) {
        withContext(Dispatchers.IO) {
            try {
                getAllocation()?.let { allocation ->
                    zbox.Zbox.downloadThumbnail(
                        allocation.id,
                        file.remotePath,
                        file.thumbnailPath,
                        callback
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateFileWithThumbNailPath(remotePath: String, thumbnailPath: String) {
        val file = filesList.value?.find { fileItem -> fileItem.remotePath == remotePath }
        file?.thumbnailPath = thumbnailPath
        file?.let { modifiedFile ->
            val index = filesList.value?.indexOf(modifiedFile)
            index?.let {
                filesList.value?.set(it, modifiedFile)
                notifyDataSetChanged.postValue(true)
            }
        }
    }

    fun updateFileWithAndroidPath(remotePath: String, androidPath: String) {
        val file = filesList.value?.find { fileItem -> fileItem.remotePath == remotePath }
        file?.thumbnailPath = androidPath
        file?.let { modifiedFile ->
            val index = filesList.value?.indexOf(modifiedFile)
            index?.let {
                filesList.value?.set(it, modifiedFile)
                notifyDataSetChanged.postValue(true)
            }
        }
    }
}
