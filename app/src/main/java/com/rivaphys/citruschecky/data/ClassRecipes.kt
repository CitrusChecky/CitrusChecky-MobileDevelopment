package com.rivaphys.citruschecky.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ClassRecipes(
    val className: String,
    val recipes: List<Recipe>
) : Parcelable