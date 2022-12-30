package org.zus.bolt.helloworld

import androidx.lifecycle.ViewModel
import org.zus.bolt.helloworld.models.WalletModel

class MainViewModel : ViewModel() {
    lateinit var wallet: WalletModel
}
