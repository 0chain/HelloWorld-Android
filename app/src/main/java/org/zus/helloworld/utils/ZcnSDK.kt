package org.zus.helloworld.utils

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.zus.helloworld.ui.bolt.TAG_BOLT
import zcn.Zcn
import zcncore.RequestTimeout
import zcncore.Zcncore
import java.util.Arrays


class ZcnSDK {
    companion object {
        fun faucet(methodName: String, input: Any, zcnToken: Double): String {
            return Zcn.faucet(methodName, Gson().toJson(input), zcnToken)
        }

        fun executeSmartContract(
            address: String,
            methodName: String,
            input: Any,
            sasToken: UInt
        ): String {
            return Zcn.executeSmartContract(
                address,
                methodName,
                Gson().toJson(input),
                sasToken.toString()
            )
        }

        fun readPoolLock(tokens: Double, fee: Double): String {
            return Zcn.readPoolLock(Zcncore.convertToValue(tokens), Zcncore.convertToValue(fee))
        }

        fun writePoolLock(allocID: String, tokens: Double, fee: Double): String {
            return Zcn.writePoolLock(
                allocID,
                Zcncore.convertToValue(tokens),
                Zcncore.convertToValue(fee)
            )
        }

        suspend fun getWalletBalance(clientId: String): Double {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    if (clientId.isBlank()) {
                        throw Exception("clientId is blank")
                    }
                    val zcnLong = Zcncore.getWalletBalance(clientId)
                    Zcncore.convertToToken(zcnLong)
                } catch (e: Exception) {
                    Log.e("ZcnSDK", "getWalletBalance unable to get wallet balance: ${e.message}")
                    0.0
                }
            }
        }

        public suspend fun estimateTransactionFee(zcnValue: String): String {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    val feesList = Zcncore.getFeeStats(object : RequestTimeout {
                        override fun get(): Long {
                            return 0
                        }

                        override fun set(p0: Long) {
                            Log.i(TAG_BOLT, "Fees Stats ==> $p0")
                        }
                    })
                    Arrays.sort(feesList)
                    val fees = feesList[feesList.size / 2]
                    val transactionFeeLong: Long =
                        (zcnToTokenLongFormat(zcnValue.toDouble()) * 0.1 + fees).toLong()
//                val fee = tokenLongToZcnFormat(transactionFeeLong).toString()
                    transactionFeeLong.toString()
                } catch (e: Exception) {
                    Log.e(TAG_BOLT, "estimateTransactionFee: $e")
                    "0"
                }
            }
        }

        /**
         *  Gets the nonce for any transaction.
         *  Nonce is a unique or randomly generated number that is used only once for a transaction.
         */
        public suspend fun getNonce(): Long {
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

        /**
         *   Converts the zcn value to usd.
         *   zcn in double format is like 1.0 ZCN
         */
        public suspend fun zcnToUsd(zcn: Double): Double {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    Zcncore.convertTokenToUSD(zcn)
                } catch (e: Exception) {
                    Log.e(TAG_BOLT, "zcnToUsd: $e")
                    0.0
                }
            }
        }

        /**
         *  Converts the zcn token value in long format to usd.
         *  zcn in long format is like 1000000000000000
         *
         *  In order to represent the smallest values and transactions possible in the network,
         *  zcn is represented in long format at the base level.
         */
        public suspend fun tokenToUsd(token: Long): Double {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    Zcncore.convertTokenToUSD(Zcncore.convertToToken(token))
                } catch (e: Exception) {
                    0.0
                }
            }
        }

        /**
         *  Converts the zcn token value in long format to double format.
         *  zcn in long format is like 1000000000000000
         *
         *  In order to represent the smallest values and transactions possible in the network,
         *  zcn is represented in long format at the base level.
         */
        public suspend fun tokenLongToZcnFormat(token: Long): Double {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    Zcncore.convertToToken(token)
                } catch (e: Exception) {
                    Log.e(TAG_BOLT, "tokenLongToZcnFormat: $e")
                    0.0
                }
            }
        }

        /**
         *  Converts the zcn token value in double format to long format.
         *  zcn in double format is like 1.0 ZCN
         */
        public suspend fun zcnToTokenLongFormat(zcn: Double): Long {
            return withContext(Dispatchers.IO) {
                return@withContext try {
                    Zcncore.convertToValue(zcn).toLong()
                } catch (e: Exception) {
                    Log.e(TAG_BOLT, "zcnToTokenLongFormat: $e")
                    0L
                }
            }
        }
    }
}
