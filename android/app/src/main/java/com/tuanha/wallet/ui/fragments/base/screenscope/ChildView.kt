package com.tuanha.wallet.ui.fragments.base.screenscope

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.map
import com.tuanha.coreapp.ui.base.fragments.BaseViewModelFragment
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.findParentFirstOrNull

interface ChildView {

    val viewModel: BaseViewModel?

    val parentViewModel: BaseViewModel?
        get() = null

    fun observeParentData() {

        val parentViewModel = parentViewModel
            ?: ((this as? Fragment)?.findParentFirstOrNull<BaseViewModelFragment<*, *>>() as? BaseViewModelFragment<*, *>)?.viewModel as? BaseViewModel
//            ?: (this as? MainActivity)?.viewModel // need convert to base activity
            ?: return

        val viewLifecycleOwner: LifecycleOwner = when (this@ChildView) {
            is Fragment -> {
                this@ChildView.viewLifecycleOwner
            }
            is Activity -> {
                this@ChildView as LifecycleOwner
            }
            else -> {
                throw RuntimeException("")
            }
        }

        parentViewModel.uiReady.map { it }.observe(viewLifecycleOwner) {

            viewModel?.updateUiReady(it)
        }
    }
}