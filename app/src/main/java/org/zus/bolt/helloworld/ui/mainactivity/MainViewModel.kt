package org.zus.bolt.helloworld.ui.mainactivity

import androidx.lifecycle.ViewModel
import org.zus.bolt.helloworld.models.bolt.WalletModel
import sdk.StorageSDK

class MainViewModel : ViewModel() {

    var wallet: WalletModel? = null
    var storageSDK: StorageSDK? = null

    fun setWalletJson(walletJson: String) {
        wallet?.walletJson = walletJson
    }
}
