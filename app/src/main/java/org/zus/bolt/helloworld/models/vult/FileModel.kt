package org.zus.bolt.helloworld.models.vult


import com.google.gson.annotations.SerializedName

data class FileModel(
    @SerializedName("actual_num_blocks")
    val actualNumBlocks: Int,
    @SerializedName("actual_size")
    val actualSize: Int,
    @SerializedName("created_at")
    val createdAt: Int,
    @SerializedName("encryption_key")
    val encryptionKey: String,
    @SerializedName("hash")
    val hash: String,
    @SerializedName("list")
    val list: Any,
    @SerializedName("lookup_hash")
    val lookupHash: String,
    @SerializedName("mimetype")
    val mimetype: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("num_blocks")
    val numBlocks: Int,
    @SerializedName("path")
    val path: String,
    @SerializedName("size")
    val size: Int,
    @SerializedName("type")
    val type: String,
    @SerializedName("updated_at")
    val updatedAt: Int
)
