package com.tuanha.wallet.data.usecases

import com.tuanha.coreapp.utils.extentions.offerActive
import com.tuanha.wallet.data.api.AppApi
import com.tuanha.wallet.data.chain.BaseChain
import com.tuanha.wallet.data.chain.ChainManager
import com.tuanha.wallet.data.chain.getChainIdStr
import com.tuanha.wallet.data.repositories.TokenRepository
import com.tuanha.wallet.entities.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.math.BigDecimal
import kotlin.coroutines.coroutineContext

class GetTokenAssetUseCase(
    private val appApi: AppApi,
    private val chainManager: ChainManager,
    private val tokenRepository: TokenRepository
) : BaseUseCase<GetTokenAssetUseCase.Param, Flow<List<Token>>> {

    override suspend fun execute(param: Param?): Flow<List<Token>> = channelFlow {
        checkNotNull(param)

        val handler = chainManager.getHandle(param.listChain.map { it.id })


        val tokens = tokenRepository.fetchTokens().toMutableList().apply {

            addAll(handler.map { it.getNativeToken() })
        }.map {

            it.copy().apply {
                chainIdAndAddress = it.chainIdAndAddress
                chainIdAndDecimals = it.chainIdAndDecimals
            }
        }


        handler.map { baseChain ->

            async(Dispatchers.IO) {
                fetchBalances(baseChain, param.listWallet, tokens)
            }
        }.awaitAll()


        val listToken = tokens.map { token ->

            token.chainIdAndBalance = token.chainIdAndAddressAndBalance.map { Pair(it.key, it.value) }.groupBy {

                it.first.substring(0, it.first.indexOf("--"))
            }.mapValues { entry ->

                entry.value.sumOf { it.second }
            }

            token
        }.filter {

            it.totalBalance > BigDecimal.ZERO
        }


        offerActive(listToken)


        launch(coroutineContext + Dispatchers.IO) {

            val marketPrices = appApi.getPrices(
                MarketCurrency.values().joinToString(",") { it.value },
                listToken.joinToString(",") { it.id }
            )

            listToken.forEach { token ->

                token.currencyAndPrice = MarketCurrency.values().associateBy({ marketCurrency ->

                    marketCurrency
                }, { marketCurrency ->

                    marketPrices[token.id]?.let { marketPrice ->

                        MarketPrice(
                            marketPrice.get(marketCurrency.value).toPrettyString(),
                            marketPrice.get("${marketCurrency.value}_24h_vol").toPrettyString(),
                            marketPrice.get("${marketCurrency.value}_24h_change").toPrettyString(),
                        )
                    } ?: let {

                        MarketPrice()
                    }
                })

                offerActive(listToken)
            }
        }

        launch(coroutineContext + Dispatchers.IO) {

            val priceChanges = appApi.getPriceChange(
                listToken.joinToString(",") { it.id }
            )

            priceChanges.forEachIndexed { index, token ->
                val a = token.get("price_change_percentage_24h_in_currency").toPrettyString()
                listToken.toList()[index].price24hChangePercent = a
            }

            offerActive(listToken)
        }

        awaitClose {

        }
    }

    private suspend fun fetchBalances(baseChain: BaseChain, listWallet: List<Wallet>, tokens: List<Token>) = withContext(coroutineContext) {

        val tokenSupportedInChain = tokens.filter { token ->

            token.getAddress(baseChain.getChainIdStr()) != null && token.getDecimals(baseChain.getChainIdStr()) != null && baseChain.support(token.getAddress(baseChain.getChainIdStr()))
        }

        listWallet.map { wallet ->

            async {
                fetchBalances(baseChain, wallet, tokenSupportedInChain)
            }
        }.awaitAll()
    }

    private suspend fun fetchBalances(baseChain: BaseChain, wallet: Wallet, tokens: List<Token>) = withContext(coroutineContext) {

        val walletAddress = wallet.getAddress(baseChain.getChainIdStr()) ?: return@withContext

        val tokenAddressAndBalance = baseChain.balance(walletAddress, tokens.map { it.getAddress(baseChain.getChainIdStr())!! })

        tokens.forEach {

            it.chainIdAndAddressAndBalance[baseChain.getChainIdStr() + "--" + walletAddress] = tokenAddressAndBalance[it.getAddress(baseChain.getChainIdStr()) ?: ""] ?: BigDecimal.ZERO
        }
    }

    data class Param(val listChain: List<Chain> = emptyList(), val listWallet: List<Wallet>) : BaseUseCase.Param()
}