package com.tuanha.wallet.data.repositories

import com.tuanha.wallet.data.chain.ChainManager
import com.tuanha.wallet.data.chain.getChainIdStr
import com.tuanha.wallet.entities.Wallet

class WalletRepository(private val chainManager: ChainManager) {

    suspend fun getAllWallet(): List<Wallet> {

        return listOf(
            Wallet("1").apply {
                chainIdAndAddress = chainManager.list.associateBy({ it.getChainIdStr() }, { "0xd1bc3f3d5b107754ee0aeed5344e30f40686a3d2" })
            },
            Wallet("12").apply {
                chainIdAndAddress = chainManager.list.associateBy({ it.getChainIdStr() }, { "0xa6ccac44a8f51704220d654460855999dff37187" })
            },
            Wallet("123").apply {
                chainIdAndAddress = chainManager.list.associateBy({ it.getChainIdStr() }, { "0x8d61ab7571b117644a52240456df66ef846cd999" })
            }
        )
    }
}