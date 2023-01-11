package org.zus.bolt.helloworld.models.vult


import com.google.gson.annotations.SerializedName
import org.zus.bolt.helloworld.models.blobber.BlobberDetail
import org.zus.bolt.helloworld.models.blobber.ReadPriceRange
import org.zus.bolt.helloworld.models.blobber.Stats
import org.zus.bolt.helloworld.models.blobber.WritePriceRange
import zcncore.Blobber

data class CliAllocationsModelItem(
    @SerializedName("blobber_details")
    val blobberDetails: List<BlobberDetail>,
    @SerializedName("blobbers")
    val blobbers: List<Blobber>,
    @SerializedName("challenge_completion_time")
    val challengeCompletionTime: Int,
    @SerializedName("curators")
    val curators: List<Any>,
    @SerializedName("data_shards")
    val dataShards: Int,
    @SerializedName("expiration_date")
    val expirationDate: Int,
    @SerializedName("id")
    val id: String,
    @SerializedName("is_immutable")
    val isImmutable: Boolean,
    @SerializedName("name")
    val name: String,
    @SerializedName("owner_id")
    val ownerId: String,
    @SerializedName("owner_public_key")
    val ownerPublicKey: String,
    @SerializedName("parity_shards")
    val parityShards: Int,
    @SerializedName("payer_id")
    val payerId: String,
    @SerializedName("read_price_range")
    val readPriceRange: ReadPriceRange,
    @SerializedName("size")
    val size: Long,
    @SerializedName("start_time")
    val startTime: Int,
    @SerializedName("stats")
    val stats: Stats,
    @SerializedName("time_unit")
    val timeUnit: Long,
    @SerializedName("tx")
    val tx: String,
    @SerializedName("write_pool")
    val writePool: Long,
    @SerializedName("write_price_range")
    val writePriceRange: WritePriceRange
)
