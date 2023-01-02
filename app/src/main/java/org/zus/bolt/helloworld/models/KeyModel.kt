package org.zus.bolt.helloworld.models

import com.google.gson.annotations.SerializedName

data class KeyModel(
    @SerializedName("public_key") val mPublicKey: String,
    @SerializedName("private_key") val mPrivateKey: String
)
