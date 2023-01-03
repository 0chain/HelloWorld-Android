package org.zus.bolt.helloworld.ui.mainactivity

import androidx.lifecycle.ViewModel
import org.zus.bolt.helloworld.models.TransactionModel
import org.zus.bolt.helloworld.models.WalletModel

class MainViewModel : ViewModel() {
    lateinit var wallet: WalletModel
    var transactionList: MutableList<TransactionModel> = mutableListOf()
}
