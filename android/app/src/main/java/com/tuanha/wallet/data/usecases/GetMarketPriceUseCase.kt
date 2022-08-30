package com.tuanha.wallet.data.usecases

import com.tuanha.wallet.data.repositories.TokenRepository
import com.tuanha.wallet.entities.MarketCurrency
import com.tuanha.wallet.entities.MarketPrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class GetMarketPriceUseCase(
    private val tokenRepository: TokenRepository,
) : BaseUseCase<GetMarketPriceUseCase.Param, Flow<Map<String, Map<MarketCurrency, MarketPrice>>>> {

    override suspend fun execute(param: Param?): Flow<Map<String, Map<MarketCurrency, MarketPrice>>> = channelFlow{

//        return tokenRepository.getToken()
    }

    class Param : BaseUseCase.Param()
}