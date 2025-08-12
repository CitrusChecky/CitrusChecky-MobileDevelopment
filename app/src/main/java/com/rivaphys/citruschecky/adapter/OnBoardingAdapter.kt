package com.rivaphys.citruschecky.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rivaphys.citruschecky.data.OnBoardingItem
import com.rivaphys.citruschecky.ui.activity.OnBoardingActivity
import com.rivaphys.citruschecky.ui.fragment.OnBoardingFragment

class OnBoardingAdapter(
    fragment: OnBoardingActivity,
    private val onBoardingItem: List<OnBoardingItem>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = onBoardingItem.size

    override fun createFragment(position: Int): Fragment {
        return OnBoardingFragment.newInstance(onBoardingItem[position])
    }
}