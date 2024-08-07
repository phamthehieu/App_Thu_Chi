package com.example.myapplication.data

import android.os.Parcel
import android.os.Parcelable

data class AccountIconFormat(
    val id: Int,
    val nameAccount: String,
    val typeAccount: Int,
    val amountAccount: String,
    val icon: Int,
    val note: String,
    val iconResource: Int,
    val typeIcon: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: ""

    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(nameAccount)
        parcel.writeInt(typeAccount)
        parcel.writeString(amountAccount)
        parcel.writeInt(icon)
        parcel.writeString(note)
        parcel.writeInt(iconResource)
        parcel.writeString(typeIcon)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AccountIconFormat> {
        override fun createFromParcel(parcel: Parcel): AccountIconFormat {
            return AccountIconFormat(parcel)
        }

        override fun newArray(size: Int): Array<AccountIconFormat?> {
            return arrayOfNulls(size)
        }
    }
}
