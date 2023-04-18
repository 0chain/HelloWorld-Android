package org.zus.bolt.helloworld.models.selectapp

import android.os.Parcel
import android.os.Parcelable

data class DetailsModel(
    val title: String,
    val value: String,
    val showArrowButton: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(value)
        dest.writeByte(if (showArrowButton) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<DetailsModel> {
        override fun createFromParcel(parcel: Parcel): DetailsModel {
            return DetailsModel(parcel)
        }

        override fun newArray(size: Int): Array<DetailsModel?> {
            return arrayOfNulls(size)
        }
    }
}

data class DetailsListModel(
    val title: String,
    val detailsList: List<DetailsModel>
)
