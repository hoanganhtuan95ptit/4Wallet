package com.tuanha.wallet.ui.fragments.home.overview.asset.token

import android.os.Bundle
import android.view.View
import com.tuanha.coreapp.ui.base.adapters.MultiAdapter
import com.tuanha.coreapp.ui.base.fragments.BaseViewModelFragment
import com.tuanha.coreapp.utils.autoCleared
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentOverviewAssetTokenBinding
import com.tuanha.wallet.ui.fragments.home.overview.OverviewScope
import com.tuanha.wallet.ui.fragments.token.adapter.TokenAdapter
import com.tuanha.wallet.utils.ext.findViewModel

class OverviewAssetTokenFragment : BaseViewModelFragment<FragmentOverviewAssetTokenBinding, OverviewAssetTokenViewModel>(R.layout.fragment_overview_asset_token) {


    override val viewModel by findViewModel<OverviewAssetTokenViewModel, OverviewScope>()

    private var tokenAdapter by autoCleared<MultiAdapter>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        observeData()
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val adapter = TokenAdapter()

        tokenAdapter = MultiAdapter(adapter).apply {

            scrollTop(true)
            setRecyclerView(binding.recyclerView)
        }
    }

    private fun observeData() = with(viewModel) {

        listTokenViewItemDisplay.observe(viewLifecycleOwner) {

            tokenAdapter?.submitList(it)
        }
    }
}