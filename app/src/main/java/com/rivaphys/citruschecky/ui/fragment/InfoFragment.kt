package com.rivaphys.citruschecky.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rivaphys.citruschecky.utils.Constants
import com.rivaphys.citruschecky.utils.Detector
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.adapter.InformationAdapter
import com.rivaphys.citruschecky.data.BoundingBox
import com.rivaphys.citruschecky.databinding.FragmentInfoBinding
import com.rivaphys.citruschecky.ui.activity.HasilDeteksiActivity
import com.rivaphys.citruschecky.viewmodel.InfoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class InfoFragment : Fragment(), Detector.DetectorListener {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InfoViewModel by viewModels()
    private lateinit var informationAdapter: InformationAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch gallery
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(requireContext(), "Camera permission required for gallery access", Toast.LENGTH_SHORT)
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupViews()
        setupObservers()
    }

    private fun setupRecyclerView() {
        informationAdapter = InformationAdapter()
        binding.rvInformation.apply {
            adapter = informationAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupViews() {
        binding.apply {
            btnGalleryInfo.setOnClickListener {
                viewModel.navigateToGallery()
            }

            btnCaptureInfo.setOnClickListener {
                viewModel.navigateToHome()
            }

            btnInformationInfo.setOnClickListener {
                Toast.makeText(requireContext(), "You are already in Information page", Toast.LENGTH_SHORT).show()
            }

            btnGithubLink.setOnClickListener {
                openGitHubLink()
            }
        }
    }

    private fun openGitHubLink() {
        try {
            val githubUrl = "https://github.com/CitrusChecky"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Cannot open GitHub link", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Error opening GitHub link", e)
        }
    }

    private fun setupObservers() {
        viewModel.informationItems.observe(viewLifecycleOwner) { items ->
            informationAdapter.submitList(items)
        }

        viewModel.navigateToGallery.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                checkPermissionAndOpenGallery()
                viewModel.onGalleryNavigationComplete()
            }
        }

        viewModel.navigateToHome.observe(viewLifecycleOwner) { shouldNavigate ->
            if (shouldNavigate) {
                findNavController().navigate(R.id.action_infoFragment_to_homeFragment)
                viewModel.onHomeNavigationComplete()
            }
        }
    }

    private fun checkPermissionAndOpenGallery() {
        if (allPermissionsGranted()) {
            galleryLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

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
            val resizedBitmap = if (originalBitmap.width > maxSize || originalBitmap.height > maxSize) {
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
                    val tempFile = File(requireContext().cacheDir, "gallery_image_${System.currentTimeMillis()}.jpg")
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
                                Toast.makeText(requireContext(), "Tidak ada objek yang terdeteksi", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
                            Log.d(TAG, "Gallery detection complete: ${boundingBoxes.size} objects detected")
                            detectionResults.addAll(boundingBoxes)

                            requireActivity().runOnUiThread {
                                if (boundingBoxes.isNotEmpty()) {
                                    navigateToResultsActivityFromGallery(resizedBitmap, boundingBoxes, tempFile.absolutePath)
                                } else {
                                    Toast.makeText(requireContext(), "Tidak ada objek yang terdeteksi", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    // Create temporary detector for gallery image
                    val galleryDetector = Detector(requireContext(), Constants.MODEL_PATH, Constants.LABELS_PATH, tempDetectorListener)
                    galleryDetector.setup()
                    galleryDetector.detect(resizedBitmap)

                } catch (e: Exception) {
                    Log.e(TAG, "Error processing gallery image", e)
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error loading gallery image", e)
            Toast.makeText(requireContext(), "Gagal memuat gambar dari galeri", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToResultsActivityFromGallery(bitmap: Bitmap, detections: List<BoundingBox>, imagePath: String) {
        Log.d(TAG, "=== DEBUG navigateToResultsActivityFromGallery ===")
        Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")
        Log.d(TAG, "Image path: $imagePath")
        Log.d(TAG, "Detections count: ${detections.size}")

        detections.forEachIndexed { index, detection ->
            Log.d(TAG, "Detection $index: ${detection.clsName} - confidence: ${detection.cnf}")
        }

        try {
            // Save bitmap for processing
            val processingFile = File(requireContext().cacheDir, "gallery_processing_${System.currentTimeMillis()}.jpg")
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
            Toast.makeText(requireContext(), "Gagal memproses gambar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Required by Detector.DetectorListener interface
    override fun onEmptyDetect() {
        // This won't be called since we're using a temporary detector
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        // This won't be called since we're using a temporary detector
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "InfoFragment"
    }
}