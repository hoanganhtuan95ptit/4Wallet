package com.tuanha.wallet.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.tuanha.coreapp.ui.base.adapters.PagerAdapter
import com.tuanha.coreapp.ui.base.fragments.BaseViewBindingFragment
import com.tuanha.coreapp.utils.extentions.getNavigationBar
import com.tuanha.coreapp.utils.extentions.resize
import com.tuanha.coreapp.utils.extentions.setDebouncedClickListener
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentHomeBinding
import com.tuanha.wallet.ui.fragments.home.dapp.DAppFragment
import com.tuanha.wallet.ui.fragments.home.market.MarketFragment
import com.tuanha.wallet.ui.fragments.home.overview.OverviewFragment
import com.tuanha.wallet.ui.fragments.home.setting.SettingFragment

class HomeFragment : BaseViewBindingFragment<FragmentHomeBinding>(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStatusBar()
        setupViewPager()
    }

    private fun setupStatusBar() = requireActivity().window.decorView.setOnApplyWindowInsetsListener { _, insets ->

        val binding = binding ?: return@setOnApplyWindowInsetsListener insets

        val navigationHeight = insets.getNavigationBar()

        if (navigationHeight > 0) {
            binding.vNavigationBar.resize(height = navigationHeight)
        }

        insets
    }

    private fun setupViewPager() {

        val binding = binding ?: return

        val pages = linkedMapOf<View, Fragment>(
            binding.ivHome to OverviewFragment(),
            binding.ivMarket to MarketFragment(),
            binding.ivNothing to DAppFragment(),
            binding.ivSetting to SettingFragment(),
        )

        pages.keys.toList().forEach {

            it.setDebouncedClickListener { tab ->
                binding.viewPager.setCurrentItem(pages.keys.toList().indexOf(tab), false)
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {

                binding.test.doOnPreDraw { binding.test.select(pages.keys.toList()[position]) }

                pages.keys.toList().forEachIndexed { index, imageView -> imageView.isSelected = index == position }
            }
        })

        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = pages.size
        binding.viewPager.adapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, pages.values.toList(), getFragment = { this })
    }
}