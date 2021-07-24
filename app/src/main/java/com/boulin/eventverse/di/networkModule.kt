package com.boulin.eventverse.di

import com.hadiyarajesh.flower.calladpater.FlowCallAdapterFactory
import com.boulin.eventverse.data.network.ApiInterface
import com.boulin.eventverse.data.network.interceptors.BearerInterceptor
import com.boulin.eventverse.data.network.adapters.DateAdapter
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Creates a module for network that exposes the ApiInterface and the BearerInterceptor (for later authorization) as singletons
 */
val networkModule = module {

    val BASE_URL = "https://us-central1-eventverse-boulin.cloudfunctions.net/api";
    val API_VERSION: Long = 1;
    val TIMEOUT_SECONDS: Long = 10

    /**
     * Returns the API host from the base URL and the API version
     */
    fun getApiHost(): String {
        return "$BASE_URL/v$API_VERSION/"
    }

    val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Date::class.java, DateAdapter()) // add adapter to convert dates
        .create()

    val bearerInterceptor = BearerInterceptor()

    val loggerInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggerInterceptor) // add the logger
        .addInterceptor(bearerInterceptor) // add the bearer (for authorization)
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl(getApiHost())
        .client(okHttpClient)
        .addCallAdapterFactory(FlowCallAdapterFactory.create()) // converts call to flow (kotlin coroutines) -> from the flower library by hadiyarajesh
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()


    single<ApiInterface> { retrofit.create(ApiInterface::class.java) }
    single { bearerInterceptor } // reference to the unique bearerInterceptor
}