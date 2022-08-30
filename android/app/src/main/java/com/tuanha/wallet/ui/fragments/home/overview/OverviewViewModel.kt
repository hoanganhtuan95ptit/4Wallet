package com.tuanha.wallet.ui.fragments.home.overview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tuanha.coreapp.ui.base.adapters.LoadingViewItem
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.*
import com.tuanha.wallet.data.usecases.chain.GetChainSelectUseCase
import com.tuanha.wallet.data.usecases.wallet.GetWalletSelectUseCase
import com.tuanha.wallet.entities.*
import com.tuanha.wallet.ui.fragments.token.adapter.ActionViewItem

class OverviewViewModel(
    private val getChainSelectUseCase: GetChainSelectUseCase,
    private val getWalletSelectUseCase: GetWalletSelectUseCase
) : BaseViewModel() {

    private val itemSearchLoading = listOf(
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
    )

    val chainList: LiveData<List<Chain>> = liveData {

        getChainSelectUseCase.execute().collect {

            postValue(it)
        }
    }

    val chainDisplay: LiveData<Pair<Image<*>, Text<*>>> = combineSources(chainList) {

        val listChain = chainList.getOrEmpty()

        if (listChain.size == 1) {

            Pair(listChain.first().logo.toImage(), listChain.first().name.toText())
        } else {

            Pair(R.drawable.ic_mutil_chain_accent_24dp.toImage(), R.string.multi_chain.toText())
        }.let {

            postValue(it)
        }
    }

    val walletList: LiveData<List<Wallet>> = liveData {

        getWalletSelectUseCase.execute().collect {

            postValue(it)
        }
    }

    val walletDisplay: LiveData<Pair<Image<*>, Text<*>>> = combineSources(walletList) {

        val listWallet = walletList.getOrEmpty()

        if (listWallet.size == 1) {

            Pair(listWallet.first().getLogoOrDefault(), listWallet.first().getNameOrDefault())
        } else {

            Pair(R.drawable.ic_wallet_on_primary_24dp.toImage(), R.string.multi_wallet.toText())
        }.let {

            postValue(it)
        }
    }

    val currency: LiveData<MarketCurrency> = MediatorLiveData<MarketCurrency>().apply {
        postValue(MarketCurrency.BTC)
    }


    val actionViewItemList: LiveData<List<ActionViewItem>> = MediatorLiveData<List<ActionViewItem>>().apply {

        listOf(
            ActionViewItem(ID_TRANSFER, R.string.transfer.toText(), R.drawable.ic_transfer_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_SWAP, R.string.swap.toText(), R.drawable.ic_swap_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_MULTI_SEND, R.string.multi_send.toText(), R.drawable.ic_multi_send_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_BRIDGE, R.string.bridge.toText(), R.drawable.ic_bridge_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_D_APP, R.string.d_app.toText(), R.drawable.ic_d_app_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_BUY_CRYPTO, R.string.buy_crypto.toText(), R.drawable.ic_buy_crypto_on_primary_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_HISTORY, R.string.history.toText(), R.drawable.ic_history_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
            ActionViewItem(ID_RECEIVE, R.string.receive.toText(), R.drawable.ic_receive_on_surface_24dp.toImage(), R.drawable.bg_round_12_surface),
        ).let {
            postValue(it)
        }
    }

    val actionViewItemListDisplay: LiveData<List<ActionViewItem>> = combineSources(actionViewItemList) {

        actionViewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postValue(it)
        }
    }


    fun updateChains(list: ArrayList<Chain>?) {
        chainList.postValue(list ?: emptyList())
    }

    fun updateWallets(list: ArrayList<Wallet>?) {
        walletList.postValue(list ?: emptyList())
    }

    fun updateCurrency(cur: MarketCurrency) {

        currency.postValue(cur)
    }
}