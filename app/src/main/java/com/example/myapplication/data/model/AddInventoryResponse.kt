package com.example.myapplication.data.model

import com.google.gson.annotations.SerializedName

data class AddInventoryResponse(
    @SerializedName("status")
    val status: Boolean? = null,
    @SerializedName("message")
    val message: String? = null
)
