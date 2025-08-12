package com.rivaphys.citruschecky.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.adapter.OnBoardingAdapter
import com.rivaphys.citruschecky.data.PreferenceHelper
import com.rivaphys.citruschecky.databinding.ActivityOnBoardingBinding
import com.rivaphys.citruschecky.viewmodel.OnBoardingViewModel

class OnBoardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnBoardingBinding
    private val viewModel: OnBoardingViewModel by viewModels()
    private lateinit var adapter: OnBoardingAdapter
    private lateinit var preferenceHelper: PreferenceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOnBoardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupViewPager()
        setupObservers()
        setupClickListener()
        updateActivityBackground(viewModel.onBoardingItem[0].backgroundColor)

        preferenceHelper = PreferenceHelper(this)

    }

    private fun setupViewPager() {
        adapter = OnBoardingAdapter(this, viewModel.onBoardingItem)
        binding.vpOnBoarding.adapter = adapter

        binding.dotsOnBoarding.attachTo(binding.vpOnBoarding)

        binding.vpOnBoarding.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.setCurrentPosition(position)
                updateButtonVisibility(position)

                val currentItem = viewModel.onBoardingItem[position]
                updateActivityBackground(currentItem.backgroundColor)
            }
        })
    }

    private fun updateActivityBackground(backgroundColor: Int) {
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, backgroundColor))
    }

    private fun updateButtonVisibility(position: Int) {
        when (position) {
            0 -> {
                binding.btnNextOnBoarding.visibility = View.VISIBLE
                binding.btnBackOnBoarding.visibility = View.GONE
                binding.btnAyoMulaiOnBoarding.visibility = View.GONE
            }

            1 -> {
                binding.btnNextOnBoarding.visibility = View.VISIBLE
                binding.btnBackOnBoarding.visibility = View.VISIBLE
                binding.btnAyoMulaiOnBoarding.visibility = View.GONE
            }

            2 -> {
                binding.btnNextOnBoarding.visibility = View.GONE
                binding.btnBackOnBoarding.visibility = View.VISIBLE
                binding.btnAyoMulaiOnBoarding.visibility = View.VISIBLE
            }
        }
    }

    private fun setupObservers() {
        viewModel.navigateToMain.observe(this) { navigate ->
            if (navigate) {
                navigateToMainActivity()
                viewModel.onNavigateToMainComplete()
            }
        }
    }


    private fun navigateToMainActivity() {
        // harusnye ke homefragment
        preferenceHelper.setFirstTime(false)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupClickListener() {
        binding.btnNextOnBoarding.setOnClickListener {
            val currentItem = binding.vpOnBoarding.currentItem
            if (currentItem < adapter.itemCount - 1) {
                binding.vpOnBoarding.currentItem = currentItem + 1
            }
        }

        binding.btnBackOnBoarding.setOnClickListener {
            val currentItem = binding.vpOnBoarding.currentItem
            if (currentItem > 0) {
                binding.vpOnBoarding.currentItem = currentItem - 1
            }
        }

        binding.btnAyoMulaiOnBoarding.setOnClickListener {
            viewModel.navigateToMainActivity()
        }
    }
}