package com.tuanha.wallet.data.api

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import com.tuanha.wallet.entities.Config
import com.tuanha.wallet.entities.Token
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface AppApi {

    @GET("https://api.coingecko.com/api/v3/coins/list")
    suspend fun getListTokenFromCoingecko(): List<Token>

    @GET("https://api.coingecko.com/api/v3/coins/{tokenId}")
    suspend fun getTokenDetail(@Path("tokenId") tokenId: String): Token



    @GET("https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/{chainIdStr}/tokenlist.json")
    suspend fun fetchListTokenSupportInChain(@Path("chainIdStr") chainIdStr: String): AppResponse<List<Token>>

    @GET("https://api.coingecko.com/api/v3/simple/price?include_market_cap=true&include_24hr_vol=true&include_24hr_change=true&include_last_updated_at=true")
    suspend fun getPrices(@Query("vs_currencies") currencies: String = "usd,btc", @Query("ids") tokenIdS: String): Map<String, ObjectNode>

    @GET("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false&price_change_percentage=1h%2C24h%2C7d")
    suspend fun getPriceChange(@Query("ids") tokenIdS: String): List<ObjectNode>


    @GET
    suspend fun fetchConfig(@Url url: String): Config

    @GET
    suspend fun fetchListToken(@Url url: String): List<Token>
}

@Keep
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class AppResponse<T> {

    @JsonProperty("statusCode")
    var statusCode: String = ""

    @JsonProperty("data")
    @JsonAlias("tokens")
    var data: T? = null
}
