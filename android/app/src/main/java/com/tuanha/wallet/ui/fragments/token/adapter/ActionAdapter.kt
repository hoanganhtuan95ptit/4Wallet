package com.tuanha.wallet.ui.fragments.token.adapter

import android.view.View
import android.view.ViewGroup
import com.tuanha.coreapp.ui.base.adapters.ViewItemAdapter
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.databinding.ItemActionBinding

class ActionAdapter(
    private val itemPadding: Int = 0,
    private val onItemClick: (View, ActionViewItem) -> Unit = { _, _ -> }
) : ViewItemAdapter<ActionViewItem, ItemActionBinding>(onItemClick) {

    override fun createViewItem(parent: ViewGroup): ItemActionBinding {
        val binding = super.createViewItem(parent)

        binding.root.updateMargin(left = itemPadding, top = itemPadding, right = itemPadding, bottom = itemPadding)

        return binding
    }

    override fun bind(binding: ItemActionBinding, viewType: Int, position: Int, item: ActionViewItem) {
        super.bind(binding, viewType, position, item)

        binding.tvAction.setText(item.name)

        binding.ivAction.load(item.image)

        binding.root.setBackgroundResource(item.background)
    }
}

data class ActionViewItem(
    var id: String,

    var name: Text<*> = "".toText(),

    var image: Image<*> = "".toImage(),

    var background: Int = 0,
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh() = apply {

    }

    override fun areItemsTheSame(): List<Any> = listOf(
        id
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        name to PAYLOAD_NAME,
        image to PAYLOAD_IMAGE,
        background to PAYLOAD_BACKGROUND
    )

    companion object {
        const val PAYLOAD_NAME = "PAYLOAD_NAME"
        const val PAYLOAD_IMAGE = "PAYLOAD_IMAGE"
        const val PAYLOAD_BACKGROUND = "PAYLOAD_BACKGROUND"
    }
}