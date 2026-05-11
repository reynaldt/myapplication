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
            val response = api.login(request)
            if (response.isSuccessful) {
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("Empty response body"))
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = try {
                    if (errorString != null) {
                        org.json.JSONObject(errorString).optString("message", "Login failed")
                    } else {
                        "Login failed with code: ${response.code()}"
                    }
                } catch (e: Exception) {
                    "Login failed with code: ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Network error occurred"))
        }
    }
}
