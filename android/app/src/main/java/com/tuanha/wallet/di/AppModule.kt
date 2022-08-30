package com.tuanha.wallet.di

import com.tuanha.wallet.data.chain.BaseChain
import com.tuanha.wallet.data.chain.ChainManager
import com.tuanha.wallet.data.chain.evm.bsc.BscChain
import com.tuanha.wallet.data.chain.evm.ethereum.EthereumChain
import org.koin.dsl.bind
import org.koin.dsl.module

@JvmField
val appModule = module {

    single { BscChain() } bind BaseChain::class

    single { EthereumChain() } bind BaseChain::class

    single { ChainManager(getKoin().getAll()) }
}

