package com.example.myapplication.network

import com.example.myapplication.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
            .header("X-API-KEY", "tBHAVS6pfrcHUStHflb40vWeQn61lLgN")

        // Auth token removed — app is now offline-first; bearer token no longer required

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            sessionManager.clearSession()
        }

        return response
    }
}
