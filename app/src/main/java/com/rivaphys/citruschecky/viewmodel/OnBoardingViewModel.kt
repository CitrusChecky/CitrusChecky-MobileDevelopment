package com.rivaphys.citruschecky.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.data.OnBoardingItem

class OnBoardingViewModel : ViewModel() {

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> = _currentPosition

    private val _navigateToMain = MutableLiveData<Boolean>()
    val navigateToMain: LiveData<Boolean> = _navigateToMain

    val onBoardingItem = listOf(
        OnBoardingItem(
            title = "Scan Jeruk Langsung",
            description = "Arahkan jerukmu langsung ke kamera atau pilih dari galeri. CitrusChecky akan deteksi langsung tingkat kebusukannya.",
            image = R.drawable.onboarding1,
            backgroundColor = R.color.secondary_citrus_modern
        ),
        OnBoardingItem(
            title = "Resep Untuk Setiap Tingkat",
            description = "Dapatkan resep yang tepat berdasarkan kondisi tingkat kebusukan jeruk, mulai dari matang, sedikit busuk, dan sangat busuk.",
            image = R.drawable.onboarding2,
            backgroundColor = R.color.secondary_tropical_gradient
        ),
        OnBoardingItem(
            title = "CheckyAI",
            description = "Tanya CheckyAI untuk inspirasi resep jeruk yang lebih banyak.",
            image = R.drawable.onboarding3,
            backgroundColor = R.color.secondary_sophisticated_citrus
        )
    )

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    fun navigateToMainActivity() {
        _navigateToMain.value = true
    }

    fun onNavigateToMainComplete() {
        _navigateToMain.value = false
    }
}