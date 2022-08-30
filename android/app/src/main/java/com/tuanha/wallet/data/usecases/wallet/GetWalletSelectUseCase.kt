package com.tuanha.wallet.data.usecases.wallet

import com.tuanha.coreapp.utils.extentions.offerActive
import com.tuanha.wallet.data.repositories.WalletRepository
import com.tuanha.wallet.data.usecases.BaseUseCase
import com.tuanha.wallet.entities.Wallet
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class GetWalletSelectUseCase(
    private val walletRepository: WalletRepository,
) : BaseUseCase<GetWalletSelectUseCase.Param, Flow<List<Wallet>>> {

    override suspend fun execute(param: Param?): Flow<List<Wallet>> = channelFlow {
        offerActive(walletRepository.getAllWallet())

        awaitClose()
    }

    class Param() : BaseUseCase.Param()
}