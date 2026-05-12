package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("status")
    val status: Boolean? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    val data: ProfileData? = null
)

data class ProfileData(
    @SerializedName("id_mitra")
    val idMitra: String? = null,
    @SerializedName("mitra_name")
    val mitraName: String? = null,
    @SerializedName("email")
    val email: String? = null
)
