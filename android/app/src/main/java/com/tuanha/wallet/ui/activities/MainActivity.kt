package com.tuanha.wallet.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import com.tuanha.coreapp.ui.base.activities.BaseAdsActivity
import com.tuanha.wallet.databinding.ActivityMainBinding

class MainActivity : BaseAdsActivity() {

    override val updateEnable: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.BLACK

        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }
}