package com.tuanha.wallet.ui.fragments.token.adapter

import android.view.View
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.tuanha.coreapp.ui.base.adapters.ViewItemAdapter
import com.tuanha.coreapp.ui.base.adapters.ViewItemCloneable
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.databinding.ItemTokenBinding
import com.tuanha.wallet.entities.Chain
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.entities.Token
import com.tuanha.wallet.entities.toDisplay
import com.tuanha.wallet.utils.ext.FormatNumberType
import com.tuanha.wallet.utils.ext.toBigDecimalOrZero
import com.tuanha.wallet.utils.ext.toDisplay
import java.math.BigDecimal

class TokenAdapter(
    onItemClick: (View, TokenViewItem) -> Unit = { _, _ -> }
) : ViewItemAdapter<TokenViewItem, ItemTokenBinding>(onItemClick) {

    override fun bind(binding: ItemTokenBinding, viewType: Int, position: Int, item: TokenViewItem, payloads: MutableList<Any>) {
        super.bind(binding, viewType, position, item, payloads)

        if (payloads.contains(TokenViewItem.PAYLOAD_AMOUNT)) {
            refreshAmount(binding, item)
        }

        if (payloads.contains(TokenViewItem.PAYLOAD_BALANCE)) {
            refreshBalance(binding, item)
        }

        if (payloads.contains(TokenViewItem.PAYLOAD_PERCENT)) {
            refreshPercent(binding, item)
        }
    }

    override fun bind(binding: ItemTokenBinding, viewType: Int, position: Int, item: TokenViewItem) {
        super.bind(binding, viewType, position, item)

        binding.tvToken.setText(item.name)
        binding.ivToken.load(item.image, CircleCrop())

        refreshAmount(binding, item)
        refreshBalance(binding, item)
        refreshPercent(binding, item)
    }

    private fun refreshAmount(binding: ItemTokenBinding, item: TokenViewItem) {

        binding.tvAmount.setText(item.amountDisplay)
    }

    private fun refreshBalance(binding: ItemTokenBinding, item: TokenViewItem) {

        binding.tvBalance.setText(item.balanceDisplay)
    }

    private fun refreshPercent(binding: ItemTokenBinding, item: TokenViewItem) {

        binding.tvPercent.isSelected = item.percent > BigDecimal.ZERO
        binding.tvPercent.setText(item.percentDisplay)
    }
}

data class TokenViewItem(
    var data: Token,

    var name: String = "",

    var image: String = "",

    var symbol: String = "",

    var amount: BigDecimal = BigDecimal.ZERO,
    var amountDisplay: Text<*> = TextStr(""),

    var balance: BigDecimal = BigDecimal.ZERO,
    var balanceDisplay: Text<*> = TextStr(""),

    var percent: BigDecimal = BigDecimal.ZERO,
    var percentDisplay: Text<*> = TextStr("")
) : ViewItemCloneable {

    override fun clone() = copy()

    fun refresh(listChain: List<Chain>, currency: MarketCurrency) = apply {

        name = data.symbol

        image = data.logo

        symbol = data.symbol


        balance = data.totalBalance

        balanceDisplay = (balance.toDisplay(FormatNumberType.BALANCE)).toText()


        amount = balance.multiply(data.currencyAndPrice[currency]?.price.toBigDecimalOrZero())

        amountDisplay = amount.toDisplay(currency)


        percent = data.price24hChangePercent.toBigDecimalOrZero()
        percentDisplay = (percent.abs().toDisplay(FormatNumberType.PERCENTAGE) + "%").toText()
    }

    override fun areItemsTheSame(): List<Any> = listOf(
        symbol
    )

    override fun getContentsCompare(): List<Pair<Any, String>> = listOf(
        amountDisplay to PAYLOAD_AMOUNT,
        balanceDisplay to PAYLOAD_BALANCE,
        percentDisplay to PAYLOAD_PERCENT
    )

    companion object {
        const val PAYLOAD_AMOUNT = "PAYLOAD_AMOUNT"
        const val PAYLOAD_BALANCE = "PAYLOAD_BALANCE"
        const val PAYLOAD_PERCENT = "PAYLOAD_PERCENT"
    }
}