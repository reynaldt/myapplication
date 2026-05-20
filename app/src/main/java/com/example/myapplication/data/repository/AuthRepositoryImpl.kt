package com.example.myapplication.data.repository

import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.LoggedInUser
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val userDao: com.example.myapplication.data.local.dao.UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<LoggedInUser> {
        return try {
            val user = userDao.findByUsername(username)
                ?: return Result.failure(Exception("User not found. Please check your username."))

            val inputHash = AppDatabase.sha256(password)
            if (user.passwordHash != inputHash) {
                return Result.failure(Exception("Wrong password. Please try again."))
            }

            val loggedInUser = LoggedInUser(
                id = user.id,
                username = user.username,
                displayName = user.displayName,
                role = UserRole.fromString(user.role)
            )
            sessionManager.saveSession(user.id, user.username, user.role, user.displayName)
            Result.success(loggedInUser)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Login error occurred"))
        }
    }

    override fun logout() {
        sessionManager.clearSession()
    }

    override fun getCurrentUser(): LoggedInUser? = sessionManager.getLoggedInUser()
}
