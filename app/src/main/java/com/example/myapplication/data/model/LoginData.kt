package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("token")
    val token: String? = null,
    @SerializedName("token_type")
    val tokenType: String? = null
)
