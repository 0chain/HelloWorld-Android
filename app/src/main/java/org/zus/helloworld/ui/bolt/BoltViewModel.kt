package org.zus.helloworld.ui.bolt

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.zus.helloworld.models.bolt.BalanceModel
import org.zus.helloworld.models.bolt.TransactionModel
import org.zus.helloworld.utils.Utils.Companion.mergeListsWithoutDuplicates
import org.zus.helloworld.utils.ZcnSDK
import zcncore.GetInfoCallback
import zcncore.Transaction
import zcncore.TransactionCallback
import zcncore.Zcncore
import java.util.*

class BoltViewModel : ViewModel() {
    val transactionsLiveData: MutableLiveData<List<TransactionModel>> = MutableLiveData()
    var balanceLiveData = MutableLiveData<String>()
    val isRefreshLiveData = MutableLiveData<Boolean>()
    val snackbarMessageLiveData: MutableLiveData<String> = MutableLiveData()

    private val getInfoCallback = GetInfoCallback { p0, p1, p2, p3 ->
        isRefreshLiveData.postValue(false)
        Log.i(TAG_BOLT, "onInfoAvailable: ")
        Log.i(TAG_BOLT, "onInfoAvailable: p0 $p0")
        Log.i(TAG_BOLT, "onInfoAvailable: p1 $p1")
        Log.i(TAG_BOLT, "onInfoAvailable: p2 $p2")
        Log.i(TAG_BOLT, "onInfoAvailable: p3 $p3")
    }

    /* Use this callback while making a transaction. */
    private val transactionCallback = object : TransactionCallback {
        override fun onAuthComplete(p0: Transaction?, p1: Long) {
            // confirmation of successful authentication of the transaction.
        }

        override fun onTransactionComplete(transaction: Transaction?, status: Long) {
            // confirmation of successful transaction.
            isRefreshLiveData.postValue(false)
            viewModelScope.launch {
                delay(1500)
                getWalletBalance()
            }
            if (status == 0L) {
                // Successful status of the transaction.
                snackbarMessageLiveData.postValue("Transaction Successful")
            } else {
                // Error status of the transaction.
                snackbarMessageLiveData.postValue("Transaction Failed")
            }
        }

        override fun onVerifyComplete(p0: Transaction?, p1: Long) {
            // confirmation of successful verification of the transaction.
            isRefreshLiveData.postValue(false)
            snackbarMessageLiveData.postValue("Transaction Verified Successfully")
        }
    }

    /**
     *  Submits a transaction to the network.
     *  @param to The address of the receiver.
     *  @param amount The amount of ZCN to send.(in base zcn long format)
     */
    suspend fun sendTransaction(to: String, amount: String) {
        withContext(Dispatchers.IO) {
            try {
                isRefreshLiveData.postValue(true)
                val transactionFee = ZcnSDK.estimateTransactionFee()
                Log.i(TAG_BOLT, "TransactionFees ==> $transactionFee")

                Zcncore.newTransaction(
                    transactionCallback,
                    /* gas = */ transactionFee,
                    /* nonce = */ZcnSDK.getNonce()
                )
                    .send(
                        /* receiver address = */ to,
                        /* amount = */ Zcncore.convertToValue(amount.toDouble()).toString(),
                        /* notes = */ "Hello world! sending tokens."
                    )
            } catch (e: Exception) {
                isRefreshLiveData.postValue(false)
                print("Error: $e")
            }
        }
    }

    /**
     *  Submits a faucet txn to receive 1 ZCN from the network.
     */
    suspend fun receiveFaucet() {
        withContext(Dispatchers.IO) {
            isRefreshLiveData.postValue(true)
            Zcncore.newTransaction(
                transactionCallback,
                /* gas = */ "0",
                /* nonce = */ZcnSDK.getNonce()
            )
                .executeSmartContract(
                    /* faucet address = */ "6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712d3",
                    /* method name = */ "pour",
                    /* inputs = */ "{}",
                    /* amount = */ Zcncore.convertToValue(1.0)
                )
        }
    }

    /**
     *  Gets the balance of the wallet.
     */
    suspend fun getWalletBalance() {
        return withContext(Dispatchers.IO) {
            try {
                isRefreshLiveData.postValue(true)
                Zcncore.getBalance { status, value, info ->
                    isRefreshLiveData.postValue(false)
                    if (status == 0L) {
                        Gson().fromJson(info, BalanceModel::class.java).let { balanceModel ->
                            balanceLiveData.postValue(
                                Zcncore.convertToToken(balanceModel.balance).toString()
                            )
                        }
                    } else {
                        print("Error: $info")
                        balanceLiveData.postValue("")
                    }
                }
            } catch (e: Exception) {
                isRefreshLiveData.postValue(false)
                print("Error: $e")
                receiveFaucet()
                balanceLiveData.postValue("")
            }
        }
    }

    /**
     *  Gets the transactions of the wallet. Use this to get all the transactions sent from the wallet and sent to the wallet.
     *
     *  for receiving all the transactions made to the wallet, use the toClientId parameter.
     *  for receiving all the transactions made from the wallet, use the fromClientId parameter.
     *
     *  @param toClientId The address of the receiver.(if using this parameter, leave the fromClientId parameter empty "")
     *  @param fromClientId The address of the sender. (vice-versa)
     *  @param sortOrder The order of the transactions. (asc or desc)
     *  @param limit The limit of the transactions. (max 20)
     *  @param offset The offset of the transactions. (to get new or old transactions, use the offset of the last transaction)
     */
    suspend fun getTransactions(
        toClientId: String,
        fromClientId: String,
        sortOrder: String,
        limit: Long,
        offset: Long,
    ) {
        withContext(Dispatchers.IO) {
            isRefreshLiveData.postValue(true)
            Zcncore.getTransactions(
                toClientId,
                fromClientId,
                /*block hash optional =*/"",
                sortOrder,
                limit,
                offset
            ) { _, _, json, error ->
                if (error.isEmpty() && !json.isNullOrBlank() && json.isNotEmpty()) {
                    val transactions = Gson().fromJson(json, Array<TransactionModel>::class.java)
                        ?: return@getTransactions
                    this@BoltViewModel.transactionsLiveData.postValue(
                        transactions.toList().mergeListsWithoutDuplicates(
                            this@BoltViewModel.transactionsLiveData.value ?: listOf()
                        )
                    )
                    isRefreshLiveData.postValue(false)
                } else {
                    Log.e(TAG_BOLT, "getTransactions: $error")
                    isRefreshLiveData.postValue(false)
                }
            }
        }
    }
}
