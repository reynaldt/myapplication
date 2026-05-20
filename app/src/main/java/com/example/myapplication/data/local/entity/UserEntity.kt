package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,   // SHA-256 hex of the password
    val role: String,           // ADMIN / STAFF / VIEWER
    val displayName: String,
    val createdAt: String
)
