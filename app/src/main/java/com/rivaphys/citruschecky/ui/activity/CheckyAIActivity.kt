package com.rivaphys.citruschecky.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rivaphys.citruschecky.utils.MarkdownProcessor
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.databinding.ActivityCheckyAiactivityBinding
import com.rivaphys.citruschecky.viewmodel.CheckyAIViewModel

class CheckyAIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckyAiactivityBinding
    private val viewModel: CheckyAIViewModel by viewModels()

    companion object {
        private const val TAG = "CheckyAIActivity"
        const val EXTRA_DETECTED_CLASSES = "detected_classes"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckyAiactivityBinding.inflate(layoutInflater)
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
        binding.btnBackCheckyai.setOnClickListener {
            finish()
        }

        // Initially hide content and show loading
        showLoading()
    }

    private fun setupObservers() {
        viewModel.aiResponse.observe(this) { response ->
            Log.d(TAG, "AI response received: ${response.length} characters")
            hideLoading()

            // Process markdown and set to TextView
            val processedText = MarkdownProcessor.processMarkdown(response)
            binding.tvAnswerCheckyai.text = processedText
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLoading()
            } else {
                hideLoading()
            }
        }

        viewModel.error.observe(this) { error ->
            if (error.isNotEmpty()) {
                hideLoading()
                binding.tvAnswerCheckyai.text = "Error: $error\n\nSilakan coba lagi."
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showLoading() {
        binding.scrollCheckyai.visibility = View.GONE
        binding.layoutLoading.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.layoutLoading.visibility = View.GONE
        binding.scrollCheckyai.visibility = View.VISIBLE
    }

    private fun processIntentData() {
        val detectedClasses = intent.getStringArrayListExtra(EXTRA_DETECTED_CLASSES)

        Log.d(TAG, "Detected classes received: $detectedClasses")

        if (detectedClasses != null && detectedClasses.isNotEmpty()) {
            viewModel.getRecipeRecommendations(detectedClasses)
        } else {
            binding.tvAnswerCheckyai.text = "Tidak ada data jeruk yang terdeteksi. Silakan kembali dan lakukan deteksi terlebih dahulu."
            Log.e(TAG, "No detected classes received")
        }
    }
}