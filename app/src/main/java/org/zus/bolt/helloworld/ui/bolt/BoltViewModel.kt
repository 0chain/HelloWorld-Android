package org.zus.bolt.helloworld.ui.bolt

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zus.bolt.helloworld.models.bolt.BalanceModel
import org.zus.bolt.helloworld.models.bolt.TransactionModel
import zcncore.GetInfoCallback
import zcncore.Transaction
import zcncore.TransactionCallback
import zcncore.Zcncore

class BoltViewModel : ViewModel() {
    val transactionsLiveData: MutableLiveData<List<TransactionModel>> = MutableLiveData()
    var balanceLiveData = MutableLiveData<String>()

    companion object {
        fun initZcncore() {
            /* initialize the sdk the with chain config. stored in config.json */
            Zcncore.init(
                """
                {
                    "chain_id": "0afc093ffb509f059c55478bc1a60351cef7b4e9c008a53a6cc8241ca8617dfe",
                    "signature_scheme": "bls0chain",
                    "block_worker": "https://demo.0chain.net/dns",
                    "min_submit": 50,
                    "min_confirmation": 50,
                    "confirmation_chain_length": 3,
                    "num_keys": 1,
                    "eth_node": "https://ropsten.infura.io/v3/f0a254d8d18b4749bd8540da63b3292b"
                }
            """.trimIndent()
            )
        }
    }

    private val getInfoCallback = GetInfoCallback { p0, p1, p2, p3 ->
        Log.i(TAG_BOLT, "onInfoAvailable: ")
        Log.i(TAG_BOLT, "onInfoAvailable: p0 $p0")
        Log.i(TAG_BOLT, "onInfoAvailable: p1 $p1")
        Log.i(TAG_BOLT, "onInfoAvailable: p2 $p2")
        Log.i(TAG_BOLT, "onInfoAvailable: p3 $p3")
    }

    suspend fun sendTransaction(to: String, amount: String) {
        withContext(Dispatchers.IO) {
            Zcncore.newTransaction(transactionCallback, /* gas = */ "0", /* nonce = */ getNonce())
                .send(
                    /* receiver address = */ to,
                    /* amount = */ Zcncore.convertToValue(amount.toDouble()).toString(),
                    /* notes = */ "Hello world! sending tokens."
                )
        }
    }

    suspend fun receiveFaucet() {
        withContext(Dispatchers.IO) {
            Zcncore.newTransaction(transactionCallback, /* gas = */ "0",/* nonce = */getNonce())
                .executeSmartContract(
                    /* faucet address = */ "6dba10422e368813802877a85039d3985d96760ed844092319743fb3a76712d3",
                    /* method name = */ "pour",
                    /* inputs = */ "{}",
                    /* amount = */ Zcncore.convertToValue(1.0)
                )
        }
    }

    /* Use this callback while making a transaction. */
    private val transactionCallback = object : TransactionCallback {
        override fun onAuthComplete(p0: Transaction?, p1: Long) {
            // confirmation of successful authentication of the transaction.
        }

        override fun onTransactionComplete(transaction: Transaction?, status: Long) {
            // confirmation of successful transaction.
            if (status == 0L) {
                // Successful status of the transaction.
            }
        }

        override fun onVerifyComplete(p0: Transaction?, p1: Long) {
            // confirmation of successful verification of the transaction.
        }
    }

    suspend fun getWalletBalance() {
        return withContext(Dispatchers.IO) {
            try {
                Zcncore.getBalance { status, value, info ->
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
                print("Error: $e")
                balanceLiveData.postValue("")
            }
        }
    }

    suspend fun getTransactions(
        toClientId: String,
        fromClientId: String,
        sortOrder: String,
        limit: Long,
        offset: Long,
    ) {
        withContext(Dispatchers.IO) {
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
                    this@BoltViewModel.transactionsLiveData.postValue(transactions.toList())
                } else {
                    Log.e(TAG_BOLT, "getTransactions: $error")
                }
            }
        }
    }

    suspend fun getBlobbers() {
        withContext(Dispatchers.IO) {
            Zcncore.getBlobbers(getInfoCallback, /* limit */ 20, /* offset */ 0, true)
        }
    }

    private suspend fun getNonce(): Long {
        return withContext(Dispatchers.IO) {
            var nonceGlobal: Long = 0L
            Zcncore.getNonce { status, nonce, error ->
                if (status == 0L && error == null) {
                    // nonce is a string
                    // nonce = "0"
                    nonceGlobal = nonce
                }
            }
            return@withContext nonceGlobal
        }
    }
}
