package com.tuanha.wallet.ui.fragments.home.overview.asset.token

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tuanha.coreapp.data.usecase.*
import com.tuanha.coreapp.ui.base.adapters.LoadingViewItem
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.R
import com.tuanha.wallet.data.usecases.GetTokenAssetUseCase
import com.tuanha.wallet.entities.*
import com.tuanha.wallet.ui.fragments.token.adapter.TokenViewItem

class OverviewAssetTokenViewModel(
    private val getTokenAssetUseCase: GetTokenAssetUseCase,
) : BaseViewModel() {

    private val itemSearchLoading = listOf(
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
    )

    @VisibleForTesting
    val currency: LiveData<MarketCurrency> = MediatorLiveData()

    @VisibleForTesting
    val listChain: LiveData<List<Chain>> = MediatorLiveData()

    @VisibleForTesting
    val listWallet: LiveData<List<Wallet>> = MediatorLiveData()


    @VisibleForTesting
    val tokenState: LiveData<ResultState<List<Token>>> = combineSources<ResultState<List<Token>>>(listChain, listWallet) {

        postDifferentValue(ResultState.Start)

        getTokenAssetUseCase.execute(GetTokenAssetUseCase.Param(listChain.getOrEmpty(), listWallet.getOrEmpty())).collect {

            postValue(ResultState.Success(it))
        }
    }.apply {

        postDifferentValue(ResultState.Start)
    }

    @VisibleForTesting
    val listToken: LiveData<List<Token>> = combineSources(tokenState) {

        val state = tokenState.get()

        state.doStart {
            postValue(emptyList())
        }

        state.doSuccess {
            postValue(it)
        }

        state.doFailed {
            postValue(emptyList())
        }
    }

    @VisibleForTesting
    val listTokenViewItem: LiveData<List<ViewItemCloneable>> = combineSources(listToken, listChain, currency) {

        listToken.getOrEmpty().map {

            TokenViewItem(it).refresh(listChain.getOrEmpty(), currency.get())
        }.toMutableList().apply {

            sortBy { token -> token.symbol }
            sortByDescending { token -> token.amount }
        }.let {

            postValue(it)
        }
    }

    val listTokenViewItemDisplay: LiveData<List<ViewItemCloneable>> = combineSources(listTokenViewItem) {

        val state = tokenState.get()

        if (state.isStart()) {

            postDifferentValue(itemSearchLoading)
            return@combineSources
        }


        listTokenViewItem.getOrEmpty().map {
            it.clone()
        }.let {
            postValue(it)
        }
    }


    val totalBalance: LiveData<Text<*>> = combineSources(listTokenViewItem, currency) {

        postDifferentValue(listTokenViewItem.getOrEmpty().filterIsInstance<TokenViewItem>().sumOf { it.amount }.toDisplay(currency.get()))
    }


    fun updateChains(list: List<Chain>?) {

        this.listChain.postDifferentValue(list ?: emptyList())
    }

    fun updateWallets(list: List<Wallet>?) {

        this.listWallet.postDifferentValue(list ?: emptyList())
    }

    fun updateCurrency(currency: MarketCurrency) {

        this.currency.postDifferentValue(currency)
    }
}