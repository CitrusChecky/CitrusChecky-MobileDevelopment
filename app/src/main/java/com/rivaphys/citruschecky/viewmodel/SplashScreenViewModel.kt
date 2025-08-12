package com.rivaphys.citruschecky.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SplashScreenViewModel : ViewModel() {

    private val _splashScreenStatus = MutableLiveData<SplashScreenStatus>()
    val splashScreenStatus: LiveData<SplashScreenStatus> get() = _splashScreenStatus

    enum class SplashScreenStatus {
        LOADING,
        FINISHED
    }

    fun startSplashScreen() {
        _splashScreenStatus.value = SplashScreenStatus.LOADING

        object : CountDownTimer(5000, 1000) {
            override fun onTick(p0: Long) {

            }

            override fun onFinish() {
                _splashScreenStatus.value = SplashScreenStatus.FINISHED
            }
        }.start()
    }
}