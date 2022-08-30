package com.tuanha.wallet.ui.fragments.wallet.adapter

import android.view.View
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.tuanha.coreapp.ui.base.adapters.ViewItemAdapter
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.databinding.ItemChooseWalletBinding
import com.tuanha.wallet.entities.Wallet
import com.tuanha.wallet.entities.getNameOrDefault
import com.tuanha.wallet.ui.fragments.chain.adapter.ChooseChainViewItem
import com.tuanha.wallet.utils.ext.toDrawable

class ChooseWalletAdapter(
    onItemClick: (View, ChooseWalletViewItem) -> Unit = { _, _ -> }
) : ViewItemAdapter<ChooseWalletViewItem, ItemChooseWalletBinding>(onItemClick) {

    override fun bind(binding: ItemChooseWalletBinding, viewType: Int, position: Int, item: ChooseWalletViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(ChooseChainViewItem.PAYLOAD_SELECT)) {
            refreshSelected(binding, item)
        }
    }

    override fun bind(binding: ItemChooseWalletBinding, viewType: Int, position: Int, item: ChooseWalletViewItem) {
        super.bind(binding, viewType, position, item)

        binding.ivWallet.load(item.image, RoundedCorners(ROUND))
        binding.tvWalletName.setText(item.name)
        binding.tvWalletInfo.setText(item.info)

        refreshSelected(binding, item)
    }

    private fun refreshSelected(binding: ItemChooseWalletBinding, item: ChooseWalletViewItem) {

        binding.ivSelection.setInvisible(!item.isSelected)
    }

    companion object {

        val ROUND = 16.toPx()
    }
}

data class ChooseWalletViewItem(
    val data: Wallet,

    var name: Text<*> = TextStr(""),
    var info: Text<*> = TextStr(""),
    var image: Image<*> = ImageStr(""),

    var isSelected: Boolean = false
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh(walletIdList: List<String>) = apply {

        name = data.getNameOrDefault()
        info = data.chainIdAndAddress.values.first().toText()

        image = data.logo.takeIf { it.isNotBlank() }?.toImage() ?: data.chainIdAndAddress.values.first().toDrawable(40.toPx())

        isSelected = data.id in walletIdList
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        data.chainIdAndAddress
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        isSelected to PAYLOAD_SELECT
    )


    companion object {

        const val PAYLOAD_SELECT = "PAYLOAD_SELECT"
    }
}