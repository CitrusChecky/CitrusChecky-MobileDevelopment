package com.rivaphys.citruschecky.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.Log
import com.rivaphys.citruschecky.data.BoundingBox
import kotlin.math.sqrt

object BitmapUtils {
    private const val TAG = "BitmapUtils"

    fun drawBoundingBoxes(
        bitmap: Bitmap,
        boundingBoxes: List<BoundingBox>,
        imageSource: String = "unknown"
    ): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val dynamicTextSize = calculateDynamicTextSize(bitmap, imageSource)
        val dynamicStrokeWidth = calculateDynamicStrokeWidth(bitmap, imageSource)

        Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")
        Log.d(TAG, "Dynamic text size: $dynamicTextSize")
        Log.d(TAG, "Dynamic stroke width: $dynamicStrokeWidth")

        val boxPaint = Paint().apply {
            strokeWidth = dynamicStrokeWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val textBackgroundPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            alpha = 200 // Semi-transparent
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = dynamicTextSize
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        boundingBoxes.forEach { box ->
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            // Set color based on class
            boxPaint.color = ClassNameFormatter.getClassColor(box.clsName)

            // Draw bounding box dengan corner radius
            val cornerRadius = dynamicStrokeWidth * 2
            canvas.drawRoundRect(
                left, top, right, bottom,
                cornerRadius, cornerRadius, boxPaint
            )

            // Prepare text dengan nama yang sudah diformat
            val confidence = (box.cnf * 100).toInt()
            val formattedClassName = ClassNameFormatter.formatClassName(box.clsName)
            val text = "$formattedClassName $confidence%"

            // Calculate text dimensions
            val bounds = Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)
            val textWidth = bounds.width().toFloat()
            val textHeight = bounds.height().toFloat()

            // Calculate padding berdasarkan ukuran teks
            val textPadding = dynamicTextSize * 0.2f
            val backgroundPadding = dynamicTextSize * 0.15f

            // Position text - prefer above the box, but inside if no space
            val textLeft = left.coerceAtMost(bitmap.width - textWidth - backgroundPadding * 2)
            val textTop = if (top > textHeight + backgroundPadding * 2) {
                // Space above - draw above the box
                top - backgroundPadding
            } else {
                // No space above - draw inside the box at top
                top + textHeight + backgroundPadding
            }

            // Draw text background with rounded corners
            val backgroundRect = RectF(
                textLeft - backgroundPadding,
                textTop - textHeight - backgroundPadding,
                textLeft + textWidth + backgroundPadding,
                textTop + backgroundPadding
            )

            val backgroundRadius = backgroundPadding * 0.5f
            canvas.drawRoundRect(backgroundRect, backgroundRadius, backgroundRadius, textBackgroundPaint)

            // Draw text
            canvas.drawText(text, textLeft, textTop - backgroundPadding/2, textPaint)

            Log.d(TAG, "Drew detection: $formattedClassName at (${left.toInt()}, ${top.toInt()}) with text size $dynamicTextSize")
        }

        return mutableBitmap
    }

    /**
     * Menghitung ukuran teks berdasarkan ukuran bitmap
     * Menggunakan referensi ukuran standar dan scaling factor
     */
    private fun calculateDynamicTextSize(bitmap: Bitmap, imageSource: String = "unknown"): Float {
        // Referensi ukuran berdasarkan sumber gambar
        val (referenceWidth, referenceTextSize) = when (imageSource) {
            "camera" -> Pair(3000f, 60f) // Gambar kamera biasanya resolusi tinggi
            "gallery" -> Pair(1080f, 40f) // Gambar galeri lebih beragam
            else -> Pair(1080f, 40f) // Default
        }

        val currentDimension = minOf(bitmap.width, bitmap.height).toFloat()
        val scaleFactor = currentDimension / referenceWidth

        // Terapkan scaling dengan batas yang disesuaikan per sumber
        val (minSize, maxSize) = when (imageSource) {
            "camera" -> Pair(30f, 150f)
            "gallery" -> Pair(20f, 100f)
            else -> Pair(24f, 120f)
        }

        val dynamicTextSize = (referenceTextSize * scaleFactor).coerceIn(minSize, maxSize)

        return dynamicTextSize
    }

    private fun calculateDynamicStrokeWidth(bitmap: Bitmap, imageSource: String = "unknown"): Float {
        val (referenceWidth, referenceStrokeWidth) = when (imageSource) {
            "camera" -> Pair(3000f, 12f)
            "gallery" -> Pair(1080f, 8f)
            else -> Pair(1080f, 8f)
        }

        val currentDimension = minOf(bitmap.width, bitmap.height).toFloat()
        val scaleFactor = currentDimension / referenceWidth

        val (minStroke, maxStroke) = when (imageSource) {
            "camera" -> Pair(4f, 25f)
            "gallery" -> Pair(3f, 15f)
            else -> Pair(3f, 20f)
        }

        val dynamicStrokeWidth = (referenceStrokeWidth * scaleFactor).coerceIn(minStroke, maxStroke)

        return dynamicStrokeWidth
    }
}