package org.zus.bolt.helloworld.models

import com.google.gson.annotations.SerializedName

data class WalletModel(
    @SerializedName("client_id") val mClientId: String,
    @SerializedName("client_key") val mClientKey: String,
    @SerializedName("mnemonics") val mMnemonics: String,
    @SerializedName("version") val mVersion: String,
    @SerializedName("date_created") val mDateCreated: String,
    @SerializedName("keys") val mKeys: List<KeyModel>
)
