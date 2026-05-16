package com.example.myapplication.data.model

data class InventoryListResponse(
    val code: Int? = null,
    val message: String? = null,
    val data: List<InventoryItem>? = null
)

data class InventoryItem(
    val id: String? = null,
    val type: String? = null,
    val description: String? = null,
    val pic: String? = null,
    val picture: String? = null,
    val movement: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
