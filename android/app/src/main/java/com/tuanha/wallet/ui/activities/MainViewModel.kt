package com.tuanha.wallet.ui.activities

import androidx.lifecycle.LiveData
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.liveData
import com.tuanha.wallet.data.usecases.SyncMarketPriceUseCase
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.entities.MarketPrice

class MainViewModel(
    private val syncMarketPriceUseCase: SyncMarketPriceUseCase
) : BaseViewModel() {

    val marketPrice: LiveData<Map<String, Map<MarketCurrency, MarketPrice>>> = liveData {

    }
}