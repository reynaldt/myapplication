package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ProfileResponse
import com.example.myapplication.data.remote.ProfileApi
import com.example.myapplication.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            Result.success(
                ProfileResponse(
                    status = true,
                    message = "Success",
                    data = com.example.myapplication.data.model.ProfileData(
                        idMitra = "offline_01",
                        mitraName = "Offline User",
                        email = "offline@example.com"
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Local error occurred"))
        }
    }
}
