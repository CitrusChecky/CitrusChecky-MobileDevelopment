package com.rivaphys.citruschecky.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.rivaphys.citruschecky.data.BoundingBox
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.max
import kotlin.math.min

class Detector(
    private val context: Context,
    private val modelPath: String,
    private val labelPath: String,
    private val detectorListener: DetectorListener
) {

    private var interpreter: Interpreter? = null
    private var labels = mutableListOf<String>()

    private var tensorWidth = 0
    private var tensorHeight = 0
    private var numChannel = 0
    private var numElements = 0

    private var originalWidth = 0
    private var originalHeight = 0

    companion object {
        private const val INPUT_MEAN = 0f
        private const val INPUT_STANDARD_DEVIATION = 255f
        private val INPUT_IMAGE_TYPE = DataType.FLOAT32
        private val OUTPUT_IMAGE_TYPE = DataType.FLOAT32

        private const val CONFIDENCE_THRESHOLD = 0.25F
        private const val IOU_THRESHOLD = 0.4F

        private const val MIN_BOX_SIZE = 0.02F
        private const val MAX_BOX_SIZE = 0.7F

        private const val MIN_ASPECT_RATIO = 0.4F
        private const val MAX_ASPECT_RATIO = 2.5F

        private const val MIN_AREA = 0.0005F
        private const val MAX_AREA = 0.4F

        private const val TAG = "Detector"
    }

    private val imageProcessor = ImageProcessor.Builder()
        .add(NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
        .add(CastOp(INPUT_IMAGE_TYPE))
        .build()

    fun setup() {
        val model = FileUtil.loadMappedFile(context, modelPath)
        val options = Interpreter.Options()
        options.setNumThreads(4)
        interpreter = Interpreter(model, options)

        val inputShape = interpreter?.getInputTensor(0)?.shape() ?: return
        val outputShape = interpreter?.getOutputTensor(0)?.shape() ?: return

        tensorWidth = inputShape[1]
        tensorHeight = inputShape[2]
        numChannel = outputShape[1]
        numElements = outputShape[2]

        Log.d(TAG, "Model setup - Input: ${tensorWidth}x${tensorHeight}, Output channels: $numChannel, Elements: $numElements")

        try {
            val inputStream: InputStream = context.assets.open(labelPath)
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line: String? = reader.readLine()
            while (line != null && line != "") {
                labels.add(line)
                line = reader.readLine()
            }

            reader.close()
            inputStream.close()

            Log.d(TAG, "Loaded ${labels.size} labels: ${labels}")
        } catch (e: IOException) {
            Log.e(TAG, "Error loading labels", e)
        }
    }

    fun clear() {
        interpreter?.close()
        interpreter = null
    }

    fun detect(frame: Bitmap) {
        interpreter ?: return
        if (tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            Log.w(TAG, "Model not properly initialized")
            return
        }

        // Store original dimensions for coordinate conversion
        originalWidth = frame.width
        originalHeight = frame.height

        Log.d(TAG, "Processing frame: ${originalWidth}x${originalHeight} -> ${tensorWidth}x${tensorHeight}")

        var inferenceTime = SystemClock.uptimeMillis()

        // Resize to model input size
        val resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, true)

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(resizedBitmap)
        val processedImage = imageProcessor.process(tensorImage)
        val imageBuffer = processedImage.buffer

        val output = TensorBuffer.createFixedSize(intArrayOf(1, numChannel, numElements), OUTPUT_IMAGE_TYPE)
        interpreter?.run(imageBuffer, output.buffer)

        val bestBoxes = bestBox(output.floatArray)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        if (bestBoxes == null || bestBoxes.isEmpty()) {
            Log.d(TAG, "No detections found")
            detectorListener.onEmptyDetect()
        } else {
            Log.d(TAG, "Found ${bestBoxes.size} detections")
            detectorListener.onDetect(bestBoxes, inferenceTime)
        }
    }

    private fun bestBox(array: FloatArray): List<BoundingBox>? {
        val boundingBoxes = mutableListOf<BoundingBox>()

        Log.d(TAG, "=== PROCESSING ${numElements} POTENTIAL DETECTIONS ===")

        var totalAboveThreshold = 0
        var totalValidated = 0

        for (c in 0 until numElements) {
            var maxConf = -1.0f
            var maxIdx = -1
            var j = 4
            var arrayIdx = c + numElements * j

            // Find class with highest confidence
            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx]
                    maxIdx = j - 4
                }
                j++
                arrayIdx += numElements
            }

            // Debug: Log all detections above a very low threshold
            if (maxConf > 0.1f) {
                val clsName = if (maxIdx < labels.size) labels[maxIdx] else "unknown"
                Log.d(TAG, "Detection candidate: $clsName confidence=${(maxConf * 100).toInt()}%")
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                totalAboveThreshold++
                val clsName = if (maxIdx < labels.size) labels[maxIdx] else "unknown"

                // Get bounding box coordinates (already normalized by model)
                val cx = array[c].coerceIn(0f, 1f)
                val cy = array[c + numElements].coerceIn(0f, 1f)
                val w = array[c + numElements * 2].coerceIn(0f, 1f)
                val h = array[c + numElements * 3].coerceIn(0f, 1f)

                Log.d(TAG, "Above threshold: $clsName (${(maxConf * 100).toInt()}%) - cx=$cx, cy=$cy, w=$w, h=$h")

                // More lenient validation for real-time detection
                if (!isValidDetection(w, h, maxConf, clsName)) {
                    Log.d(TAG, "  -> REJECTED by validation")
                    continue
                }

                totalValidated++

                // Calculate corner coordinates
                val x1 = (cx - (w / 2F)).coerceIn(0f, 1f)
                val y1 = (cy - (h / 2F)).coerceIn(0f, 1f)
                val x2 = (cx + (w / 2F)).coerceIn(0f, 1f)
                val y2 = (cy + (h / 2F)).coerceIn(0f, 1f)

                // Ensure valid box dimensions
                if (x2 <= x1 || y2 <= y1) {
                    Log.d(TAG, "Invalid box dimensions: ($x1, $y1, $x2, $y2)")
                    continue
                }

                val boundingBox = BoundingBox(
                    x1 = x1, y1 = y1, x2 = x2, y2 = y2,
                    cx = cx, cy = cy, w = w, h = h,
                    cnf = maxConf, cls = maxIdx, clsName = clsName
                )

                boundingBoxes.add(boundingBox)
                Log.d(TAG, "  -> ACCEPTED: $clsName (${(maxConf * 100).toInt()}%)")
            }
        }

        Log.d(TAG, "SUMMARY: ${totalAboveThreshold} above threshold, ${totalValidated} validated, ${boundingBoxes.size} final boxes")

        if (boundingBoxes.isEmpty()) {
            Log.d(TAG, "No valid boxes - returning null")
            return null
        }

        Log.d(TAG, "Applying NMS to ${boundingBoxes.size} boxes")
        val nmsResult = applyNMS(boundingBoxes)
        Log.d(TAG, "After NMS: ${nmsResult.size} boxes remain")

        return nmsResult
    }

    private fun isValidDetection(w: Float, h: Float, confidence: Float, className: String): Boolean {
        // Strict size validation - reject huge boxes
        if (w < MIN_BOX_SIZE || h < MIN_BOX_SIZE) {
            Log.d(TAG, "Box too small: ${w}x${h}")
            return false
        }

        if (w > MAX_BOX_SIZE || h > MAX_BOX_SIZE) {
            Log.d(TAG, "Box too large: ${w}x${h} - REJECTED")
            return false
        }

        // Strict area validation - reject giant areas
        val area = w * h
        if (area < MIN_AREA || area > MAX_AREA) {
            Log.d(TAG, "Invalid area: $area - REJECTED")
            return false
        }

        // Reasonable aspect ratio check
        val aspectRatio = w / h
        if (aspectRatio < MIN_ASPECT_RATIO || aspectRatio > MAX_ASPECT_RATIO) {
            val inverseRatio = h / w
            if (inverseRatio < MIN_ASPECT_RATIO || inverseRatio > MAX_ASPECT_RATIO) {
                Log.d(TAG, "Invalid aspect ratio: $aspectRatio - REJECTED")
                return false
            }
        }

        // Additional validation for orange objects
        if (className.lowercase().contains("jeruk") ||
            className.lowercase().contains("orange") ||
            className.lowercase().contains("matang") ||
            className.lowercase().contains("busuk")) {

            // Reject extremely elongated shapes
            val roundness = minOf(w, h) / maxOf(w, h)
            if (roundness < 0.4f) {
                Log.d(TAG, "Orange too elongated: roundness=$roundness - REJECTED")
                return false
            }
        }

        Log.d(TAG, "Detection ACCEPTED: $className (${(confidence * 100).toInt()}%) - ${w}x${h}")
        return true
    }

    private fun applyNMS(boxes: List<BoundingBox>): MutableList<BoundingBox> {
        val sortedBoxes = boxes.sortedByDescending { it.cnf }.toMutableList()
        val selectedBoxes = mutableListOf<BoundingBox>()

        Log.d(TAG, "NMS input: ${sortedBoxes.size} boxes")

        while (sortedBoxes.isNotEmpty()) {
            val first = sortedBoxes.removeAt(0)
            selectedBoxes.add(first)

            Log.d(TAG, "Selected box: ${first.clsName} (${(first.cnf * 100).toInt()}%)")

            val iterator = sortedBoxes.iterator()
            while (iterator.hasNext()) {
                val nextBox = iterator.next()
                val iou = calculateIoU(first, nextBox)

                if (iou >= IOU_THRESHOLD) {
                    Log.d(TAG, "Removing overlapping box: ${nextBox.clsName} (IoU: $iou)")
                    iterator.remove()
                }
            }
        }

        Log.d(TAG, "NMS output: ${selectedBoxes.size} boxes")
        return selectedBoxes
    }

    private fun calculateIoU(box1: BoundingBox, box2: BoundingBox): Float {
        val x1 = maxOf(box1.x1, box2.x1)
        val y1 = maxOf(box1.y1, box2.y1)
        val x2 = minOf(box1.x2, box2.x2)
        val y2 = minOf(box1.y2, box2.y2)

        val intersectionArea = maxOf(0F, x2 - x1) * maxOf(0F, y2 - y1)
        val box1Area = box1.w * box1.h
        val box2Area = box2.w * box2.h
        val unionArea = box1Area + box2Area - intersectionArea

        return if (unionArea > 0) intersectionArea / unionArea else 0f
    }

    interface DetectorListener {
        fun onEmptyDetect()
        fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long)
    }
}