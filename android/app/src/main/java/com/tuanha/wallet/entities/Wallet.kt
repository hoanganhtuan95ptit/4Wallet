package com.tuanha.wallet.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.tuanha.coreapp.utils.extentions.*
import com.tuanha.wallet.R
import com.tuanha.wallet.utils.ext.toDrawable
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.ConcurrentHashMap

@Keep
@Parcelize
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Wallet(
    val id: String = "",

    val name: String = "",

    val logo: String = ""
) : Parcelable {

    @JsonProperty("chainIdAndAddress")
    var chainIdAndAddress: Map<String, String> = ConcurrentHashMap()

}


fun Wallet.getNameOrDefault(): Text<*> = name.takeIf { it.isNotBlank() }?.toText() ?: TextRes(R.string.wallet_name_default)

fun Wallet.getLogoOrDefault(): Image<*> = logo.takeIf { it.isNotBlank() }?.toImage() ?: chainIdAndAddress.values.first().toDrawable(40.toPx())


fun Wallet.getAddress(chain: Chain?) = getAddress(chain?.idStr)

fun Wallet.getAddress(chainId: String?) = if (chainId.isNullOrBlank()) null else chainIdAndAddress[chainId]

