package org.zus.bolt.helloworld.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class WalletModel {
    @SerializedName("client_id")
    private final String mClientId;
    @SerializedName("client_key")
    private final String mClientKey;
    @SerializedName("mnemonics")
    private final String mMnemonics;
    @SerializedName("version")
    private final String mVersion;
    @SerializedName("date_created")
    private final String mDateCreated;
    @SerializedName("keys")
    private final List<KeyModel> mKeys;

    public WalletModel(String mClientId, String mClientKey, String mMnemonics, String mVersion, String mDateCreated, List<KeyModel> mKeys) {
        this.mClientId = mClientId;
        this.mClientKey = mClientKey;
        this.mMnemonics = mMnemonics;
        this.mVersion = mVersion;
        this.mDateCreated = mDateCreated;
        this.mKeys = mKeys;
    }

    public String getmClientId() {
        return mClientId;
    }

    public String getmClientKey() {
        return mClientKey;
    }

    public String getmMnemonics() {
        return mMnemonics;
    }

    public String getmVersion() {
        return mVersion;
    }

    public String getmDateCreated() {
        return mDateCreated;
    }

    public List<KeyModel> getmKeys() {
        return mKeys;
    }
}
