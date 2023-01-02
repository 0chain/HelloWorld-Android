package org.zus.bolt.helloworld.models

import com.google.gson.annotations.SerializedName

data class BalanceModel(
    @SerializedName("txn") val transactionHash: String,
    val balance: Long
)
