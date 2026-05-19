package com.example.myapplication.data.repository

import com.example.myapplication.data.model.AddInventoryResponse
import com.example.myapplication.data.model.InventoryListResponse
import com.example.myapplication.data.remote.InventoryApi
import com.example.myapplication.domain.repository.InventoryRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

class InventoryRepositoryImpl(
    private val dao: com.example.myapplication.data.local.dao.InventoryDao,
    private val logDao: com.example.myapplication.data.local.dao.LogDao
) : InventoryRepository {

    override suspend fun getInventory(): Result<InventoryListResponse> {
        return try {
            val entities = dao.getAllInventoryItems()
            val items = entities.map { entity ->
                com.example.myapplication.data.model.InventoryItem(
                    id = entity.id,
                    type = entity.type,
                    description = entity.description,
                    pic = entity.pic,
                    picture = entity.picture,
                    movement = entity.movement,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
            Result.success(
                InventoryListResponse(
                    code = 200,
                    message = "Success",
                    data = items
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Local DB error occurred"))
        }
    }

    override suspend fun addInventory(
        movementType: String,
        type: String,
        description: String,
        pic: String,
        picture: File
    ): Result<AddInventoryResponse> {
        return try {
            val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            val entity = com.example.myapplication.data.local.entity.InventoryEntity(
                id = System.currentTimeMillis().toString(),
                type = type,
                description = description,
                pic = pic,
                picture = picture.absolutePath,
                movement = movementType,
                createdAt = now,
                updatedAt = now
            )
            dao.insertInventoryItem(entity)
            Result.success(
                AddInventoryResponse(
                    status = true,
                    message = "Item saved to database"
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Local DB error occurred"))
        }
    }

    override suspend fun checkoutInventory(id: String, picName: String): Result<Boolean> {
        return try {
            val item = dao.getInventoryItemById(id)
            if (item != null) {
                val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                // Construct updated item with outbound movement
                val updatedItem = item.copy(
                    movement = "outbound",
                    pic = picName,
                    updatedAt = now
                )
                // Insert/Replace back into DB
                dao.insertInventoryItem(updatedItem)
                
                // Log the checkout
                val log = com.example.myapplication.data.local.entity.LogEntity(
                    message = "Item '${item.type}' (Description: ${item.description}) checked out by $picName",
                    timestamp = now
                )
                logDao.insertLog(log)
                Result.success(true)
            } else {
                Result.failure(Exception("Item not found"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Checkout error occurred"))
        }
    }

    override suspend fun getLogs(): Result<List<com.example.myapplication.data.local.entity.LogEntity>> {
        return try {
            val logs = logDao.getAllLogs()
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to get logs"))
        }
    }
}
