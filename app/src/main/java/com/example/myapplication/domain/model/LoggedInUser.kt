package com.example.myapplication.domain.model

data class LoggedInUser(
    val id: String,
    val username: String,
    val displayName: String,
    val role: UserRole
)
