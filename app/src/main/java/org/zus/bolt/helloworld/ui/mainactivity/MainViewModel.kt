package org.zus.bolt.helloworld.ui.mainactivity

import androidx.lifecycle.ViewModel
import org.zus.bolt.helloworld.models.bolt.WalletModel

class MainViewModel : ViewModel() {
    var wallet: WalletModel? = null

    fun setWalletJson(walletJson: String) {
        wallet?.walletJson = walletJson
    }
}
