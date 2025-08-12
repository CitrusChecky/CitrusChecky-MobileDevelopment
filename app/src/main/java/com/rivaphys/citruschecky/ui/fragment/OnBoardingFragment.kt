package com.rivaphys.citruschecky.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.rivaphys.citruschecky.R
import com.rivaphys.citruschecky.data.OnBoardingItem
import com.rivaphys.citruschecky.databinding.FragmentOnBoardingBinding

class OnBoardingFragment : Fragment() {

    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val item = arguments?.getParcelable<OnBoardingItem>(ARG_ON_BOARDING_ITEM)
        item?.let { setupView(it) }
    }

    private fun setupView(onBoardingItem: OnBoardingItem) {
        binding.apply {
            tvTitleOnBoarding.text = onBoardingItem.title
            tvDescOnBoarding.text = onBoardingItem.description
            ivOnBoarding.setImageResource(onBoardingItem.image)
            root.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    onBoardingItem.backgroundColor
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val ARG_ON_BOARDING_ITEM = "onboarding_item"

        fun newInstance(item: OnBoardingItem): OnBoardingFragment {
            val fragment = OnBoardingFragment()
            val args = Bundle()
            args.putParcelable(ARG_ON_BOARDING_ITEM, item)
            fragment.arguments = args
            return fragment
        }
    }
}