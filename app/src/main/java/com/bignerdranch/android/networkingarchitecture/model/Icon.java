package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

public class Icon {
    @SerializedName("prefix")private String mPrefix;
    @SerializedName("suffix")private String mSuffix;

    public String getPrefix() {
        return mPrefix;
    }

    public String getSuffix() {
        return mSuffix;
    }
}
