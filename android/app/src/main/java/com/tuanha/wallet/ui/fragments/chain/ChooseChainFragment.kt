package com.tuanha.wallet.ui.fragments.chain

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tuanha.coreapp.ui.base.adapters.MultiAdapter
import com.tuanha.coreapp.ui.base.dialogs.BaseViewModelSheetFragment
import com.tuanha.coreapp.utils.autoCleared
import com.tuanha.coreapp.utils.extentions.getOrEmpty
import com.tuanha.coreapp.utils.extentions.setDebouncedClickListener
import com.tuanha.coreapp.utils.extentions.toPx
import com.tuanha.wallet.DATA_CHAINS
import com.tuanha.wallet.KEY_REQUEST
import com.tuanha.wallet.PARAM_CHAIN_ID
import com.tuanha.wallet.R
import com.tuanha.wallet.databinding.FragmentListBinding
import com.tuanha.wallet.databinding.FragmentListChooseBinding
import com.tuanha.wallet.ui.fragments.chain.adapter.ChooseChainAdapter
import com.tuanha.wallet.ui.fragments.chain.adapter.ChooseChainViewItem

class ChooseChainFragment : BaseViewModelSheetFragment<FragmentListChooseBinding, ChooseChainViewModel>(R.layout.fragment_list_choose) {

    private var adapter by autoCleared<MultiAdapter>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTitle()
        setupNegative()
        setupPositive()
        setupBottomSheet()
        setupRecyclerView()

        observeData()
    }

    private fun setupTitle() {

        val binding = binding ?: return

        binding.tvTitle.setText(R.string.title_choose_chain)
    }

    private fun setupNegative() {

        val binding = binding ?: return
        binding.tvNegative.setDebouncedClickListener {

            dismiss()
        }
    }

    private fun setupPositive() {

        val binding = binding ?: return

        binding.tvPositive.setDebouncedClickListener {

            var chainSelectList = viewModel.viewItemListDisplay.value?.filterIsInstance<ChooseChainViewItem>()?.filter { it.isSelected }?.map { it.data } ?: emptyList()

            if (chainSelectList.isEmpty()) {
                chainSelectList = viewModel.chainList.getOrEmpty()
            }

            setFragmentResult(arguments?.getString(KEY_REQUEST) ?: "", bundleOf(DATA_CHAINS to chainSelectList))
            dismiss()
        }
    }


    private fun setupBottomSheet() {

        val onGlobalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            updateLocationAction()
        }

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
                updateLocationAction()
            }
        })

        bottomSheet.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)

    }

    private fun setupRecyclerView() {

        val binding = binding ?: return

        val chooseChainAdapter = ChooseChainAdapter { _, item ->
            viewModel.switchSelect(item)
        }

        adapter = MultiAdapter(chooseChainAdapter).apply {

            setRecyclerView(binding.recyclerView)
        }
    }

    private fun observeData() = with(viewModel) {

        viewItemListDisplay.observe(viewLifecycleOwner) {

            adapter?.submitList(it)
        }

        updateChainIds(arguments?.getStringArrayList(PARAM_CHAIN_ID))
    }

    private fun updateLocationAction() {

        val binding = binding ?: return

        if (binding.root.height <= 0) return

        val rect = Rect()
        bottomSheet.getGlobalVisibleRect(rect)

        if (rect.height() <= 0) return

        val transactionYMin = listOf(binding.vHeader.bottom, binding.tvTitle.bottom).maxOrNull() ?: 20.toPx()
        val transactionYNew = maxOf(rect.height() - binding.vBackgroundAction.height, transactionYMin).toFloat()

        if (binding.vBackgroundAction.translationY == transactionYNew) return

        binding.tvNegative.translationY = transactionYNew
        binding.tvPositive.translationY = transactionYNew
        binding.vBackgroundAction.translationY = transactionYNew
    }

    companion object {

        fun newInstance(keyRequest: String = "", chainIds: List<String>) = ChooseChainFragment().apply {
            arguments = bundleOf(
                KEY_REQUEST to keyRequest,
                PARAM_CHAIN_ID to chainIds
            )
        }
    }
}