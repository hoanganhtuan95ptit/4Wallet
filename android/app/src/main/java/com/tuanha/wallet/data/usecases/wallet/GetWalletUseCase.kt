package com.tuanha.wallet.data.usecases.wallet

import com.tuanha.coreapp.utils.extentions.offerActive
import com.tuanha.wallet.data.repositories.WalletRepository
import com.tuanha.wallet.data.usecases.BaseUseCase
import com.tuanha.wallet.entities.Wallet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class GetWalletUseCase(
    private val walletRepository: WalletRepository,
) : BaseUseCase<GetWalletUseCase.Param, Flow<List<Wallet>>> {

    override suspend fun execute(param: Param?): Flow<List<Wallet>> = channelFlow {

        offerActive(walletRepository.getAllWallet())

        awaitClose()
    }

    data class Param(val listChainId: List<Long> = emptyList()) : BaseUseCase.Param()
}