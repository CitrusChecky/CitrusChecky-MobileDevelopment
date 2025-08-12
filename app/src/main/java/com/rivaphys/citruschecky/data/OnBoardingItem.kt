package com.rivaphys.citruschecky.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnBoardingItem(
    val title: String,
    val description: String,
    val image: Int,
    val backgroundColor: Int
) : Parcelable
