package org.zus.bolt.helloworld.ui.vult

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zus.bolt.helloworld.models.blobber.BlobberNodeModel
import org.zus.bolt.helloworld.models.blobber.BlobbersUrlIdModel
import org.zus.bolt.helloworld.models.blobber.StatsModel
import org.zus.bolt.helloworld.models.vult.AllocationModel
import org.zus.bolt.helloworld.models.vult.FileModel
import org.zus.bolt.helloworld.models.vult.FileResponseModel
import sdk.Sdk
import sdk.StorageSDK
import zbox.Allocation
import zbox.StatusCallbackMocked

class VultViewModel : ViewModel() {
    lateinit var storageSDK: StorageSDK
    lateinit var allocationId: String
    lateinit var allocation: AllocationModel
    var files: MutableLiveData<List<FileModel>> = MutableLiveData()

    companion object {
        fun initZboxStorageSDK(config: String, walletJSON: String): StorageSDK =
            try {
                Sdk.init(config)
                Log.i(TAG_VULT, "initZboxStorageSDK: sdk initialized successfully")
                Sdk.initStorageSDK(walletJSON, config)
            } catch (e: Exception) {
                Log.e(TAG_VULT, "initZboxStorageSDK Exception: ", e)
                StorageSDK()
            }

    }

    private val statusCallbackMocked = object : StatusCallbackMocked {
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
            CoroutineScope(viewModelScope.coroutineContext).launch {
                listFiles("/")
            }
        }

        override fun error(p0: String?, p1: String?, p2: Long, p3: Exception?) {
            Log.d(TAG_VULT, "error: ")
            Log.d(TAG_VULT, "error: p0: $p0")
            Log.d(TAG_VULT, "error: p1: $p1")
            Log.d(TAG_VULT, "error: p2: $p2")
            Log.d(TAG_VULT, "error: p3: $p3")
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
        }

    }

    fun createAllocation(
        allocationName: String,
        dataShards: Long,
        parityShards: Long,
        allocationSize: Long,
        expirationSeconds: Long,
        lockTokens: String,
    ) {
        Log.i(TAG_VULT, "createAllocation: ")
        Log.i(TAG_VULT, "createAllocation: allocationName: $allocationName")
        Log.i(TAG_VULT, "createAllocation: dataShards: $dataShards")
        Log.i(TAG_VULT, "createAllocation: parityShards: $parityShards")
        Log.i(TAG_VULT, "createAllocation: allocationSize: $allocationSize")
        Log.i(TAG_VULT, "createAllocation: expirationSeconds: $expirationSeconds")
        Log.i(TAG_VULT, "createAllocation: lockTokens: $lockTokens")

        try {
            storageSDK.createAllocation(
                allocationName,
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

    fun createAllocationWithBlobber(
        allocationName: String,
        dataShards: Long,
        parityShards: Long,
        allocationSize: Long,
        expirationNanoSeconds: Long,
        lockTokens: String,
        blobbersUrls: String,
        blobberIds: String,
    ) {
        Log.i(TAG_VULT, "createAllocationWithBlobber: ")
        Log.i(TAG_VULT, "createAllocationWithBlobber: allocationName: $allocationName")
        Log.i(TAG_VULT, "createAllocationWithBlobber: dataShards: $dataShards")
        Log.i(TAG_VULT, "createAllocationWithBlobber: parityShards: $parityShards")
        Log.i(TAG_VULT, "createAllocationWithBlobber: allocationSize: $allocationSize")
        Log.i(
            TAG_VULT,
            "createAllocationWithBlobber: expirationNanoSeconds: $expirationNanoSeconds"
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
                expirationNanoSeconds,
                lockTokens,
                blobbersUrls,
                blobberIds
            )
            Log.i(TAG_VULT, "createAllocationWithBlobber: successfully created allocation")
        } catch (e: Exception) {
            Log.e(TAG_VULT, "createAllocationWithBlobber Exception: ", e)
        }
    }

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

    suspend fun uploadFile(
        workDir: String,
        fileName: String,
        filePathURI: String?,
        fileAttr: String?,
    ) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "uploadFile: ")
            Log.i(TAG_VULT, "uploadFile: fileName: $fileName")
            Log.i(TAG_VULT, "uploadFile: filePathURI: $filePathURI")
            Log.i(TAG_VULT, "uploadFile: fileAttr: $fileAttr")
            try {
                getAllocation()?.uploadFile(
                    /*work dir =*/
                    workDir,
                    /* local path =*/
                    filePathURI,
                    /* remote path =*/
                    "/$fileName",
                    /*file attrs =*/
                    fileAttr,
                    false,
                    statusCallbackMocked
                )
            } catch (e: Exception) {
                Log.e(TAG_VULT, "uploadFile Exception: ", e)
            }
        }
    }

    suspend fun downloadFile(fileName: String, downloadPath: String) {
        withContext(Dispatchers.IO) {
            Log.i(TAG_VULT, "downloadFile: ")
            Log.i(TAG_VULT, "downloadFile: fileName: $fileName")
            Log.i(TAG_VULT, "downloadFile: downloadPath: $downloadPath")
            try {
                getAllocation()?.downloadFile(
                    /* remote path =*/
                    "/$fileName",
                    /* file local download path =*/
                    downloadPath,
                    statusCallbackMocked
                )
            } catch (e: Exception) {
                Log.e(TAG_VULT, "downloadFile Exception: ", e)
            }
        }
    }

    suspend fun listFiles(remotePath: String) {
        return withContext(Dispatchers.IO) {
            getAllocation()?.let { allocation ->
                try {
                    val json = allocation.listDir(remotePath)
                    Log.i(TAG_VULT, "listFiles: json: $json")
                    val files = Gson().fromJson(json, FileResponseModel::class.java)
                    this@VultViewModel.files.postValue(files.list)
                } catch (e: Exception) {
                    Log.e(TAG_VULT, "listFiles Exception: ", e)
                }
            }
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
/* fun shareFile(
     remotePath: String,
     fileName: String,
     shareDuration: Long
 ) {
     storageSDK.getAllocation(storageSDK.allocations.split(' ')[0])
         .getShareAuthToken()
     file name =
     fileName,
     share duration =
     shareDuration,
     statusCallbackMocked
     )
 }*/
}
