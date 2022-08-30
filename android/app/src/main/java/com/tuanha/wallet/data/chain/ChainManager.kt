@file:Suppress("SimpleRedundantLet")

package com.tuanha.wallet.data.chain

import android.util.Log
import com.tuanha.wallet.entities.Chain

data class ChainManager(val list: List<BaseChain>) {

    private var chainIdAndHandle: Map<Long, BaseChain>? = null

    private var chainIdStrAndHandle: Map<String, BaseChain>? = null


    private var chainIdAndChain: Map<Long, Chain>? = null


    suspend fun getHandle(listChainId: List<Long>): List<BaseChain> {

        return getHandle(*listChainId.toLongArray())
    }

    suspend fun getHandle(vararg chainId: Long): List<BaseChain> {

        val map = chainIdAndHandle ?: list.associateBy {

            it.getChain().id
        }.apply {

            chainIdAndHandle = this
        }

        return if (chainId.isEmpty()) {
            map
        } else {
            map.filter { it.key in chainId }
        }.let {
            it.values.toList()
        }
    }

    suspend fun getHandle(vararg chainIdStr: String): List<BaseChain> {

        val map = chainIdStrAndHandle ?: list.associateBy {

            it.getChain().idStr
        }.apply {

            chainIdStrAndHandle = this
        }

        return if (chainIdStr.isEmpty()) {
            map
        } else {
            map.filter { it.key in chainIdStr }
        }.let {
            it.values.toList()
        }
    }

    suspend fun getHandle(chainIdStr: String): BaseChain? {

        val map = chainIdStrAndHandle ?: list.associateBy {

            it.getChain().idStr
        }.apply {

            chainIdStrAndHandle = this
        }

        return map[chainIdStr]
    }

    suspend fun getChain(listChainId: List<Long>): List<Chain> {
        return getChain(*listChainId.toLongArray())
    }

    suspend fun getChain(vararg chainId: Long): List<Chain> {

        val map = chainIdAndChain ?: list.associateBy({

            it.getChain().id
        }, {

            it.getChain()
        }).apply {

            chainIdAndChain = this
        }

        return if (chainId.isEmpty()) {
            map
        } else {
            map.filter { it.key in chainId }
        }.let {
            it.values.toList()
        }
    }
}