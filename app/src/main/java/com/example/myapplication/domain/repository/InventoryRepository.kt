package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.AddInventoryResponse
import com.example.myapplication.data.model.InventoryListResponse
import java.io.File

interface InventoryRepository {
    suspend fun getInventory(): Result<InventoryListResponse>

    suspend fun addInventory(
        movementType: String,
        type: String,
        description: String,
        pic: String,
        picture: File
    ): Result<AddInventoryResponse>

    suspend fun checkoutInventory(id: String, picName: String): Result<Boolean>

    suspend fun getLogs(): Result<List<com.example.myapplication.data.local.entity.LogEntity>>
}
