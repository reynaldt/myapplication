package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.LoggedInUser

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<LoggedInUser>
    fun logout()
    fun getCurrentUser(): LoggedInUser?
}
