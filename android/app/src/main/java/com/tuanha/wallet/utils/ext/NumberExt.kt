@file:Suppress("NAME_SHADOWING", "SpellCheckingInspection")

package com.tuanha.wallet.utils.ext

import com.tuanha.wallet.entities.MarketCurrency
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat


private val K__ by lazy { 1000.toBigDecimal() }
private val M__ by lazy { 1000000.toBigDecimal() }
private val B__ by lazy { 1000000000.toBigDecimal() }
private val T__ by lazy { 1000000000000.toBigDecimal() }
private val QUA by lazy { 1000000000000000.toBigDecimal() }
private val QUI by lazy { 1000000000000000000.toBigDecimal() }

/**
 * tìm vị trí có nghĩa
 * @param lenght độ dài có nghĩa
 */
private fun BigDecimal.findIndexMean(lenght: Int): Int {

    val decimal = subtract(setScale(0, RoundingMode.FLOOR))

    if (decimal == BigDecimal.ZERO) {
        return 0
    }

    var text = decimal.toPlainString()
    text = text.substring(2, kotlin.math.min(text.length, 18))

    var index = text.length
    var count = 0
    var hasMean = false

    // tìm vị trí có nghĩa
    for (i in text.indices) {

        if (text[i].code != 48 && !hasMean) {
            hasMean = true
        }

        if (hasMean) {
            count++
        }

        if (count == lenght) {
            index = i + 1
            break
        }
    }

    // làm tròn giá trị
    for (i in index - 1 downTo 0) {

        if (i == 0 && text[i].code == 48) {
            return 0
        } else if (text[i].code != 48) {
            return i + 1
        }
    }

    return index
}

/**
 * format number
 */
private fun BigDecimal.toDisplay(afterCommaLength: Int? = null): String {

    val afterCommaLength = afterCommaLength ?: findIndexMean(100)

    val decimalFormat = if (afterCommaLength <= 0) {

        DecimalFormat("#,##0")
    } else {

        var end = ""

        for (i in 1..afterCommaLength) {
            end += "0"
        }

        DecimalFormat("#,##0.$end")
    }

    return decimalFormat.format(this)
}

fun String.toDisplay(type: FormatNumberType): String {

    return toBigDecimalOrZero().toDisplay(type)
}

fun BigDecimal.toDisplay(type: FormatNumberType): String = when {

    this > QUI -> {
        this.divide(QUA, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "q"
    }
    this > QUA -> {
        this.divide(T__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "t"
    }
    this > T__ -> {
        this.divide(B__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "B"
    }
    this > B__ -> {
        this.divide(M__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "M"
    }
    type == FormatNumberType.BALANCE -> {
        setScale(9, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE -> {
        setScale(type.round, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_USD -> {
        setScale(4, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_BTC -> {
        setScale(8, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_ETH -> {
        setScale(7, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_BNB -> {
        setScale(6, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_MATIC -> {
        setScale(3, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_AVAX -> {
        setScale(5, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_FTM -> {
        setScale(3, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_CRO -> {
        setScale(3, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.VALUE_KLAY -> {
        setScale(3, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.PERCENTAGE -> {
        setScale(2, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.RATE -> {
        setScale(kotlin.math.min(findIndexMean(3), 1000), BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.SLIPPAGE -> {
        setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.MIN_RECEIVED -> {
        setScale(6, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.GAS_FEE && this < BigDecimal.ONE -> {
        setScale(findIndexMean(3), BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type == FormatNumberType.GAS_FEE && this > BigDecimal.ONE -> {
        setScale(2, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type in listOf(FormatNumberType.VALUE_2, FormatNumberType.REALTIME_PRICE, FormatNumberType.ALL_TIME_HIGH, FormatNumberType.ALL_TIME_LOW) && this < BigDecimal.ONE -> {
        setScale(kotlin.math.min(findIndexMean(3), 8), BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type in listOf(FormatNumberType.VALUE_2, FormatNumberType.REALTIME_PRICE, FormatNumberType.ALL_TIME_HIGH, FormatNumberType.ALL_TIME_LOW) && this > BigDecimal.ONE -> {
        setScale(2, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < BigDecimal.ONE -> {
        setScale(kotlin.math.min(findIndexMean(1), 8), BigDecimal.ROUND_HALF_UP).toDisplay()
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < K__ -> {
        setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1)
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < M__ -> {
        this.divide(K__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "K"
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < B__ -> {
        this.divide(M__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "M"
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < T__ -> {
        this.divide(B__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "B"
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < QUA -> {
        this.divide(T__, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "t"
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) && this < QUI -> {
        this.divide(QUA, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "q"
    }
    type in listOf(FormatNumberType.MARKET_CAP, FormatNumberType.VOLUME, FormatNumberType.TOTAL_SUPPLY, FormatNumberType.LIQUIDITY) -> {
        this.divide(QUI, MathContext.DECIMAL128).setScale(1, BigDecimal.ROUND_HALF_UP).toDisplay(1) + "Q"
    }
    else -> {
        setScale(4, BigDecimal.ROUND_HALF_UP).toDisplay()
    }
}

fun String?.toDisplayOrZero(type: FormatNumberType): String = this?.toDisplay(type) ?: "0"

fun BigDecimal?.toDisplayOrZero(type: FormatNumberType): String = this?.toDisplay(type) ?: "0"

fun String?.toDisplayOrDefault(type: FormatNumberType, default: String): String = this?.toDisplay(type) ?: default

fun BigDecimal?.toDisplayOrDefault(type: FormatNumberType, default: String): String = this?.toDisplay(type) ?: default

enum class FormatNumberType(var round: Int = 0) {
    BALANCE,
    VALUE,
    VALUE_USD,
    VALUE_BTC,
    VALUE_ETH,
    VALUE_BNB,
    VALUE_MATIC,
    VALUE_AVAX,
    VALUE_FTM,
    VALUE_CRO,
    VALUE_KLAY,
    VALUE_2,
    REALTIME_PRICE,
    ALL_TIME_HIGH,
    ALL_TIME_LOW,

    MARKET_CAP,
    VOLUME,
    TOTAL_SUPPLY,
    LIQUIDITY,

    PERCENTAGE,
    RATE,
    SLIPPAGE,
    MIN_RECEIVED,
    GAS_FEE
}
