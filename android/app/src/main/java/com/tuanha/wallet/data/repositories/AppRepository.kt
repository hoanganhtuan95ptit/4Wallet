@file:Suppress("SimpleRedundantLet")

package com.tuanha.wallet.data.repositories

import com.tuanha.coreapp.utils.FieldMemory
import com.tuanha.wallet.data.api.AppApi
import com.tuanha.wallet.entities.Config
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class AppRepository(private val appApi: AppApi) {

    private val mutex = Mutex()

    private var config: FieldMemory<Config> = FieldMemory()

    private val listConfigUrl = listOf(
        "https://raw.githubusercontent.com/hoanganhtuan95ptit/4Wallet/main/assets/config.json"
    )

    suspend fun fetchConfig(): Config = withContext(coroutineContext) {

        return@withContext mutex.withLock("GET_CONFIG") {

            val configCache: Config? = this@AppRepository.config.getData()

            if (configCache != null) {
                return@withLock configCache
            }

            val configRemote: Config? = listConfigUrl.map { url ->

                async { runCatching { appApi.fetchConfig(url) }.getOrNull() }
            }.let {

                it.awaitAll().filterNotNull().firstOrNull()
            }

            if (configRemote != null) {

                this@AppRepository.config.setData(configRemote)
            } else {

                throw RuntimeException("not found config")
            }

            return@withLock this@AppRepository.config.getData() as Config
        }
    }
}