package com.tuanha.wallet

import com.google.firebase.FirebaseApp
import com.tuanha.wallet.di.*
import kotlinx.coroutines.newSingleThreadContext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : com.tuanha.coreapp.App() {

    companion object {
        lateinit var shared: App
    }

    override fun onCreate() {
        super.onCreate()
        shared = this

        startKoin {

            androidContext(this@App)

            androidLogger(Level.NONE)

            modules(
                appModule,

                apiModule,

                daoModule,

                cacheModule,

                memoryModule,

                realtimeModule,

                repositoryModule,

                interactModule,

                exceptionModule,

                viewModelModule
            )
        }
    }

}