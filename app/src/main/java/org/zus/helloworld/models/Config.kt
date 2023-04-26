package org.zus.helloworld.models


import com.google.gson.annotations.SerializedName

data class Config(
    @SerializedName("block_worker")
    val blockWorker: String,
    @SerializedName("confirmation_chain_length")
    val confirmationChainLength: Int,
    @SerializedName("signature_scheme")
    val signatureScheme: String
)
