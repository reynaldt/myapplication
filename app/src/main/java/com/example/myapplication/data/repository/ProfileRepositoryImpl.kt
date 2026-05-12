package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ProfileResponse
import com.example.myapplication.data.remote.ProfileApi
import com.example.myapplication.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = api.getProfile()
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorString != null) {
                        org.json.JSONObject(errorString).optString("message", "Failed to load profile")
                    } else {
                        "Failed with code: ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Failed with code: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error occurred"))
        }
    }
}
