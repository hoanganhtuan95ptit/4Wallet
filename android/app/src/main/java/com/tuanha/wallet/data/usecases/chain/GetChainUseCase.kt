package com.tuanha.wallet.data.usecases.chain

import com.tuanha.coreapp.utils.extentions.offerActive
import com.tuanha.wallet.data.chain.ChainManager
import com.tuanha.wallet.data.usecases.BaseUseCase
import com.tuanha.wallet.entities.Chain
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class GetChainUseCase(
    private val chainManager: ChainManager
) : BaseUseCase<GetChainUseCase.Param, Flow<List<Chain>>> {

    override suspend fun execute(param: Param?): Flow<List<Chain>> = channelFlow {

        offerActive(chainManager.getChain(emptyList()))

        awaitClose {

        }
    }

    data class Param(val listChainId: List<Long> = emptyList()) : BaseUseCase.Param()
}