package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.ProfileResponse

interface ProfileRepository {
    suspend fun getProfile(): Result<ProfileResponse>
}
