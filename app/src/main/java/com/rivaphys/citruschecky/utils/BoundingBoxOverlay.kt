package com.rivaphys.citruschecky.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.rivaphys.citruschecky.data.BoundingBox

class BoundingBoxOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()
    private var bounds = Rect()

    // Improved scaling parameters
    private var scaleX = 1f
    private var scaleY = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    init {
        initPaints()
    }

    fun clear() {
        Log.d("BoundingBoxOverlay", "Clearing overlay")
        results = emptyList()
        invalidate()
    }

    fun clearDetections() {
        Log.d("BoundingBoxOverlay", "Clearing all detections")
        results = emptyList()
        invalidate()
    }

    private fun initPaints() {
        textBackgroundPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
            textSize = 40f
            alpha = 200
        }

        textPaint.apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textSize = 40f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        boxPaint.apply {
            strokeWidth = 6f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
    }

    fun setScaleParameters(scaleX: Float, scaleY: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
        Log.d("BoundingBoxOverlay", "Setting scale parameters: scaleX=$scaleX, scaleY=$scaleY, offsetX=$offsetX, offsetY=$offsetY")
        this.scaleX = scaleX
        this.scaleY = scaleY
        this.offsetX = offsetX
        this.offsetY = offsetY
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (results.isEmpty()) {
            return
        }

        Log.d("BoundingBoxOverlay", "Drawing ${results.size} bounding boxes on ${width}x${height} canvas")

        results.forEach { boundingBox ->
            drawBoundingBox(canvas, boundingBox)
        }
    }

    private fun drawBoundingBox(canvas: Canvas, boundingBox: BoundingBox) {
        // Coordinates are already normalized (0-1) from detector
        val normalizedX1 = boundingBox.x1.coerceIn(0f, 1f)
        val normalizedY1 = boundingBox.y1.coerceIn(0f, 1f)
        val normalizedX2 = boundingBox.x2.coerceIn(0f, 1f)
        val normalizedY2 = boundingBox.y2.coerceIn(0f, 1f)

        // Convert directly to canvas pixel coordinates
        // No additional scaling needed since coordinates are normalized
        var left = normalizedX1 * width
        var top = normalizedY1 * height
        var right = normalizedX2 * width
        var bottom = normalizedY2 * height

        // Ensure proper ordering (left < right, top < bottom)
        if (left > right) {
            val temp = left
            left = right
            right = temp
        }
        if (top > bottom) {
            val temp = top
            top = bottom
            bottom = temp
        }

        // Clamp to view bounds
        left = left.coerceIn(0f, width.toFloat())
        top = top.coerceIn(0f, height.toFloat())
        right = right.coerceIn(0f, width.toFloat())
        bottom = bottom.coerceIn(0f, height.toFloat())

        // Skip if box is too small after conversion
        val boxWidth = right - left
        val boxHeight = bottom - top
        if (boxWidth < 20f || boxHeight < 20f) {
            Log.d("BoundingBoxOverlay", "Skipping too small box after conversion: ${boxWidth}x${boxHeight}")
            return
        }

        // Skip if box is suspiciously large (>80% of screen)
        val screenArea = width * height
        val boxArea = boxWidth * boxHeight
        val areaRatio = boxArea / screenArea

        if (areaRatio > 0.8f) {
            Log.d("BoundingBoxOverlay", "Skipping too large box: area ratio = $areaRatio")
            return
        }

        boxPaint.color = getClassColor(boundingBox.clsName)

        val cornerRadius = 12f
        canvas.drawRoundRect(
            left, top, right, bottom,
            cornerRadius, cornerRadius, boxPaint
        )

        // Draw center point for reference
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        val centerPaint = Paint().apply {
            color = boxPaint.color
            style = Paint.Style.FILL
            alpha = 150
        }
        canvas.drawCircle(centerX, centerY, 6f, centerPaint)

        // Prepare and draw text
        drawBoundingBoxText(canvas, boundingBox, left, top, right, bottom)

        // Debug logging with more reasonable output
        Log.d("BoundingBoxOverlay", "${boundingBox.clsName}: " +
                "normalized(${normalizedX1.format(3)}, ${normalizedY1.format(3)}, ${normalizedX2.format(3)}, ${normalizedY2.format(3)}) -> " +
                "pixel(${left.toInt()}, ${top.toInt()}, ${right.toInt()}, ${bottom.toInt()}) " +
                "size: ${boxWidth.toInt()}x${boxHeight.toInt()} (${(areaRatio * 100).toInt()}% screen)")
    }

    // Extension function for formatting floats
    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

    private fun drawBoundingBoxText(
        canvas: Canvas,
        boundingBox: BoundingBox,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        val confidence = (boundingBox.cnf * 100).toInt()
        val formattedClassName = formatClassName(boundingBox.clsName)
        val drawableText = "$formattedClassName $confidence%"

        textPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
        val textWidth = bounds.width().toFloat()
        val textHeight = bounds.height().toFloat()

        val textPadding = 8f
        val backgroundPadding = 6f

        val textLeft = left.coerceAtMost(width - textWidth - backgroundPadding * 2)
        val textTop = if (top > textHeight + backgroundPadding * 2) {
            // Space above - draw above the box
            top - backgroundPadding
        } else {
            // No space above - draw inside the box at top
            top + textHeight + backgroundPadding
        }

        val backgroundRect = RectF(
            textLeft - backgroundPadding,
            textTop - textHeight - backgroundPadding,
            textLeft + textWidth + backgroundPadding,
            textTop + backgroundPadding
        )

        canvas.drawRoundRect(backgroundRect, 4f, 4f, textBackgroundPaint)

        // Draw text
        canvas.drawText(drawableText, textLeft, textTop - backgroundPadding/2, textPaint)
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        Log.d("BoundingBoxOverlay", "Setting ${boundingBoxes.size} detection results")

        // Validate and log bounding boxes
        val validBoxes = boundingBoxes.filter { box ->
            val isValid = box.x1 >= 0f && box.y1 >= 0f &&
                    box.x2 <= 1f && box.y2 <= 1f &&
                    box.x1 < box.x2 && box.y1 < box.y2

            if (!isValid) {
                Log.w("BoundingBoxOverlay", "Invalid bounding box filtered out: " +
                        "(${box.x1}, ${box.y1}, ${box.x2}, ${box.y2}) - ${box.clsName}")
            }

            isValid
        }

        results = validBoxes
        Log.d("BoundingBoxOverlay", "Valid boxes: ${validBoxes.size}/${boundingBoxes.size}")
        invalidate()
    }

    private fun getClassColor(className: String): Int {
        return when (className.lowercase()) {
            "matang", "ripe" -> Color.rgb(34, 139, 34)
            "sangat-busuk", "very-rotten" -> Color.rgb(220, 20, 60)
            "sedikit-busuk", "slightly-rotten" -> Color.rgb(255, 140, 0)
            else -> Color.rgb(0, 191, 255)
        }
    }

    private fun formatClassName(className: String): String {
        return when (className.lowercase()) {
            "matang" -> "Matang"
            "sangat-busuk" -> "Sangat Busuk"
            "sedikit-busuk" -> "Sedikit Busuk"
            "ripe" -> "Matang"
            "very-rotten" -> "Sangat Busuk"
            "slightly-rotten" -> "Sedikit Busuk"
            else -> className.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
    }
}