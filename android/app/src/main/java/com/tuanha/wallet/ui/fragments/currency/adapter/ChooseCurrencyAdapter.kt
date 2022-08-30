package com.tuanha.wallet.ui.fragments.currency.adapter

import android.view.View
import com.tuanha.coreapp.ui.base.adapters.ViewItemAdapter
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.databinding.ItemChooseCurrencyBinding
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.ui.fragments.chain.adapter.ChooseChainViewItem

class ChooseCurrencyAdapter(
    onItemClick: (View, ChooseCurrencyViewItem) -> Unit = { _, _ -> }
) : ViewItemAdapter<ChooseCurrencyViewItem, ItemChooseCurrencyBinding>(onItemClick) {

    override fun bind(binding: ItemChooseCurrencyBinding, viewType: Int, position: Int, item: ChooseCurrencyViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(ChooseChainViewItem.PAYLOAD_SELECT)) {
            refreshSelected(binding, item)
        }
    }

    override fun bind(binding: ItemChooseCurrencyBinding, viewType: Int, position: Int, item: ChooseCurrencyViewItem) {
        super.bind(binding, viewType, position, item)

        binding.ivWallet.load(item.image)
        binding.tvWalletName.setText(item.name)

        refreshSelected(binding, item)
    }

    private fun refreshSelected(binding: ItemChooseCurrencyBinding, item: ChooseCurrencyViewItem) {

        binding.ivSelection.setVisible(item.isSelected)
    }
}

data class ChooseCurrencyViewItem(
    val data: MarketCurrency,

    var name: Text<*> = TextStr(""),
    var image: Image<*> = ImageStr(""),

    var isSelected: Boolean = false
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh(chainIdSelectList: List<MarketCurrency>) = apply {

        name = data.value.toText()
        image = data.imageRes.toImage()

        isSelected = chainIdSelectList.any { it.value == data.value }
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data.value
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to PAYLOAD_SELECT
    )

    companion object {

        const val PAYLOAD_SELECT = "PAYLOAD_SELECT"
    }
}