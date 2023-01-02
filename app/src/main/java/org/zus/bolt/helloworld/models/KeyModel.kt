package org.zus.bolt.helloworld.models;

import com.google.gson.annotations.SerializedName;

public class KeyModel {
    @SerializedName("public_key")
    private String mPublicKey;
    @SerializedName("private_key")
    private String mPrivateKey;

    public KeyModel(String mPublicKey, String mPrivateKey) {
        this.mPublicKey = mPublicKey;
        this.mPrivateKey = mPrivateKey;
    }

    public String getmPublicKey() {
        return mPublicKey;
    }

    public String getmPrivateKey() {
        return mPrivateKey;
    }
}
