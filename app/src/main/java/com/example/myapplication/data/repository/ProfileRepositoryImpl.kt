package com.example.myapplication.data.repository

import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.data.model.ProfileData
import com.example.myapplication.data.model.ProfileResponse
import com.example.myapplication.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val sessionManager: SessionManager
) : ProfileRepository {

    override suspend fun getProfile(): Result<ProfileResponse> {
        val user = sessionManager.getLoggedInUser()
            ?: return Result.failure(Exception("No active local session"))

        return Result.success(
            ProfileResponse(
                status = true,
                message = "Local profile loaded",
                data = ProfileData(
                    id = user.id,
                    username = user.username,
                    displayName = user.displayName,
                    role = user.role.label
                )
            )
        )
    }
}
