package com.tuanha.wallet.ui.fragments.base.screenscope

import android.app.Activity
import com.tuanha.wallet.ui.fragments.base.screenscope.ChildView

interface ScreenView : ChildView {

    override fun observeParentData() {

        super.observeParentData()

        if (this is Activity) {

            viewModel?.updateUiReady(true)
        }
    }
}