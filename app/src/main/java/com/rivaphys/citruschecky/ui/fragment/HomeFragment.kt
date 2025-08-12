package com.rivaphys.citruschecky.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rivaphys.citruschecky.utils.Constants
import com.rivaphys.citruschecky.utils.Detector
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.data.BoundingBox
import com.rivaphys.citruschecky.databinding.FragmentHomeBinding
import com.rivaphys.citruschecky.ui.activity.HasilDeteksiActivity
import com.rivaphys.citruschecky.viewmodel.HomeViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeFragment : Fragment(), Detector.DetectorListener {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var detector: Detector
    private val isFrontCamera = false

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: androidx.camera.core.Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private var previewWidth = 0
    private var previewHeight = 0
    private var analyzerWidth = 0
    private var analyzerHeight = 0

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            processGalleryImage(selectedImageUri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        detector = Detector(requireContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, this)
        detector.setup()

        setupViews()
        setupObservers()
        setupCamera()
    }

    private fun setupViews() {
        binding.apply {
            btnGalleryHome.setOnClickListener {
                viewModel.navigateToGallery()
            }

            btnCaptureHome.setOnClickListener {
                capturePhoto()
            }

            btnInformationHome.setOnClickListener {
                viewModel.navigateToInfo()
            }
        }
    }

    private fun setupObservers() {
        viewModel.navigateToGallery.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                galleryLauncher.launch("image/*")
                viewModel.onGalleryNavigationComplete()
            }
        }

        viewModel.navigateToInfo.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_homeFragment_to_infoFragment)
                viewModel.onInfoNavigationComplete()
            }
        }
    }

    private fun setupCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        val rotation = binding.previewCameraHome.display.rotation

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // Use aspect ratio instead of fixed resolution for flexibility
        // Model akan handle resize ke 960x960 di preprocessing
        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        imageAnalyzer?.setAnalyzer(cameraExecutor) { imageProxy ->
            // Store analyzer dimensions
            analyzerWidth = imageProxy.width
            analyzerHeight = imageProxy.height

            Log.d(TAG, "Analyzer dimensions: ${analyzerWidth}x${analyzerHeight}")
            Log.d(TAG, "Rotation: ${imageProxy.imageInfo.rotationDegrees}")

            val bitmapBuffer = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

            val matrix = Matrix().apply {
                // Handle rotation properly
                when (imageProxy.imageInfo.rotationDegrees) {
                    90 -> {
                        postRotate(90f)
                        postTranslate(imageProxy.height.toFloat(), 0f)
                    }
                    180 -> {
                        postRotate(180f)
                        postTranslate(imageProxy.width.toFloat(), imageProxy.height.toFloat())
                    }
                    270 -> {
                        postRotate(270f)
                        postTranslate(0f, imageProxy.width.toFloat())
                    }
                }

                if (isFrontCamera) {
                    postScale(-1f, 1f, imageProxy.width.toFloat() / 2, imageProxy.height.toFloat() / 2)
                }
            }

            val rotatedBitmap = if (imageProxy.imageInfo.rotationDegrees != 0) {
                Bitmap.createBitmap(
                    bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true
                )
            } else {
                bitmapBuffer
            }

            imageProxy.close()
            detector.detect(rotatedBitmap)
        }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                imageAnalyzer,
                imageCapture
            )

            preview?.setSurfaceProvider(binding.previewCameraHome.surfaceProvider)

            // Get preview dimensions after binding
            preview?.setSurfaceProvider { surfaceRequest ->
                binding.previewCameraHome.surfaceProvider.onSurfaceRequested(surfaceRequest)

                // Wait a bit for surface to be ready, then get dimensions
                binding.previewCameraHome.post {
                    previewWidth = binding.previewCameraHome.width
                    previewHeight = binding.previewCameraHome.height

                    Log.d(TAG, "Preview dimensions: ${previewWidth}x${previewHeight}")

                    // Update overlay with correct scaling
                    updateBoundingBoxScaling()
                }
            }

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun updateBoundingBoxScaling() {
        if (previewWidth > 0 && previewHeight > 0 && analyzerWidth > 0 && analyzerHeight > 0) {
            // Calculate scaling factors
            val scaleX = previewWidth.toFloat() / analyzerWidth.toFloat()
            val scaleY = previewHeight.toFloat() / analyzerHeight.toFloat()

            Log.d(TAG, "Calculated scaling: scaleX=$scaleX, scaleY=$scaleY")

            // Apply scaling to overlay
            binding.boundingBoxHome.setScaleParameters(scaleX, scaleY)
        }
    }

    override fun onEmptyDetect() {
        requireActivity().runOnUiThread {
            Log.d(TAG, "No detections - clearing overlay")
            binding.boundingBoxHome.clearDetections()
            viewModel.updateCurrentDetections(emptyList())
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        requireActivity().runOnUiThread {
            // Ensure scaling is updated before setting results
            updateBoundingBoxScaling()

            binding.boundingBoxHome.setResults(boundingBoxes)
            viewModel.updateCurrentDetections(boundingBoxes)

            Log.d(TAG, "Detected ${boundingBoxes.size} objects")
            Log.d(TAG, "Inference time: ${inferenceTime}ms")

            // Debug log coordinates
            boundingBoxes.forEachIndexed { index, box ->
                Log.d(TAG, "Box $index: (${box.x1}, ${box.y1}, ${box.x2}, ${box.y2}) - ${box.clsName}")
            }
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val currentDetections = viewModel.getCurrentDetections()
        Log.d(TAG, "Current detections count: ${currentDetections.size}")

        if (currentDetections.isEmpty()) {
            Toast.makeText(requireContext(), "Tidak ada objek yang terdeteksi", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // Buat file untuk menyimpan foto yang diambil
        val photoFile = File(
            requireContext().cacheDir,
            "captured_photo_${System.currentTimeMillis()}.jpg"
        )

        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    Toast.makeText(requireContext(), "Gagal mengambil foto", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo saved to: ${photoFile.absolutePath}")

                    lifecycleScope.launch {
                        try {
                            // Load bitmap dari file yang baru disimpan
                            val capturedBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

                            if (capturedBitmap != null) {
                                Log.d(
                                    TAG,
                                    "Captured bitmap loaded: ${capturedBitmap.width}x${capturedBitmap.height}"
                                )
                                navigateToResultsActivity(
                                    capturedBitmap,
                                    currentDetections,
                                    photoFile.absolutePath
                                )
                            } else {
                                Log.e(TAG, "Failed to load captured bitmap")
                                Toast.makeText(
                                    requireContext(),
                                    "Gagal memproses foto",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing captured photo", e)
                            Toast.makeText(
                                requireContext(),
                                "Gagal memproses foto",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        )
    }

    private fun navigateToResultsActivity(
        bitmap: Bitmap,
        detections: List<BoundingBox>,
        photoPath: String
    ) {
        Log.d(TAG, "=== DEBUG navigateToResultsActivity ===")
        Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")
        Log.d(TAG, "Photo path: $photoPath")
        Log.d(TAG, "Detections count: ${detections.size}")

        detections.forEachIndexed { index, detection ->
            Log.d(TAG, "Detection $index: ${detection.clsName} - confidence: ${detection.cnf}")
        }

        try {
            // Simpan bitmap untuk processing ke file terpisah
            val processingFile = File(
                requireContext().cacheDir,
                "processing_image_${System.currentTimeMillis()}.jpg"
            )

            FileOutputStream(processingFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }

            Log.d(TAG, "Processing bitmap saved to: ${processingFile.absolutePath}")

            val intent = Intent(requireContext(), HasilDeteksiActivity::class.java).apply {
                putExtra("image_path", processingFile.absolutePath)
                putExtra("captured_photo_path", photoPath) // Path foto asli yang diambil
                putParcelableArrayListExtra("bounding_boxes", ArrayList(detections))
            }

            Log.d(TAG, "Starting HasilDeteksiActivity...")
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Error in navigateToResultsActivity", e)
            Toast.makeText(
                requireContext(),
                "Gagal memproses gambar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun processGalleryImage(imageUri: Uri) {
        try {
            Log.d(TAG, "Processing gallery image: $imageUri")

            // Load bitmap from URI
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(TAG, "Gallery bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")

            // Resize bitmap if too large for processing
            val maxSize = 1024
            val resizedBitmap =
                if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
                    val ratio = minOf(
                        maxSize.toFloat() / originalBitmap.width,
                        maxSize.toFloat() / originalBitmap.height
                    )
                    val newWidth = (originalBitmap.width * ratio).toInt()
                    val newHeight = (originalBitmap.height * ratio).toInt()

                    Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                } else {
                    originalBitmap
                }

            // Run detection on the selected image
            lifecycleScope.launch {
                try {
                    // Create a temporary file for the selected image
                    val tempFile = File(
                        requireContext().cacheDir,
                        "gallery_image_${System.currentTimeMillis()}.jpg"
                    )
                    FileOutputStream(tempFile).use { fos ->
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                    }

                    // Run detection using the detector
                    val detectionResults = mutableListOf<BoundingBox>()

                    // Create a callback to capture detection results
                    val tempDetectorListener = object : Detector.DetectorListener {
                        override fun onEmptyDetect() {
                            Log.d(TAG, "No objects detected in gallery image")
                            requireActivity().runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Tidak ada objek yang terdeteksi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onDetect(
                            boundingBoxes: List<BoundingBox>,
                            inferenceTime: Long
                        ) {
                            Log.d(
                                TAG,
                                "Gallery detection complete: ${boundingBoxes.size} objects detected"
                            )
                            detectionResults.addAll(boundingBoxes)

                            requireActivity().runOnUiThread {
                                if (boundingBoxes.isNotEmpty()) {
                                    navigateToResultsActivityFromGallery(
                                        resizedBitmap,
                                        boundingBoxes,
                                        tempFile.absolutePath
                                    )
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Tidak ada objek yang terdeteksi",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    // Create temporary detector for gallery image
                    val galleryDetector = Detector(
                        requireContext(),
                        Constants.MODEL_PATH,
                        Constants.LABELS_PATH,
                        tempDetectorListener
                    )
                    galleryDetector.setup()
                    galleryDetector.detect(resizedBitmap)

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing gallery image", e)
                    requireActivity().runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Gagal memproses gambar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading gallery image", e)
            Toast.makeText(requireContext(), "Gagal memuat gambar dari galeri", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun navigateToResultsActivityFromGallery(
        bitmap: Bitmap,
        detections: List<BoundingBox>,
        imagePath: String
    ) {
        Log.d(TAG, "=== DEBUG navigateToResultsActivityFromGallery ===")
        Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")
        Log.d(TAG, "Image path: $imagePath")
        Log.d(TAG, "Detections count: ${detections.size}")

        detections.forEachIndexed { index, detection ->
            Log.d(TAG, "Detection $index: ${detection.clsName} - confidence: ${detection.cnf}")
        }

        try {
            // Save bitmap for processing
            val processingFile = File(
                requireContext().cacheDir,
                "gallery_processing_${System.currentTimeMillis()}.jpg"
            )
            FileOutputStream(processingFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            }

            Log.d(TAG, "Gallery processing bitmap saved to: ${processingFile.absolutePath}")

            val intent = Intent(requireContext(), HasilDeteksiActivity::class.java).apply {
                putExtra("image_path", processingFile.absolutePath)
                putExtra("original_image_path", imagePath) // Original gallery image path
                putExtra("image_source", "gallery") // Flag to indicate source
                putParcelableArrayListExtra("bounding_boxes", ArrayList(detections))
            }

            Log.d(TAG, "Starting HasilDeteksiActivity from gallery...")
            startActivity(intent)

        } catch (e: Exception) {
            Log.e(TAG, "Error in navigateToResultsActivityFromGallery", e)
            Toast.makeText(
                requireContext(),
                "Gagal memproses gambar: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        cameraExecutor.shutdown()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}