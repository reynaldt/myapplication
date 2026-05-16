package com.example.myapplication.domain.repository

import com.example.myapplication.data.model.AddInventoryResponse
import java.io.File

interface InventoryRepository {
    suspend fun addInventory(
        type: String,
        description: String,
        pic: String,
        picture: File
    ): Result<AddInventoryResponse>
}
