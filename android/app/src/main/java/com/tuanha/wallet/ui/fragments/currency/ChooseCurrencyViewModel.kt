package com.tuanha.wallet.ui.fragments.currency

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.ui.fragments.currency.adapter.ChooseCurrencyViewItem

class ChooseCurrencyViewModel : BaseViewModel() {


    @VisibleForTesting
    val currencySelectIdList: LiveData<List<MarketCurrency>> = MediatorLiveData<List<MarketCurrency>>().apply {
        value = emptyList()
    }

    val currencyList: LiveData<List<MarketCurrency>> = liveData {

        postDifferentValue(MarketCurrency.values().toList())
    }

    @VisibleForTesting
    val currencyViewItemList: LiveData<List<ViewItemCloneable>> = combineSources(currencyList, currencySelectIdList) {

        val chainList = currencyList.get()
        val chainIdSelectList = currencySelectIdList.getOrEmpty()


        val list = arrayListOf<ViewItemCloneable>()

        chainList.map {

            ChooseCurrencyViewItem(it).refresh(chainIdSelectList)
        }.let {

            list.addAll(it)
        }

        postValue(list)
    }

    val viewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(currencyViewItemList) {

        currencyViewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postValue(it)
        }
    }

    fun updateChainIds(ids: List<MarketCurrency>?) {

        currencySelectIdList.postValue(ids ?: emptyList())
    }

}