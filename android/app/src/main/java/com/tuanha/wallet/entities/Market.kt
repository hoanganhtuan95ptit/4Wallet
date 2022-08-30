package com.tuanha.wallet.entities

import com.tuanha.coreapp.utils.extentions.Text
import com.tuanha.coreapp.utils.extentions.toText
import com.tuanha.wallet.R
import com.tuanha.wallet.utils.ext.FormatNumberType
import com.tuanha.wallet.utils.ext.toBigDecimalOrZero
import com.tuanha.wallet.utils.ext.toDisplay
import java.math.BigDecimal

private const val SPACE_REPLACE = "******"


data class MarketPrice(
    val price: String = "",

    val price24hVol: String = "",
    val price24hChange: String = "",
)

enum class MarketCurrency(val value: String, val round: Int = 9, val formatStr: String = "", val imageRes: Int = 0) {
    USD("usd", 4, formatStr = "$${SPACE_REPLACE}", imageRes = R.drawable.img_dola_background),
    BTC("btc", 9, formatStr = "₿${SPACE_REPLACE}", imageRes = R.drawable.img_bitcoin_background),
    ETH("eth", 9, formatStr = "Ξ${SPACE_REPLACE}", imageRes = R.drawable.img_etherum_background),
    BNB("bnb", 9, formatStr = "${SPACE_REPLACE}BNB", imageRes = R.drawable.img_bnb_background)
}


fun String.toCurrency() = MarketCurrency.values().toList().let { list ->

    list.firstOrNull { it.value == this } ?: list.first()
}

fun List<String>.toCurrencies() = map { it.toCurrency() }

fun MarketCurrency.toString() = value

fun List<MarketCurrency>.toStrings() = map { it.value }

fun String.toDisplay(currency: MarketCurrency): Text<*> {

    return toBigDecimalOrZero().toDisplay(currency)
}

fun BigDecimal.toDisplay(currency: MarketCurrency): Text<*> {

    val type = FormatNumberType.VALUE.apply {
        round = currency.round
    }

    return currency.formatStr.replace(SPACE_REPLACE, toDisplay(type)).toText()
}



