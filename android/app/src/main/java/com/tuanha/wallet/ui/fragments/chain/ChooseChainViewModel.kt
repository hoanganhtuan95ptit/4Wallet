package com.tuanha.wallet.ui.fragments.chain

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
import com.tuanha.wallet.data.usecases.chain.GetChainUseCase
import com.tuanha.wallet.entities.Chain
import com.tuanha.wallet.ui.fragments.chain.adapter.ChooseChainViewItem

class ChooseChainViewModel(
    private val getChainUseCase: GetChainUseCase
) : BaseViewModel() {

    private val itemSearchLoading = listOf(
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
        LoadingViewItem(R.layout.item_token_loading),
    )


    @VisibleForTesting
    val chainSelectIdList: LiveData<List<String>> = MediatorLiveData<List<String>>().apply {
        value = emptyList()
    }


    @VisibleForTesting
    val chainState: LiveData<ResultState<List<Chain>>> = liveData<ResultState<List<Chain>>> {

        postDifferentValue(ResultState.Start)

        getChainUseCase.execute().collect {

            postValue(ResultState.Success(it))
        }
    }.apply {

        postDifferentValue(ResultState.Start)
    }

    val chainList: LiveData<List<Chain>> = combineSources(chainState) {

        val state = chainState.get()

        state.doStart {
            postValue(emptyList())
        }

        state.doSuccess {
            postValue(it)
        }
    }

    @VisibleForTesting
    val chainViewItemList: LiveData<List<ViewItemCloneable>> = combineSources(chainList, chainSelectIdList) {

        val state = chainState.get()
        val chainList = chainList.get()
        val chainIdSelectList = chainSelectIdList.getOrEmpty()

        if (state.isStart()) {
            postValue(itemSearchLoading)
            return@combineSources
        }

        val list = arrayListOf<ViewItemCloneable>()

        chainList.map {

            ChooseChainViewItem(it).refresh(chainIdSelectList)
        }.let {

            list.addAll(it)
        }

        postValue(list)
    }

    val viewItemListDisplay: LiveData<List<ViewItemCloneable>> = combineSources(chainViewItemList) {

        chainViewItemList.getOrEmpty().map {
            it.clone()
        }.let {
            postValue(it)
        }
    }

    fun switchSelect(item: ChooseChainViewItem) {

        val chainSelectIdList = chainSelectIdList.getOrEmpty().toMutableList()

        chainSelectIdList.remove(item.data.idStr)

        if (!item.isSelected) {

            chainSelectIdList.add(item.data.idStr)
        }

        this.chainSelectIdList.postValue(chainSelectIdList)
    }

    fun updateChainIds(ids: ArrayList<String>?) {

        chainSelectIdList.postValue(ids ?: emptyList())
    }

}