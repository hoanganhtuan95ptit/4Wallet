package com.tuanha.wallet.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal
import java.math.MathContext
import java.util.concurrent.ConcurrentHashMap

@Keep
@Parcelize
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Token constructor(
    var id: String = "",

    var name: String = "",
    var symbol: String = "",

    var image: ObjectNode? = null,
    var logo: String = "",
) : Parcelable {

    @JsonProperty("address", access = JsonProperty.Access.WRITE_ONLY)
    var addressInChain: String? = null

    @JsonProperty("decimals", access = JsonProperty.Access.WRITE_ONLY)
    var decimalsInChain: Int? = null

    @JsonProperty("price_change_percentage_24h_in_currency", access = JsonProperty.Access.WRITE_ONLY)
    var price24hChangePercent: String = ""


    @JsonProperty("chainIdAndAddress")
    var chainIdAndAddress: Map<String, String> = ConcurrentHashMap()

    @JsonProperty("chainIdAndDecimals")
    var chainIdAndDecimals: Map<String, Int> = ConcurrentHashMap()


    @JsonIgnore
    var currencyAndPrice: Map<MarketCurrency, MarketPrice> = emptyMap()

    @JsonIgnore
    var chainIdAndBalance: Map<String, BigDecimal> = ConcurrentHashMap()

    @JsonIgnore
    var chainIdAndAddressAndBalance: ConcurrentHashMap<String, BigDecimal> = ConcurrentHashMap()


    val totalBalance: BigDecimal
        @JsonIgnore
        get() = chainIdAndBalance.map {

            chainIdAndDecimals[it.key]?.let { decimals -> it.value.divide(BigDecimal.TEN.pow(decimals), MathContext.DECIMAL128) } ?: BigDecimal.ZERO
        }.sumOf {

            it
        }
}


fun Token.getAddress(chain: Chain?) = getAddress(chain?.idStr)

fun Token.getAddress(chainId: String?) = if (chainId.isNullOrBlank()) null else chainIdAndAddress[chainId]


fun Token.getBalance(chain: Chain?) = getBalance(chain?.idStr)

fun Token.getBalance(chainId: String?) = if (chainId.isNullOrBlank()) null else chainIdAndBalance[chainId]


fun Token.getDecimals(chain: Chain?) = getDecimals(chain?.idStr)

fun Token.getDecimals(chainId: String?) = if (chainId.isNullOrBlank()) null else chainIdAndDecimals[chainId]
