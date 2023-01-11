package org.zus.bolt.helloworld.models


import com.google.gson.annotations.SerializedName

data class NetworkModel(
    @SerializedName("block_worker")
    val blockWorker: String,
    @SerializedName("config")
    val config: Config,
    @SerializedName("data_shards")
    val dataShards: Int,
    @SerializedName("domain_url")
    val domainUrl: String,
    @SerializedName("explorer_url")
    val explorerUrl: String,
    @SerializedName("network_fee_url")
    val networkFeeUrl: String,
    @SerializedName("parity_shards")
    val parityShards: Int,
    @SerializedName("zbox_url")
    val zboxUrl: String
)
