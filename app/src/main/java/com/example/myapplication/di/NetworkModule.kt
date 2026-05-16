package com.example.myapplication.di

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.myapplication.AppConfig
import com.example.myapplication.data.remote.InventoryApi
import com.example.myapplication.data.remote.LoginApi
import com.example.myapplication.data.remote.ProfileApi
import com.example.myapplication.data.local.SessionManager

val networkModule = module {
    single {
        val sessionManager: SessionManager = get()
        val loggingInterceptor = if (AppConfig.IS_DEBUG) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else null
        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("X-API-KEY", "tBHAVS6pfrcHUStHflb40vWeQn61lLgN")
            
            val token = sessionManager.fetchAuthToken()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
        if (loggingInterceptor != null) {
            clientBuilder.addInterceptor(loggingInterceptor)
        }
        clientBuilder.build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://itga.accentuates.co.id/itga/")
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<LoginApi> {
        get<Retrofit>().create(LoginApi::class.java)
    }

    single<ProfileApi> {
        get<Retrofit>().create(ProfileApi::class.java)
    }

    single<InventoryApi> {
        get<Retrofit>().create(InventoryApi::class.java)
    }
}
