@file:Suppress("UNCHECKED_CAST")

package com.tuanha.wallet.data.usecases

import android.os.Environment
import android.util.Log
import com.tuanha.core.utils.extentions.toJson
import com.tuanha.core.utils.extentions.toListObject
import com.tuanha.wallet.App
import com.tuanha.wallet.data.api.AppApi
import com.tuanha.wallet.data.chain.ChainManager
import com.tuanha.wallet.data.chain.getChainIdStr
import com.tuanha.wallet.data.repositories.TokenRepository
import com.tuanha.wallet.entities.Token
import com.tuanha.wallet.entities.getAddress
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.*

class SyncMarketPriceUseCase(
    private val appApi: AppApi,
    private val chainManager: ChainManager,
    private val tokenRepository: TokenRepository
) : BaseUseCase<SyncMarketPriceUseCase.Param, Flow<List<Token>>> {

    override suspend fun execute(param: Param?): Flow<List<Token>> = channelFlow {

        syncInfo()
        syncDecimal()

        awaitClose {}
    }

    private suspend fun syncInfo() = takeIf { false }?.syncSing(appApi.getListTokenFromCoingecko().filter { it.id.isNotBlank() }) { indexChunked, index, token ->

        var to: Token? = null

        while (to == null) {

            delay(3000)
            to = kotlin.runCatching { appApi.getTokenDetail(token.id) }.getOrNull()
            Log.d("tuanha", "syncInfo: $indexChunked-$index")
        }

        if (to.chainIdAndAddress.filter { it.value.isNotBlank() }.values.isNotEmpty()) {

            to.logo = kotlin.runCatching { to.image?.get("large")?.textValue() }.getOrNull() ?: kotlin.runCatching { to.image?.get("small")?.textValue() }.getOrNull()
                    ?: kotlin.runCatching { to.image?.get("thumb")?.textValue() }.getOrNull() ?: ""

            to.image = null

            to
        } else {

            null
        }
    }

    private suspend fun syncDecimal() = takeIf { false }?.syncList(tokenRepository.fetchTokens()) { indexChunked, tokens ->

        chainManager.list.mapIndexed { _, baseChain ->

            val tokenAddressAndDecimal = hashMapOf<String, Int>()

            tokens.filter { it.getAddress(baseChain.getChainIdStr()) != null }.chunked(10).forEach { list ->

                tokenAddressAndDecimal.putAll(kotlin.runCatching { baseChain.decimals(list.map { it.getAddress(baseChain.getChainIdStr())!! }) }.getOrDefault(emptyMap()))
            }

            tokens.forEachIndexed { index, it ->

                val tokenAddress = it.getAddress(baseChain.getChainIdStr())

                if (tokenAddressAndDecimal.containsKey(tokenAddress)) {

                    val chainIdAndDecimals = it.chainIdAndDecimals.toMutableMap()
                    chainIdAndDecimals[baseChain.getChainIdStr()] = tokenAddressAndDecimal[tokenAddress]!!
                    Log.d("tuanha", "syncDecimal: $indexChunked $index ${chainIdAndDecimals.toJson()}")
                    it.chainIdAndDecimals = chainIdAndDecimals
                }
            }
        }

        tokens
    }

    private inline fun syncSing(tokens: List<Token>, update: (indexChunked: Int, index: Int, Token) -> Token?) {

        tokens.chunked(2000).forEachIndexed { indexChunked, list ->

            val pa = App.shared.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath

            val filePath = "$pa/tokenList$indexChunked.json"

            if (!File(pa).exists()) File(pa).mkdirs()
            if (!File(filePath).exists()) File(filePath).createNewFile()

            val tokenLocal = readFile(filePath).toListObject(Token::class.java).toMutableList()

            list.forEachIndexed { index, token ->

                update.invoke(indexChunked, index, token)?.let {

                    tokenLocal.add(it)
                    writeFile(filePath, tokenLocal.associateBy { it.id }.values.toJson())
                }
            }
        }
    }

    private inline fun syncList(tokens: List<Token>, update: (indexChunked: Int, List<Token>) -> List<Token>?) {

        tokens.chunked(2000).forEachIndexed { indexChunked, list ->

            val pa = App.shared.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!.absolutePath

            val filePath = "$pa/tokenList$indexChunked.json"

            if (!File(pa).exists()) File(pa).mkdirs()
            if (!File(filePath).exists()) File(filePath).createNewFile()

            val tokenLocal = readFile(filePath).toListObject(Token::class.java).toMutableList()

            update.invoke(indexChunked, list)?.let {

                tokenLocal.addAll(it)
                writeFile(filePath, tokenLocal.associateBy { it.id }.values.toJson())
            }
        }
    }

    private fun readFile(filePath: String): String {

        val builder = StringBuilder()

        var bufferedReader: BufferedReader? = null

        try {
            bufferedReader = BufferedReader(FileReader(File(filePath)))
        } catch (e: FileNotFoundException) {
            Log.d("tuanha", "readFile: ", e)
        }


        try {
            var row = ""
            while (bufferedReader?.readLine()?.also { row = it } != null) {
                builder.append(
                    """
                        $row
                        
                        """.trimIndent()
                )
            }
            bufferedReader?.close()
        } catch (e: IOException) {
            Log.d("tuanha", "readFile: ", e)
        }

        return builder.toString()
    }

    private fun writeFile(filePath: String, text: String?) {

        var fileWriter: FileWriter? = null

        try {
            fileWriter = FileWriter(filePath, false)
            fileWriter.write(text)
        } catch (e: IOException) {
            throw RuntimeException("IOException occurred. ", e)
        } finally {
            fileWriter?.close()
        }
    }

    class Param : BaseUseCase.Param()
}