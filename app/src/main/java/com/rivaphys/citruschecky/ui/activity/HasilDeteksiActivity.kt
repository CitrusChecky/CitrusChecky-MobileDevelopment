package com.rivaphys.citruschecky.ui.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.adapter.DetectionResultAdapter
import com.rivaphys.citruschecky.adapter.RecipeAdapter
import com.rivaphys.citruschecky.data.BoundingBox
import com.rivaphys.citruschecky.data.DetectionResult
import com.rivaphys.citruschecky.databinding.ActivityHasilDeteksiBinding
import com.rivaphys.citruschecky.viewmodel.HasilDeteksiViewModel
import java.io.File

class HasilDeteksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHasilDeteksiBinding
    private val viewModel: HasilDeteksiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHasilDeteksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViews()
        setupObservers()
        processIntentData()
    }

    private fun setupViews() {
        Log.d(TAG, "Setting up views...")

        binding.tvBackHasilDeteksi.setOnClickListener {
            Log.d(TAG, "Back button clicked")
            finish()
        }

        binding.rvHasilDeteksi.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "Detection RecyclerView setup complete")

        try {
            val recyclerViewResep = findViewById<androidx.recyclerview.widget.RecyclerView>(
                R.id.rv_resep_hasil_deteksi
            )

            if (recyclerViewResep != null) {
                recyclerViewResep.layoutManager = LinearLayoutManager(this)
                Log.d(TAG, "Recipe RecyclerView setup complete")
            } else {
                Log.e(TAG, "Recipe RecyclerView not found in layout!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recipe RecyclerView", e)
        }

        binding.btnCheckyaiHasilDeteksi.setOnClickListener {
            val detectedClasses = viewModel.detectionResults.value?.map { it.className } ?: emptyList()
            if (detectedClasses.isNotEmpty()) {
                val intent = Intent(this, CheckyAIActivity::class.java)
                intent.putStringArrayListExtra(CheckyAIActivity.EXTRA_DETECTED_CLASSES, ArrayList(detectedClasses))
                startActivity(intent)
            } else {
                Toast.makeText(this, "Tidak ada data deteksi yang tersedia", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        viewModel.detectionResults.observe(this) { results ->
            Log.d(TAG, "Detection results received: ${results.size}")
            results.forEachIndexed { index, result ->
                Log.d(TAG, "Result $index: ${result.className} - ${result.confidence}")
            }
            setupDetectionRecyclerView(results)
        }

        viewModel.processedBitmap.observe(this) { bitmap ->
            Log.d(TAG, "Processed bitmap received: ${bitmap.width}x${bitmap.height}")
            binding.ivHasilDeteksi.setImageBitmap(bitmap)
        }

        viewModel.originalImageBitmap.observe(this) { bitmap ->
            Log.d(TAG, "Captured photo bitmap received: ${bitmap?.width}x${bitmap?.height}")
        }

        viewModel.recipes.observe(this) { recipes ->
            Log.d(TAG, "Recipes received: ${recipes.size}")
            recipes.forEachIndexed { index, recipe ->
                Log.d(TAG, "Recipe $index: ${recipe.className} with ${recipe.recipes.size} recipes")
            }
            setupRecipeRecyclerView(recipes)
        }
    }

    private fun processIntentData() {
        Log.d(TAG, "=== DEBUG processIntentData ===")

        val imagePath = intent.getStringExtra("image_path")
        val capturedPhotoPath = intent.getStringExtra("captured_photo_path")
        val originalImagePath = intent.getStringExtra("original_image_path")
        val imageSource = intent.getStringExtra("image_source") ?: "camera"
        val boundingBoxes = intent.getParcelableArrayListExtra<BoundingBox>("bounding_boxes")

        Log.d(TAG, "Image path: $imagePath")
        Log.d(TAG, "Captured photo path: $capturedPhotoPath")
        Log.d(TAG, "Original image path: $originalImagePath")
        Log.d(TAG, "Image source: $imageSource")
        Log.d(TAG, "Bounding boxes count: ${boundingBoxes?.size ?: 0}")

        boundingBoxes?.forEachIndexed { index, box ->
            Log.d(TAG, "Received box $index: ${box.clsName} - confidence: ${box.cnf}")
        }

        if (imagePath != null && boundingBoxes != null) {
            try {
                val file = File(imagePath)
                Log.d(TAG, "Processing file exists: ${file.exists()}")
                Log.d(TAG, "Processing file size: ${file.length()} bytes")

                val bitmap = BitmapFactory.decodeFile(imagePath)
                if (bitmap != null) {
                    Log.d(TAG, "Processing bitmap loaded successfully: ${bitmap.width}x${bitmap.height}")

                    var originalBitmap: Bitmap? = null
                    val originalPath = when (imageSource) {
                        "gallery" -> originalImagePath
                        "camera" -> capturedPhotoPath
                        else -> capturedPhotoPath
                    }

                    if (originalPath != null) {
                        val originalFile = File(originalPath)
                        if (originalFile.exists()) {
                            originalBitmap = BitmapFactory.decodeFile(originalPath)
                            Log.d(TAG, "Original image loaded: ${originalBitmap?.width}x${originalBitmap?.height}")
                        }
                    }

                    viewModel.processDetectionResults(
                        bitmap,
                        boundingBoxes,
                        originalBitmap,
                        originalPath,
                        imageSource
                    )

                    file.delete()
                    Log.d(TAG, "Temporary processing file deleted")
                } else {
                    Log.e(TAG, "Failed to decode bitmap from file")
                    Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bitmap from file", e)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Log.e(TAG, "Missing data - imagePath: $imagePath, boundingBoxes: ${boundingBoxes?.size}")
            Toast.makeText(this, "Data tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupDetectionRecyclerView(results: List<DetectionResult>) {
        val adapter = DetectionResultAdapter(results)
        binding.rvHasilDeteksi.adapter = adapter
    }

    private fun setupRecipeRecyclerView(recipes: List<com.rivaphys.citruschecky.data.ClassRecipes>) {
        Log.d(TAG, "Setting up recipe RecyclerView with ${recipes.size} class recipes")

        val recyclerView = binding.rvResepHasilDeteksi
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = RecipeAdapter(recipes)
        recyclerView.adapter = adapter

        Log.d(TAG, "Recipe RecyclerView setup complete")

        recipes.forEachIndexed { index, classRecipe ->
            Log.d(TAG, "Class $index: ${classRecipe.className} has ${classRecipe.recipes.size} recipes")
            classRecipe.recipes.forEachIndexed { recipeIndex, recipe ->
                Log.d(TAG, "  Recipe $recipeIndex: ${recipe.title}")
            }
        }
    }

    companion object {
        private const val TAG = "HasilDeteksiActivity"
    }
}