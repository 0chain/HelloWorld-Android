package org.zus.helloworld.models.vult


import com.google.gson.annotations.SerializedName

data class FileResponseModel(
    @SerializedName("actual_num_blocks")
    val actualNumBlocks: Int,
    @SerializedName("actual_size")
    val actualSize: Int,
    @SerializedName("created_at")
    val createdAt: Int,
    @SerializedName("encryption_key")
    val encryptionKey: String,
    @SerializedName("list")
    val list: List<FileModel>,
    @SerializedName("lookup_hash")
    val lookupHash: String,
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
