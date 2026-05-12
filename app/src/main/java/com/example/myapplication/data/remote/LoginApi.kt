package com.example.myapplication.data.remote

import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
