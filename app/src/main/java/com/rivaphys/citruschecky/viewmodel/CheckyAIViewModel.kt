package com.rivaphys.citruschecky.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.java.ChatFutures
import com.rivaphys.citruschecky.utils.CheckyAI
import com.rivaphys.citruschecky.data.Response

class CheckyAIViewModel : ViewModel() {
    companion object {
        private const val TAG = "CheckyAIViewModel"
    }

    private val _aiResponse = MutableLiveData<String>()
    val aiResponse: LiveData<String> = _aiResponse

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private lateinit var chatFutures: ChatFutures

    init {
        initializeGeminiAI()
    }

    private fun initializeGeminiAI() {
        try {
            val model = CheckyAI()
            val modelFuture = model.getModelAPI()
            chatFutures = modelFuture.startChat()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Gemini AI", e)
            _error.value = "Gagal menginisialisasi AI: ${e.message}"
        }
    }

    fun getRecipeRecommendations(detectedClasses: List<String>) {
        if (detectedClasses.isEmpty()) {
            _error.value = "Tidak ada kelas jeruk yang terdeteksi"
            return
        }

        _isLoading.value = true

        val uniqueClasses = detectedClasses.distinct()
        val classesText = uniqueClasses.joinToString(", ")

        val prompt = """
        Saya adalah CheckyAI, asisten resep di aplikasi CitrusChecky. 
        Berdasarkan kondisi jeruk yang terdeteksi: $classesText
        
        Berikan resep non-makanan (pembersih alami, perawatan kulit, aromaterapi, pewangi ruangan, atau kerajinan tangan) untuk setiap kondisi jeruk yang berbeda.
        
        Aturan:
        - Berikan 4-5 resep per kondisi jeruk yang terdeksi.
        - Jika ada beberapa jeruk dengan kondisi sama, cukup berikan resep untuk kondisi tersebut sekali saja
        - Meski jeruk sangat busuk, tetap berikan resep yang bisa dimanfaatkan
        - Jelaskan bahan dan cara pembuatan dengan detail
    """.trimIndent()

        CheckyAI.getResponse(chatFutures, prompt, object : Response {
            override fun onResponse(response: String) {
                _isLoading.value = false
                _aiResponse.value = response
                Log.d(TAG, "AI Response received: ${response.length} characters")
            }

            override fun onError(throwable: Throwable) {
                _isLoading.value = false
                _error.value = "Terjadi kesalahan: ${throwable.message}"
                Log.e(TAG, "AI Response error", throwable)
            }
        })
    }
}