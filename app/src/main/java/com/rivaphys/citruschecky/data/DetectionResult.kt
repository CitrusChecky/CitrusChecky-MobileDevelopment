package com.rivaphys.citruschecky.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class DetectionResult(
    val className: String,
    val originalClassName: String? = null,
    val confidence: Float,
    val capturedImagePath: String? = null,
    val galleryImagePath: String? = null,
    val imageSource: String = "camera",
    val description: String? = null
)