package com.tuanha.wallet.ui.fragments.home.overview

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnLayout
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.resource.bitmap.CenterInside
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.tabs.TabLayoutMediator
import com.tuanha.coreapp.ui.base.adapters.MultiAdapter
import com.tuanha.coreapp.ui.base.adapters.PagerAdapter
import com.tuanha.coreapp.ui.base.fragments.BaseViewModelFragment
import com.tuanha.coreapp.ui.servicer.BaseForegroundService
import com.tuanha.coreapp.utils.autoCleared
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.DATA
import com.tuanha.wallet.DATA_CHAINS
import com.tuanha.wallet.DATA_WALLETS
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentOverviewBinding
import com.tuanha.wallet.entities.toCurrency
import com.tuanha.wallet.ui.fragments.SyncService
import com.tuanha.wallet.ui.fragments.chain.ChooseChainFragment
import com.tuanha.wallet.ui.fragments.currency.ChooseCurrencyFragment
import com.tuanha.wallet.ui.fragments.home.overview.asset.nft.OverviewAssetNftFragment
import com.tuanha.wallet.ui.fragments.home.overview.asset.token.OverviewAssetTokenFragment
import com.tuanha.wallet.ui.fragments.home.overview.asset.token.OverviewAssetTokenViewModel
import com.tuanha.wallet.ui.fragments.token.adapter.ActionAdapter
import com.tuanha.wallet.ui.fragments.wallet.ChooseWalletFragment
import com.tuanha.wallet.utils.ext.findViewModel

class OverviewFragment : BaseViewModelFragment<FragmentOverviewBinding, OverviewViewModel>(R.layout.fragment_overview), OverviewScope {


    private var actionAdapter by autoCleared<MultiAdapter>()


    private val assetViewModel by findViewModel<OverviewAssetTokenViewModel, OverviewScope>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.root?.doOnLayout {
            BaseForegroundService.startOrResume(requireActivity().applicationContext, SyncService::class.java)
        }

        setupChain()
        setupAction()
        setupWallet()
        setupCurrency()
        setupStatusBar()
        setupViewPager()

        observeData()
        observeAssetData()
    }

    private fun setupChain() {

        val binding = binding ?: return

        val keyRequest = "chooseChain"

        childFragmentManager.setFragmentResultListener(keyRequest, viewLifecycleOwner) { _, results ->

            viewModel.updateChains(results.getParcelableArrayList(DATA_CHAINS))
        }

        binding.frameChain.root.setBackgroundResource(R.drawable.bg_round_24_solid_surface_variant_stroke_divider_1dp)
        binding.frameChain.ivMore.setVisible(false)

        binding.frameChain.root.setDebouncedClickListener {

            ChooseChainFragment.newInstance(keyRequest, viewModel.chainList.getOrEmpty().map { it.idStr }).show(childFragmentManager, "")
        }
    }

    private fun setupAction() {

        val binding = binding ?: return

        val spanCount = 4
        val itemPadding = 4.toPx()

        binding.recAction.updatePadding(left = itemPadding * 3, right = itemPadding * 3)


        val adapter = ActionAdapter(itemPadding) { view, actionViewItem ->

        }

        actionAdapter = MultiAdapter(adapter).apply {

            binding.recAction.adapter = this
            binding.recAction.layoutManager = GridLayoutManager(binding.recAction.context, spanCount)
        }
    }

    private fun setupWallet() {

        val binding = binding ?: return

        val keyRequest = "chooseWallet"

        childFragmentManager.setFragmentResultListener(keyRequest, viewLifecycleOwner) { _, results ->

            viewModel.updateWallets(results.getParcelableArrayList(DATA_WALLETS))
        }

        binding.frameWallet.root.setBackgroundResource(R.drawable.bg_round_24_solid_surface_variant_stroke_divider_1dp)
        binding.frameWallet.ivMore.setVisible(false)

        binding.frameWallet.root.setDebouncedClickListener {

            ChooseWalletFragment.newInstance(keyRequest, viewModel.walletList.getOrEmpty().map { it.id }).show(childFragmentManager, "")
        }
    }

    private fun setupCurrency() {

        val binding = binding ?: return

        val keyRequest = "chooseCurrency"

        childFragmentManager.setFragmentResultListener(keyRequest, viewLifecycleOwner) { _, results ->

            viewModel.updateCurrency(results.getString(DATA)!!.toCurrency())
        }

        binding.frameCurrency.root.updatePadding(right = 16.toPx())

//        binding.frameCurrency.root.setBackgroundColor(Color.TRANSPARENT)
        binding.frameCurrency.ivMore.setImageResource(R.drawable.ic_more_on_surface_24dp)
        binding.frameCurrency.ivOption.setVisible(false)


        binding.frameCurrency.root.setDebouncedClickListener {

            ChooseCurrencyFragment.newInstance(keyRequest, listOfNotNull(viewModel.currency.value)).show(childFragmentManager, "")
        }
    }

    private fun setupStatusBar() = requireActivity().window.decorView.setOnApplyWindowInsetsListener { _, insets ->

        val binding = binding ?: return@setOnApplyWindowInsetsListener insets

        val statusHeight = insets.getStatusBar()

        if (statusHeight > 0) {
            binding.toolBar.resize(height = statusHeight + 56.toPx())
            binding.toolBar.updatePadding(top = statusHeight)

            binding.frameHeader.updateMargin(top = statusHeight + 56.toPx())
        }

        insets
    }

    private fun setupViewPager() {

        val binding = binding ?: return

        val pages = LinkedHashMap<String, Fragment>()

        pages[getString(R.string.assets_token)] = OverviewAssetTokenFragment()
        pages[getString(R.string.assets_nft)] = OverviewAssetNftFragment()
        pages["Favorites"] = OverviewAssetNftFragment()
        pages["Supply"] = OverviewAssetNftFragment()
        pages["Liquidity Pool"] = OverviewAssetNftFragment()

        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.offscreenPageLimit = pages.size
        binding.viewPager.adapter = PagerAdapter(childFragmentManager, viewLifecycleOwner.lifecycle, pages.keys.toList(), getTitle = {

            this
        }, getFragment = {

            pages[this]!!
        })

        TabLayoutMediator(binding.tabLayout, binding.viewPager, true, false) { tab, position -> tab.text = pages.keys.toList()[position] }.attach()
    }

    private fun observeData() = with(viewModel) {


        currency.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            binding.ivCurrency.setImageResource(it.imageRes)
            binding.frameCurrency.tvOption.setText(it.value)

            assetViewModel.updateCurrency(it)
        }

        chainDisplay.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            binding.frameChain.ivOption.load(it.first, if (it.first is ImageRes) CenterInside() else CircleCrop(), withCrossFade = false)
            binding.frameChain.tvOption.setText(it.second)
        }

        chainList.observe(viewLifecycleOwner) {

            assetViewModel.updateChains(it)
        }

        walletList.observe(viewLifecycleOwner) {

            assetViewModel.updateWallets(it)
        }

        walletDisplay.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            binding.frameWallet.ivOption.load(it.first, if (it.first is ImageRes) CenterInside() else CircleCrop(), withCrossFade = false)
            binding.frameWallet.tvOption.setText(it.second)
        }

        actionViewItemListDisplay.observe(viewLifecycleOwner) {

            actionAdapter?.submitList(it)
        }
    }

    private fun observeAssetData() = with(assetViewModel) {

        totalBalance.observe(viewLifecycleOwner) {

            val binding = binding ?: return@observe

            binding.tvTotalBalance.setText(it)
        }
    }
}

interface OverviewScope