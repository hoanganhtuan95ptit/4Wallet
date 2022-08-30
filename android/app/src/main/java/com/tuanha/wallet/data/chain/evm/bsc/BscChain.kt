package com.tuanha.wallet.data.chain.evm.bsc

import com.tuanha.wallet.data.chain.evm.EVMChain
import com.tuanha.wallet.data.chain.evm.MultiCallEVMChain
import com.tuanha.wallet.data.chain.getChainIdStr
import com.tuanha.wallet.entities.Chain
import com.tuanha.wallet.entities.Token
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Hex
import kotlin.coroutines.coroutineContext

class BscChain : EVMChain(), MultiCallEVMChain {

    private var listToken: List<Token>? = null

    override val multiCallContract: String by lazy {
        "0x1Ee38d535d541c55C9dae27B12edf090C608E6Fb"
    }

    private val mutexFetchListTokenSupportInChain by lazy {
        Mutex()
    }

    override suspend fun getChain(): Chain {
        return Chain(id = 56, idStr = "binance-smart-chain", name = "Binance", logo = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/binance/info/logo.png")
    }

    override suspend fun getNativeToken(): Token {
        return Token(id = "binancecoin", name = "BNB Beacon Chain", symbol = "BNB", logo = "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850").apply {

            chainIdAndAddress = mapOf(getChainIdStr() to Hex.toHexString(getChainIdStr().toByteArray()))
            chainIdAndDecimals = mapOf(getChainIdStr() to 18)
        }
    }

    override suspend fun getRpcUrls(): List<String> {
        return listOf(
            "https://rpc.ankr.com/bsc"
        )
    }

    override suspend fun fetchListTokenSupportInChain(): List<Token> = withContext(coroutineContext) {

        return@withContext mutexFetchListTokenSupportInChain.withLock(getChainIdStr() + "fetchListTokenSupportInChain") {

            if (listToken != null) {
                return@withLock listToken!!
            }

            listToken = appApi.fetchListTokenSupportInChain("binance").data

            return@withLock listToken!!
        }
    }

}