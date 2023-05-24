package org.zus.helloworld.models.vult


import com.google.gson.annotations.SerializedName
import org.zus.helloworld.models.blobber.StatsModel

data class AllocationModelItem(
    @SerializedName("data_shards")
    val dataShards: Int,
    @SerializedName("expiration_date")
    val expirationDate: Long,
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("parity_shards")
    val parityShards: Int,
    @SerializedName("size")
    val size: Long,
    @SerializedName("stats")
    val statsModel: String,
)
