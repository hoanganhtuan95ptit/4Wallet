package com.tuanha.wallet.di

import com.tuanha.wallet.BuildConfig
import com.tuanha.wallet.data.api.AppApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.tuanha.wallet.data.api.retrofit.interceptor.LoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

@JvmField
val apiModule = module {

    single {
        OkHttpClient
            .Builder()
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .connectTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(LoggingInterceptor().setLevel(if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE))
            .hostnameVerifier { _, _ -> true }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("http://45.77.206.34:3000/")
            .addConverterFactory(JacksonConverterFactory.create())
            .client(get())
            .build()
    }

    single<AppApi> { (get() as Retrofit).create(AppApi::class.java) }

}