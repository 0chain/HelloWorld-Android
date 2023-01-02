package org.zus.bolt.helloworld.ui.vult

import androidx.lifecycle.ViewModel
import sdk.Sdk
import sdk.StorageSDK

class VultViewModel : ViewModel() {
    private val storageSDK: StorageSDK by lazy {
        StorageSDK()
    }

    fun initZbox() {
        Sdk.initStorageSDK(/*config*/"",/*wallet*/"")
    }

    fun createAllocation() {
//        storageSDK.redeemFreeStorage()
    }

    fun uploadFile() {

    }
}
