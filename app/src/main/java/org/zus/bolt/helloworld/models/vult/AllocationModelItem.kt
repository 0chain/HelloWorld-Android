package org.zus.bolt.helloworld.models.vult


import com.google.gson.annotations.SerializedName

data class AllocationModelItem(
    @SerializedName("data_shards")
    val dataShards: Int,
    @SerializedName("expiration_date")
    val expirationDate: Int,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("parity_shards")
    val parityShards: Int,
    @SerializedName("size")
    val size: Long,
    @SerializedName("stats")
    val stats: String
)
