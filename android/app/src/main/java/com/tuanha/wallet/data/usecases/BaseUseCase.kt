package com.tuanha.wallet.data.usecases

import org.koin.core.KoinComponent


interface BaseUseCase<Param : BaseUseCase.Param, Result> : KoinComponent {
    suspend fun execute(param: Param? = null): Result

    open class Param
}