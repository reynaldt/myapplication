package com.example.myapplication.di

import com.example.myapplication.AppConfig
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.data.remote.InventoryApi
import com.example.myapplication.data.remote.LoginApi
import com.example.myapplication.data.remote.ProfileApi
import com.example.myapplication.network.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val networkModule = module {
    single {
        val sessionManager: SessionManager = get()
        val loggingInterceptor = if (AppConfig.IS_DEBUG) {
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
        } else null

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))

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
