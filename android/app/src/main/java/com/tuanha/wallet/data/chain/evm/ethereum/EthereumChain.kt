@file:Suppress("UNCHECKED_CAST")

package com.tuanha.wallet.data.chain.evm.ethereum

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

class EthereumChain : EVMChain(), MultiCallEVMChain {

    private var listToken: List<Token>? = null


    private val mutexFetchListTokenSupportInChain by lazy {
        Mutex()
    }

    override val multiCallContract: String by lazy {
        "0xeefBa1e63905eF1D7ACbA5a8513c70307C1cE441"
    }

    private val chain: Chain by lazy {
        Chain(id = 1, idStr = "ethereum", name = "Ethereum", logo = "https://raw.githubusercontent.com/trustwallet/assets/master/blockchains/ethereum/info/logo.png")
    }

    private val nativeToken: Token by lazy {
        Token(id = "ethereum", name = "Ethereum", symbol = "eth", logo = "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880").apply {

            chainIdAndAddress = mapOf(chain.idStr to Hex.toHexString(chain.idStr.toByteArray()))
            chainIdAndDecimals = mapOf(chain.idStr to 18)
        }
    }


    override suspend fun getChain(): Chain {
        return chain
    }

    override suspend fun getNativeToken(): Token {
        return nativeToken
    }

    override suspend fun getRpcUrls(): List<String> {
        return listOf(
            "https://rpc.ankr.com/eth"
        )
    }

    override suspend fun fetchListTokenSupportInChain(): List<Token> = withContext(coroutineContext) {

        return@withContext mutexFetchListTokenSupportInChain.withLock(getChainIdStr() + "fetchListTokenSupportInChain") {

            if (listToken != null) {
                return@withLock listToken!!
            }

            listToken = appApi.fetchListTokenSupportInChain(getChainIdStr()).data


            return@withLock listToken!!
        }
    }

}