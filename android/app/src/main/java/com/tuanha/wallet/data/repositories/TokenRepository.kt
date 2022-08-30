@file:Suppress("SimpleRedundantLet")

package com.tuanha.wallet.data.repositories

import android.util.Log
import com.tuanha.coreapp.utils.FieldMemory
import com.tuanha.wallet.data.api.AppApi
import com.tuanha.wallet.entities.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.coroutineContext

class TokenRepository(
    private val appApi: AppApi,
    private val appRepository: AppRepository
) {

    private val myListMutex = Mutex()

    private var listToken: FieldMemory<CopyOnWriteArrayList<Token>> = FieldMemory(CopyOnWriteArrayList())


    suspend fun fetchTokens(): List<Token> = withContext(coroutineContext + Dispatchers.IO) {

        return@withContext myListMutex.withLock("GET_TOKENS") {

            val listTokenCache: List<Token>? = this@TokenRepository.listToken.getData()

            if (listTokenCache != null && listTokenCache.isNotEmpty()) {

                return@withLock listTokenCache
            }

            val listTokenRemote: List<Token> = appRepository.fetchConfig().listUrlToken.map {

                async {
                    runCatching {

                        appApi.fetchListToken(it)
                    }.getOrElse {

                        Log.d("tuanha", "fetchTokens: ", it)
                        null
                    }
                }
            }.let {

                it.awaitAll().filterNotNull().flatten().associateBy { token -> token.id }.values.toList()
            }

            if (listTokenRemote.isNotEmpty()) {
                this@TokenRepository.listToken.setData(CopyOnWriteArrayList(listTokenRemote))
            } else {
                throw RuntimeException("not found list token")
            }

            return@withLock this@TokenRepository.listToken.getData() ?: emptyList()
        }
    }

}