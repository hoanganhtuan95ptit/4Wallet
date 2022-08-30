package com.tuanha.wallet.ui.fragments.wallet

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tuanha.core.utils.extentions.toArrayList
import com.tuanha.coreapp.ui.base.adapters.MultiAdapter
import com.tuanha.coreapp.ui.base.dialogs.BaseViewModelSheetFragment
import com.tuanha.coreapp.utils.autoCleared
import com.tuanha.coreapp.utils.extentions.getOrEmpty
import com.tuanha.coreapp.utils.extentions.setDebouncedClickListener
import com.tuanha.coreapp.utils.extentions.toPx
import com.tuanha.wallet.*
import com.tuanha.wallet.databinding.FragmentListBinding
import com.tuanha.wallet.databinding.FragmentListChooseBinding
import com.tuanha.wallet.ui.fragments.wallet.adapter.ChooseWalletAdapter
import com.tuanha.wallet.ui.fragments.wallet.adapter.ChooseWalletViewItem

class ChooseWalletFragment : BaseViewModelSheetFragment<FragmentListChooseBinding, ChooseWalletViewModel>(R.layout.fragment_list_choose) {

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

        val binding = binding?:return

        binding.tvTitle.setText(R.string.title_choose_wallet)
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

            var walletSelectList = viewModel.viewItemListDisplay.getOrEmpty().filterIsInstance<ChooseWalletViewItem>().filter { it.isSelected }.map { it.data }

            if (walletSelectList.isEmpty()) {
                walletSelectList = viewModel.walletList.getOrEmpty().toArrayList()
            }

            setFragmentResult(arguments?.getString(KEY_REQUEST) ?: "", bundleOf(DATA_WALLETS to walletSelectList))
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

        val chooseWalletAdapter = ChooseWalletAdapter{ _, item ->
            viewModel.switchSelect(item)
        }

        adapter = MultiAdapter(chooseWalletAdapter).apply {

            setRecyclerView(binding.recyclerView)
        }
    }

    private fun observeData() = with(viewModel) {

        viewItemListDisplay.observe(viewLifecycleOwner) {

            adapter?.submitList(it)
        }

        updateWalletIds(arguments?.getStringArrayList(PARAM_WALLET_ID))
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

        fun newInstance(keyRequest: String = "", walletIds: List<String>) = ChooseWalletFragment().apply {
            arguments = bundleOf(
                KEY_REQUEST to keyRequest,
                PARAM_WALLET_ID to walletIds
            )
        }
    }
}