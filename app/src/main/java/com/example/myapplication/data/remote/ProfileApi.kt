package com.example.myapplication.data.remote

import com.example.myapplication.data.model.ProfileResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProfileApi {
    @GET("profile")
    suspend fun getProfile(): Response<ProfileResponse>
}
