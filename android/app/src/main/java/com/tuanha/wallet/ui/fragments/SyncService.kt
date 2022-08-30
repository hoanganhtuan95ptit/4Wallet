package com.tuanha.wallet.ui.fragments

import com.tuanha.coreapp.ui.servicer.BaseForegroundService
import com.tuanha.coreapp.utils.extentions.serviceScope
import com.tuanha.wallet.data.usecases.SyncMarketPriceUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SyncService : BaseForegroundService() {

    private val syncMarketPriceUseCase: SyncMarketPriceUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch(handler + Dispatchers.IO) {

            syncMarketPriceUseCase.execute().collect {

            }
        }
    }
}