@file:Suppress("UNCHECKED_CAST", "NAME_SHADOWING")

package com.tuanha.wallet.data.chain.evm

import android.util.Log
import com.tuanha.wallet.data.chain.getNativeTokenAddress
import com.tuanha.wallet.data.chain.getNativeTokenDecimal
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.bouncycastle.util.encoders.Hex
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.utils.Numeric
import java.math.BigDecimal
import kotlin.coroutines.coroutineContext

interface MultiCallEVMChain {

    val multiCallContract: String

    suspend fun decimalAll(listTokenAddress: List<String>): Map<String, Int> = withContext(coroutineContext) {

        if (this@MultiCallEVMChain !is EVMChain) {
            error("not support ${this@MultiCallEVMChain.javaClass.simpleName}")
        }

        val nativeDecimal = async {
            getNativeTokenDecimal()
        }


        val listTokenAddress = listTokenAddress.filter { it.isNotBlank() }

        val listDecimal = listTokenAddress.chunked(500).map {

            async { decimal1000(nativeDecimal, it) }
        }.awaitAll().flatten()


        val map = hashMapOf<String, Int>()

        listTokenAddress.forEachIndexed { index, s ->

            map[listTokenAddress[index]] = listDecimal[index]
        }

        map
    }

    private suspend fun decimal1000(nativeDecimal: Deferred<Int>, listTokenAddress: List<String>): List<Int> {

        if (this@MultiCallEVMChain !is EVMChain) {
            error("not support ${this@MultiCallEVMChain.javaClass.simpleName}")
        }

        val staticStruct = listTokenAddress.map { tokenAddress ->

            val function = Function(
                "decimals",
                emptyList(),
                listOf<TypeReference<*>>(object : TypeReference<Uint8>() {})
            )

            val encodeDataOfNameFunction = FunctionEncoder.encode(function)

            DynamicStruct(Address(tokenAddress), DynamicBytes(Hex.decode(encodeDataOfNameFunction.substring(2).toByteArray())))
        }

        val tokenDecimals = multi(staticStruct)

        return listTokenAddress.mapIndexed { index, s ->

            if (s == getNativeTokenAddress()) {
                nativeDecimal.await()
            } else {
                Numeric.toBigInt(tokenDecimals[index].value).toInt()
            }
        }

    }

    suspend fun balanceAll(walletAddress: String, listTokenAddress: List<String>): Map<String, BigDecimal> = withContext(coroutineContext) {

        if (this@MultiCallEVMChain !is EVMChain) {
            error("not support ${this@MultiCallEVMChain.javaClass.simpleName}")
        }

        val nativeBalance = async {
            callWeb3j { it.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().balance.toBigDecimal() }
        }

        val listTokenAddress = listTokenAddress.filter { it.isNotBlank() }

        val listBalance = listTokenAddress.chunked(500).map {

            async { balance1000(nativeBalance, walletAddress, it) }
        }.awaitAll().flatten()

        val map = hashMapOf<String, BigDecimal>()

        listTokenAddress.forEachIndexed { index, s ->

            map[listTokenAddress[index]] = listBalance[index]
        }

        map
    }

    private suspend fun balance1000(nativeBalance: Deferred<BigDecimal>, walletAddress: String, listTokenAddress: List<String>): List<BigDecimal> {

        if (this@MultiCallEVMChain !is EVMChain) {
            error("not support ${this@MultiCallEVMChain.javaClass.simpleName}")
        }

        val staticStruct = listTokenAddress.map { tokenAddress ->

            val function = Function(
                "balanceOf",
                listOf(Address(walletAddress)),
                listOf<TypeReference<*>>(object : TypeReference<Uint256>() {
                })
            )

            val encodeDataOfNameFunction = FunctionEncoder.encode(function)

            DynamicStruct(Address(tokenAddress), DynamicBytes(Hex.decode(encodeDataOfNameFunction.substring(2).toByteArray())))
        }

        val tokenBalances = kotlin.runCatching { multi(staticStruct) }.getOrNull()?.map { Numeric.toBigInt(it.value).toBigDecimal() } ?: listTokenAddress.map { BigDecimal.ZERO }

        return listTokenAddress.mapIndexed { index, s ->

            if (s == getNativeTokenAddress()) {
                nativeBalance.await()
            } else {
                tokenBalances[index]
            }
        }
    }

    suspend fun multi(staticStruct: List<DynamicStruct>): List<DynamicBytes> = withContext(coroutineContext) {

        if (this@MultiCallEVMChain !is EVMChain) {
            error("not support ${this@MultiCallEVMChain.javaClass.simpleName}")
        }

        val aggregateFunction = Function(
            "aggregate",
            listOf(DynamicArray(DynamicStruct::class.java, staticStruct)),
            listOf(object : TypeReference<Uint256>() {}, object : TypeReference<DynamicArray<DynamicBytes>>() {})
        )

        val responseValue = callSmartContractFunction(aggregateFunction, multiCallContract, null)

        val response = FunctionReturnDecoder.decode(
            responseValue, aggregateFunction.outputParameters
        )

        val dynamicArray = response.getOrNull(1) as? DynamicArray<*> ?: throw RuntimeException("Can't call multi")

        dynamicArray.value as List<DynamicBytes>
    }
}