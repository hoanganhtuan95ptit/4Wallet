@file:Suppress("UNCHECKED_CAST")

package com.tuanha.wallet.data.chain.evm

import com.tuanha.coreapp.utils.extentions.offerActive
import com.tuanha.wallet.data.chain.BaseChain
import com.tuanha.wallet.data.chain.getNativeTokenAddress
import com.tuanha.wallet.data.chain.getNativeTokenDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.koin.core.inject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthCall
import org.web3j.protocol.http.HttpService
import java.math.BigDecimal


abstract class EVMChain : BaseChain() {

    private val client: OkHttpClient by inject()

    abstract suspend fun getRpcUrls(): List<String>


    override suspend fun decimals(tokenAddress: String): Int {

        if (tokenAddress == getNativeTokenAddress()) {

            return getNativeTokenDecimal()
        }

        val function = Function(
            "decimals",
            emptyList(),
            listOf<TypeReference<*>>(object : TypeReference<Uint8>() {})
        )

        val responseValue = callSmartContractFunction(function, tokenAddress, null)

        val response = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )

        return (response.getOrNull(0) as? Uint8)?.value?.toInt() ?: throw RuntimeException("Can't get token decimal")
    }

    override suspend fun decimals(listTokenAddress: List<String>): Map<String, Int> {

        return if (this is MultiCallEVMChain) decimalAll(listTokenAddress)
        else error("function need override")
    }

    override suspend fun balance(walletAddress: String, tokenAddress: String): BigDecimal {

        if (tokenAddress == getNativeTokenAddress()) {

            return balanceNativeToken(walletAddress)
        }

        val function = Function(
            "balanceOf",
            listOf(Address(walletAddress)),
            listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
            })
        )

        val responseValue = callSmartContractFunction(function, tokenAddress.uppercase(), null)

        val response = FunctionReturnDecoder.decode(
            responseValue, function.outputParameters
        )

        return (response.getOrNull(0) as? Uint256)?.value?.let { BigDecimal(it) } ?: throw RuntimeException("Can't get token decimal")
    }

    override suspend fun balance(walletAddress: String, listTokenAddress: List<String>): Map<String, BigDecimal> {

        return if (this is MultiCallEVMChain) balanceAll(walletAddress, listTokenAddress)
        else error("function need override")
    }

    override suspend fun balanceNativeToken(walletAddress: String): BigDecimal {

        return callWeb3j { it.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().balance.toBigDecimal() }
    }

    open suspend fun callSmartContractFunction(function: Function, contractAddress: String, fromAddress: String?): String? {

        val encodedFunction = FunctionEncoder.encode(function)

        val response = ethCall(Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST)

        return response.value
    }


    open suspend fun ethCall(transaction: Transaction, defaultBlockParameter: DefaultBlockParameter): EthCall = callWeb3j {

        ethCall(transaction, defaultBlockParameter, it)
    }

    open fun ethCall(transaction: Transaction, defaultBlockParameter: DefaultBlockParameter, node: Web3j): EthCall = try {

        node.ethCall(transaction, defaultBlockParameter).send()
    } catch (ex: Exception) {

        EthCall().apply { error.message = ex.localizedMessage }
    }


    suspend inline fun <reified T> callWeb3j(crossinline action: (Web3j) -> T): T = channelFlow {

        val responses = getRpcUrls().map {

            async(Dispatchers.IO) {

                val response: Any? = runCatching {
                    action.invoke(it.toWeb3j())
                }.getOrElse {
                    it
                }

                if (response is T) {

                    offerActive(response as T)

                    null
                } else {

                    response
                }
            }
        }

        launch {

            responses.awaitAll().takeIf {

                it.all { response -> response != null }
            }?.firstOrNull {

                throw (it as Throwable)
            }
        }

        awaitClose {
            responses.forEach { ethCall -> ethCall.cancel() }
        }
    }.first()


    fun String.toWeb3j(): Web3j {

        return Web3j.build(HttpService(this, client, false))
    }

}