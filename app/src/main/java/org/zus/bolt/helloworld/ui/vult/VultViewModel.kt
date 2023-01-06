package org.zus.bolt.helloworld.ui.vult

import android.util.Log
import androidx.lifecycle.ViewModel
import sdk.Sdk
import sdk.StorageSDK
import zbox.StatusCallbackMocked

class VultViewModel : ViewModel() {
    companion object {
        fun initZboxStorageSDK(config: String, walletJSON: String): StorageSDK =
            try {
                Sdk.initStorageSDK(walletJSON, config)
            } catch (e: Exception) {
                Log.e(TAG_VULT, "initZboxStorageSDK Exception: ", e)
                StorageSDK()
            }
    }

    lateinit var storageSDK: StorageSDK

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
        expirationNanoSeconds: Long,
        lockTokens: String,
    ) {
        Log.i(TAG_VULT, "createAllocation: ")
        Log.i(TAG_VULT, "createAllocation: allocationName: $allocationName")
        Log.i(TAG_VULT, "createAllocation: dataShards: $dataShards")
        Log.i(TAG_VULT, "createAllocation: parityShards: $parityShards")
        Log.i(TAG_VULT, "createAllocation: allocationSize: $allocationSize")
        Log.i(TAG_VULT, "createAllocation: expirationNanoSeconds: $expirationNanoSeconds")
        Log.i(TAG_VULT, "createAllocation: lockTokens: $lockTokens")

        try {
            storageSDK.createAllocation(
                allocationName,
                dataShards,
                parityShards,
                allocationSize,
                expirationNanoSeconds,
                lockTokens
            )
            Log.i(TAG_VULT, "createAllocation: successfully created allocation")
        } catch (e: Exception) {
            Log.e(TAG_VULT, "createAllocation Exception: ", e)
        }
    }

    fun createAllocationWithBlobber() {
        try {
            storageSDK.createAllocationWithBlobbers(
                /* allocation name =*/"test allocation",
                /* datashards =*/0,
                /* parityshards =*/0,
                /* size in kilobytes=*/0,
                /* expiration in nanoseconds=*/0,
                /* lock tokens =*/"",
                /* blobbers =*/"",
                /* blobber id's*/""
            )
        } catch (e: Exception) {
            Log.e(TAG_VULT, "createAllocationWithBlobber Exception: ", e)
        }
    }

    fun uploadFile(filePathURI: String?, fileAttr: String?) {
        storageSDK.getAllocation(storageSDK.allocations.split(' ')[0])
            .uploadFile(
                /* work dir =*/
                "/",
                /* local path =*/
                filePathURI,
                /* remote path =*/
                "/",
                /* file attrs =*/
                fileAttr,
                false,
                statusCallbackMocked
            )
    }

    fun downloadFile(fileName: String, downloadPath: String) {
        storageSDK.getAllocation(storageSDK.allocations.split(' ')[0])
            .downloadFile(
                /* file local download path =*/
                downloadPath,
                /* remote path =*/
                "/$fileName",
                statusCallbackMocked
            )
    }

    /*fun shareFile(fileName: String, shareDuration: Long) {
        storageSDK.getAllocation(storageSDK.allocations.split(' ')[0])
            .share()shareFile(
                *//* file name =*//*
                fileName,
                *//* share duration =*//*
                shareDuration,
                statusCallbackMocked
            )
    }*/
}
