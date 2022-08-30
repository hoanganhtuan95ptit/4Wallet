package com.tuanha.wallet.di

import com.tuanha.wallet.ui.activities.MainViewModel
import com.tuanha.wallet.ui.fragments.chain.ChooseChainViewModel
import com.tuanha.wallet.ui.fragments.currency.ChooseCurrencyViewModel
import com.tuanha.wallet.ui.fragments.home.overview.OverviewViewModel
import com.tuanha.wallet.ui.fragments.home.overview.asset.token.OverviewAssetTokenViewModel
import com.tuanha.wallet.ui.fragments.wallet.ChooseWalletViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@JvmField
val viewModelModule = module {

    viewModel {
        MainViewModel(get())
    }

    viewModel {
        OverviewViewModel(get(), get())
    }

    viewModel {
        OverviewAssetTokenViewModel(get())
    }

    viewModel {
        ChooseChainViewModel(get())
    }

    viewModel {
        ChooseWalletViewModel(get())
    }

    viewModel {
        ChooseCurrencyViewModel()
    }
}
