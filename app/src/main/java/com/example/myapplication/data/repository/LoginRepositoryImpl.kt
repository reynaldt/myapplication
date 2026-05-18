package com.example.myapplication.data.repository

import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse
import com.example.myapplication.data.remote.LoginApi
import com.example.myapplication.domain.repository.LoginRepository

class LoginRepositoryImpl(
    private val api: LoginApi
) : LoginRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            Result.success(
                LoginResponse(
                    status = true,
                    message = "Login offline success",
                    data = com.example.myapplication.data.model.LoginData(
                        token = "offline_token",
                        tokenType = "Bearer"
                    )
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Local error occurred"))
        }
    }
}
