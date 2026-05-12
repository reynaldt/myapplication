package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("status")
    val status: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: LoginData? = null
)
