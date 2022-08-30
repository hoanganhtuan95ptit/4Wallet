package com.tuanha.wallet.di

import com.tuanha.wallet.data.repositories.AppRepository
import com.tuanha.wallet.data.repositories.TokenRepository
import com.tuanha.wallet.data.repositories.WalletRepository
import org.koin.dsl.module

@JvmField
val repositoryModule = module {

    single { AppRepository(get()) }

    single { TokenRepository(get(), get()) }

    single { WalletRepository(get()) }
}
