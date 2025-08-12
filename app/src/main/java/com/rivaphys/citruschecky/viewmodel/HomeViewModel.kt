package com.rivaphys.citruschecky.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rivaphys.citruschecky.data.BoundingBox

class HomeViewModel : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _navigateToGallery = MutableLiveData<Boolean>()
    val navigateToGallery: LiveData<Boolean> = _navigateToGallery

    private val _navigateToInfo = MutableLiveData<Boolean>()
    val navigateToInfo: LiveData<Boolean> = _navigateToInfo

    // Menyimpan deteksi saat ini
    private val _currentDetections = MutableLiveData<List<BoundingBox>>()
    val currentDetections: LiveData<List<BoundingBox>> = _currentDetections

    fun navigateToGallery() {
        _navigateToGallery.value = true
    }

    fun onGalleryNavigationComplete() {
        _navigateToGallery.value = false
    }

    fun navigateToInfo() {
        _navigateToInfo.value = true
    }

    fun onInfoNavigationComplete() {
        _navigateToInfo.value = false
    }

    fun updateCurrentDetections(detections: List<BoundingBox>) {
        Log.d(TAG, "Updating current detections: ${detections.size}")
        detections.forEachIndexed { index, detection ->
            Log.d(TAG, "Detection $index: ${detection.clsName} - ${detection.cnf}")
        }
        _currentDetections.value = detections
    }

    fun getCurrentDetections(): List<BoundingBox> {
        val detections = _currentDetections.value ?: emptyList()
        Log.d(TAG, "Getting current detections: ${detections.size}")
        return detections
    }
}