package com.rivaphys.citruschecky.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recipe(
    val title: String,
    val ingredients: String
) : Parcelable