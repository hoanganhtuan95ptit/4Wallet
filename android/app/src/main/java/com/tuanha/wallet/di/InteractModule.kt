package com.tuanha.wallet.di

import com.tuanha.wallet.data.usecases.GetMarketPriceUseCase
import com.tuanha.wallet.data.usecases.GetTokenAssetUseCase
import com.tuanha.wallet.data.usecases.SyncMarketPriceUseCase
import com.tuanha.wallet.data.usecases.chain.GetChainSelectUseCase
import com.tuanha.wallet.data.usecases.chain.GetChainUseCase
import com.tuanha.wallet.data.usecases.wallet.GetWalletSelectUseCase
import com.tuanha.wallet.data.usecases.wallet.GetWalletUseCase
import org.koin.dsl.module

@JvmField
val interactModule = module {


    single { GetChainUseCase(get()) }

    single { GetChainSelectUseCase(get()) }


    single { GetWalletUseCase(get()) }

    single { GetWalletSelectUseCase(get()) }


    single { GetTokenAssetUseCase(get(), get(), get()) }


    single { GetMarketPriceUseCase(get()) }

    single { SyncMarketPriceUseCase(get(), get(), get()) }

}
