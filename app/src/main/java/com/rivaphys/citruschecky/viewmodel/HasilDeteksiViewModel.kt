package com.rivaphys.citruschecky.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rivaphys.citruschecky.utils.ClassNameFormatter
import com.rivaphys.citruschecky.data.BoundingBox
import com.rivaphys.citruschecky.data.ClassRecipes
import com.rivaphys.citruschecky.data.DetectionResult
import com.rivaphys.citruschecky.repository.RecipeRepository
import com.rivaphys.citruschecky.utils.BitmapUtils

class HasilDeteksiViewModel : ViewModel() {

    companion object {
        private const val TAG = "HasilDeteksiViewModel"
    }

    private val _detectionResults = MutableLiveData<List<DetectionResult>>()
    val detectionResults: LiveData<List<DetectionResult>> = _detectionResults

    private val _processedBitmap = MutableLiveData<Bitmap>()
    val processedBitmap: LiveData<Bitmap> = _processedBitmap

    private val _originalImageBitmap = MutableLiveData<Bitmap?>()
    val originalImageBitmap: MutableLiveData<Bitmap?> = _originalImageBitmap

    private val _recipes = MutableLiveData<List<ClassRecipes>>()
    val recipes: LiveData<List<ClassRecipes>> = _recipes

    fun processDetectionResults(
        originalBitmap: Bitmap,
        boundingBoxes: List<BoundingBox>,
        originalImage: Bitmap? = null,
        imagePath: String? = null,
        imageSource: String = "camera"
    ) {
        Log.d(TAG, "=== DEBUG processDetectionResults ===")
        Log.d(TAG, "Original bitmap: ${originalBitmap.width}x${originalBitmap.height}")
        Log.d(TAG, "Original image: ${originalImage?.width}x${originalImage?.height}")
        Log.d(TAG, "Image path: $imagePath")
        Log.d(TAG, "Image source: $imageSource")
        Log.d(TAG, "Bounding boxes: ${boundingBoxes.size}")

        // Process detection results dengan nama yang sudah diformat
        val results = boundingBoxes.map { box ->
            Log.d(TAG, "Processing detection: ${box.clsName} - ${box.cnf}")
            val formattedClassName = ClassNameFormatter.formatClassName(box.clsName)
            Log.d(TAG, "Formatted class name: $formattedClassName")

            DetectionResult(
                className = formattedClassName, // Gunakan nama yang sudah diformat
                originalClassName = box.clsName, // Simpan nama asli jika diperlukan
                confidence = box.cnf,
                capturedImagePath = if (imageSource == "camera") imagePath else null,
                galleryImagePath = if (imageSource == "gallery") imagePath else null,
                imageSource = imageSource,
                description = ClassNameFormatter.getClassDescription(box.clsName) // Tambahkan deskripsi
            )
        }

        Log.d(TAG, "Detection results created: ${results.size}")
        _detectionResults.value = results

        // Process bitmap with bounding boxes
        val processedBitmap = BitmapUtils.drawBoundingBoxes(originalBitmap, boundingBoxes)
        Log.d(TAG, "Processed bitmap: ${processedBitmap.width}x${processedBitmap.height}")
        _processedBitmap.value = processedBitmap

        // Set original image bitmap if available
        if (originalImage != null) {
            _originalImageBitmap.value = originalImage
            Log.d(TAG, "Original image bitmap set")
        }

        // Get recipes based on detected classes (gunakan nama asli untuk pencarian)
        val detectedClasses = boundingBoxes.map { it.clsName } // Gunakan nama asli dari model
        Log.d(TAG, "Detected classes (original): $detectedClasses")

        val recipes = RecipeRepository.getAllRecipesByDetections(detectedClasses)
        Log.d(TAG, "Recipes found: ${recipes.size}")

        _recipes.value = recipes
    }
}