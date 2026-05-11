package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.LoginRequest
import com.example.myapplication.data.model.LoginResponse

interface LoginRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
}
