package com.tuanha.wallet.data.chain

import com.tuanha.wallet.data.api.AppApi
import com.tuanha.wallet.entities.Chain
import com.tuanha.wallet.entities.Token
import com.tuanha.wallet.entities.getAddress
import com.tuanha.wallet.entities.getDecimals
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.math.BigDecimal
import kotlin.coroutines.coroutineContext

abstract class BaseChain : KoinComponent {

    var tokenAddressAndToken: Map<String, Token>? = null


    val appApi: AppApi by inject()


    private val mutexDecimal by lazy {
        Mutex()
    }

    private val mutexFetchTokenAddressAndToken by lazy {
        Mutex()
    }


    abstract suspend fun getChain(): Chain

    abstract suspend fun getNativeToken(): Token


    abstract suspend fun fetchListTokenSupportInChain(): List<Token>


    abstract suspend fun decimals(tokenAddress: String): Int

    abstract suspend fun decimals(listTokenAddress: List<String>): Map<String, Int>


    abstract suspend fun balance(walletAddress: String, tokenAddress: String): BigDecimal

    abstract suspend fun balance(walletAddress: String, listTokenAddress: List<String>): Map<String, BigDecimal>


    abstract suspend fun balanceNativeToken(walletAddress: String): BigDecimal


    open suspend fun fetchTokenAddressAndTokenSupportInChain(): Map<String, Token> = withContext(coroutineContext) {

        return@withContext mutexFetchTokenAddressAndToken.withLock(getChain().idStr + "fetchTokenAddressAndToken") {

            if (tokenAddressAndToken != null) {
                return@withLock tokenAddressAndToken!!
            }

            tokenAddressAndToken = fetchListTokenSupportInChain().associateBy { it.addressInChain?.lowercase() ?: "" }.filter { it.key.isNotBlank() }

            return@withLock tokenAddressAndToken ?: emptyMap()
        }
    }

    open suspend fun support(tokenAddress: String?): Boolean {

        return if (tokenAddress.isNullOrBlank()) false
        else if (tokenAddress == getNativeTokenAddress()) return true
        else fetchTokenAddressAndTokenSupportInChain().containsKey(tokenAddress.lowercase())
    }
}

suspend fun BaseChain.getChainId(): Long = getChain().id

suspend fun BaseChain.getChainIdStr(): String = getChain().idStr


suspend fun BaseChain.getNativeTokenSymbol(): String = getNativeToken().symbol

suspend fun BaseChain.getNativeTokenDecimal(): Int = getNativeToken().getDecimals(getChainIdStr()) ?: 0

suspend fun BaseChain.getNativeTokenAddress(): String = getNativeToken().getAddress(getChainIdStr()) ?: ""


suspend fun BaseChain.decimalsOrNull(tokenAddress: String) = kotlin.runCatching { decimals(tokenAddress) }.getOrNull()


suspend fun BaseChain.balanceOrNull(walletAddress: String, tokenAddress: String) = kotlin.runCatching { balance(walletAddress, tokenAddress) }.getOrNull()

suspend fun BaseChain.balanceOrZero(walletAddress: String, tokenAddress: String) = kotlin.runCatching { balance(walletAddress, tokenAddress) }.getOrDefault(BigDecimal.ZERO)