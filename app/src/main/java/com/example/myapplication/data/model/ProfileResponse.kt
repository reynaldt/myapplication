package com.example.myapplication.data.model

data class ProfileResponse(
    val status: Boolean? = null,
    val message: String? = null,
    val data: ProfileData? = null
)

data class ProfileData(
    val id: String? = null,
    val username: String? = null,
    val displayName: String? = null,
    val role: String? = null
)
