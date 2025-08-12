package com.rivaphys.citruschecky.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.data.PreferenceHelper
import com.rivaphys.citruschecky.databinding.ActivitySplashScreenBinding
import com.rivaphys.citruschecky.viewmodel.SplashScreenViewModel
import com.rivaphys.citruschecky.viewmodel.SplashScreenViewModel.SplashScreenStatus.*

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var viewModel: SplashScreenViewModel
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[SplashScreenViewModel::class.java]
        viewModel.startSplashScreen()

        viewModel.splashScreenStatus.observe(this, { status ->
            when (status) {
                FINISHED -> {
                    val sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)
                    val isFirstTime = sharedPref.getBoolean("is_first_time", true)

                    if (isFirstTime) {
                        // Pertama kali, tampilkan onboarding
                        navigateToOnBoarding()
                        // Set flag bahwa sudah tidak first time lagi
                        sharedPref.edit().putBoolean("is_first_time", false).apply()
                    } else {
                        // Bukan pertama kali, langsung ke main activity
                        navigateToMainActivity()
                    }
                }

                LOADING -> {

                }
            }
        })
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToOnBoarding() {
        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()
    }
}