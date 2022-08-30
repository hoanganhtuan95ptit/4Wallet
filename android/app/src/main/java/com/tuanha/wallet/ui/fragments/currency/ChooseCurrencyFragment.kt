package com.tuanha.wallet.ui.fragments.currency

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.tuanha.coreapp.ui.base.adapters.MultiAdapter
import com.tuanha.coreapp.ui.base.dialogs.BaseViewModelSheetFragment
import com.tuanha.coreapp.utils.autoCleared
import com.tuanha.wallet.DATA
import com.tuanha.wallet.KEY_REQUEST
import com.tuanha.wallet.PARAM_CURRENCY
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentChooseCurrencyBinding
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.entities.toCurrencies
import com.tuanha.wallet.entities.toStrings
import com.tuanha.wallet.ui.fragments.currency.adapter.ChooseCurrencyAdapter

class ChooseCurrencyFragment : BaseViewModelSheetFragment<FragmentChooseCurrencyBinding, ChooseCurrencyViewModel>(R.layout.fragment_choose_currency) {

    private var adapter by autoCleared<MultiAdapter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitle()
        setupRecyclerView()

        observeData()
    }

    private fun setupTitle() {

        val binding = binding ?: return

        binding.tvTitle.setText(R.string.title_choose_chain)
    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val chooseChainAdapter = ChooseCurrencyAdapter { _, item ->
            setFragmentResult(requireArguments().getString(KEY_REQUEST, ""), bundleOf(DATA to item.data.value))
            dismiss()
        }

        adapter = MultiAdapter(chooseChainAdapter).apply {

            setRecyclerView(binding.recyclerView)
        }
    }

    private fun observeData() = with(viewModel) {

        viewItemListDisplay.observe(viewLifecycleOwner) {

            adapter?.submitList(it)
        }

        updateChainIds(arguments?.getStringArrayList(PARAM_CURRENCY)?.toCurrencies() ?: emptyList())
    }

    companion object {

        fun newInstance(keyRequest: String = "", chainIds: List<MarketCurrency>) = ChooseCurrencyFragment().apply {
            arguments = bundleOf(
                KEY_REQUEST to keyRequest,
                PARAM_CURRENCY to chainIds.toStrings()
            )
        }
    }
}