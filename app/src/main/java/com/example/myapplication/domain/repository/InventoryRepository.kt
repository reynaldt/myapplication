package com.example.myapplication.domain.repository

interface InventoryRepository {
    // Search / filter
    fun searchInventory(
        query: String = "",
        category: String = "",
        status: String = "",
        movement: String = ""
    ): kotlinx.coroutines.flow.Flow<List<com.example.myapplication.data.local.entity.InventoryEntity>>

    fun getAllInventoryFlow(): kotlinx.coroutines.flow.Flow<List<com.example.myapplication.data.local.entity.InventoryEntity>>

    // CRUD
    suspend fun addInventory(
        category: com.example.myapplication.domain.model.ItemCategory,
        itemName: String,
        itemDescription: String,
        pic: String,
        picture: java.io.File,
        notes: String,
        quantity: Int
    ): Result<com.example.myapplication.data.local.entity.InventoryEntity>

    suspend fun checkoutInventory(
        id: String,
        picName: String,
        notes: String,
        photoFile: java.io.File?
    ): Result<Boolean>

    suspend fun markItemLost(id: String, notes: String): Result<Boolean>

    suspend fun deleteInventory(id: String): Result<Boolean>

    suspend fun getLogs(): Result<List<com.example.myapplication.data.local.entity.LogEntity>>
}
