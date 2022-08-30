package com.tuanha.wallet.ui.fragments.chain.adapter

import android.util.Log
import android.view.View
import com.tuanha.core.utils.extentions.toJson
import com.tuanha.coreapp.ui.base.adapters.ViewItemAdapter
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.databinding.ItemChooseChainBinding
import com.tuanha.wallet.entities.Chain

class ChooseChainAdapter(
    onItemClick: (View, ChooseChainViewItem) -> Unit = { _, _ -> }
) : ViewItemAdapter<ChooseChainViewItem, ItemChooseChainBinding>(onItemClick) {

    override fun bind(binding: ItemChooseChainBinding, viewType: Int, position: Int, item: ChooseChainViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(ChooseChainViewItem.PAYLOAD_SELECT)) {
            refreshSelected(binding, item)
        }
    }

    override fun bind(binding: ItemChooseChainBinding, viewType: Int, position: Int, item: ChooseChainViewItem) {
        super.bind(binding, viewType, position, item)

        binding.ivChain.load(item.image)
        binding.tvChainName.setText(item.name)

        refreshSelected(binding, item)
    }

    private fun refreshSelected(binding: ItemChooseChainBinding, item: ChooseChainViewItem) {

        binding.ivSelection.setVisible(item.isSelected)
    }
}

data class ChooseChainViewItem(
    val data: Chain,

    var name: Text<*> = TextStr(""),
    var image: Image<*> = ImageStr(""),

    var isSelected: Boolean = false
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh(chainIdSelectList: List<String>) = apply {

        name = data.name.toText()
        image = data.logo.toImage()

        isSelected = chainIdSelectList.any { it in data.idStr }
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data.idStr
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to PAYLOAD_SELECT
    )

    companion object {

        const val PAYLOAD_SELECT = "PAYLOAD_SELECT"
    }
}