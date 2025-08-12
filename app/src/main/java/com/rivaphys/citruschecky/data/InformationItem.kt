package com.rivaphys.citruschecky.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InformationItem(
    val title: String,
    val description: String
) : Parcelable