package com.tuanha.wallet.ui.fragments.wallet

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tuanha.coreapp.data.usecase.ResultState
import com.tuanha.coreapp.data.usecase.doStart
import com.tuanha.coreapp.data.usecase.doSuccess
import com.tuanha.coreapp.data.usecase.isStart
import com.tuanha.coreapp.ui.base.adapters.LoadingViewItem
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.ui.viewmodels.BaseViewModel
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.R
import com.tuanha.wallet.data.usecases.wallet.GetWalletUseCase
import com.tuanha.wallet.entities.Wallet
import com.tuanha.wallet.ui.fragments.wallet.adapter.ChooseWalletViewItem

class ChooseWalletViewModel(
    private val getWalletUseCase: GetWalletUseCase
) : BaseViewModel() {

    private val itemSearchLoading = listOf(
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
    )


    @VisibleForTesting
    val walletSelectIdList: LiveData<List<String>> = MediatorLiveData<List<String>>().apply {
        postValue(emptyList())
    }


    @VisibleForTesting
    val walletState: LiveData<ResultState<List<Wallet>>> = liveData<ResultState<List<Wallet>>> {

        postDifferentValue(ResultState.Start)

        getWalletUseCase.execute().collect {

            postValue(ResultState.Success(it))
        }
    }.apply {

        postDifferentValue(ResultState.Start)
    }

    val walletList: LiveData<List<Wallet>> = combineSources(walletState) {

        val state = walletState.get()

        state.doStart {
            postValue(emptyList())
        }

        state.doSuccess {
            postValue(it)
        }
    }

    @VisibleForTesting
    val walletViewItemList: LiveData<List<ViewItemCloneable>> = combineSources(walletList, walletSelectIdList) {

        val state = walletState.get()
        val walletList = walletList.get()
        val walletIdList = walletSelectIdList.getOrEmpty()

        if (state.isStart()) {
            postValue(itemSearchLoading)
            return@combineSources
        }

        val list = arrayListOf<ViewItemCloneable>()

        walletList.map {

            ChooseWalletViewItem(it).refresh(walletIdList)
        }.let {

            list.addAll(it)
        }

        postValue(list)
    }

    val viewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(walletViewItemList) {

        walletViewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postValue(it)
        }
    }

    fun switchSelect(item: ChooseWalletViewItem) {

        val walletSelectIdList = walletSelectIdList.getOrEmpty().toMutableList()

        walletSelectIdList.remove(item.data.id)

        if (!item.isSelected) {

            walletSelectIdList.add(item.data.id)
        }

        this.walletSelectIdList.postValue(walletSelectIdList)
    }

    fun updateWalletIds(list: ArrayList<String>?) {

        walletSelectIdList.postValue(list ?: emptyList())
    }
}