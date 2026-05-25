package com.example.myapplication.data.repository

import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.data.local.entity.LogEntity
import com.example.myapplication.data.util.InventoryCodeGenerator
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import com.example.myapplication.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class InventoryRepositoryImpl(
    private val dao: InventoryDao,
    private val logDao: LogDao,
    private val sessionManager: SessionManager
) : InventoryRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // ── Queries ──────────────────────────────────────────────────────────────

    override fun searchInventory(
        query: String,
        category: String,
        status: String,
        movement: String
    ): Flow<List<InventoryEntity>> = dao.searchInventory(query, category, status, movement)

    override fun getAllInventoryFlow(): Flow<List<InventoryEntity>> = dao.getAllInventoryFlow()

    // ── Add inbound item ─────────────────────────────────────────────────────

    override suspend fun addInventory(
        category: ItemCategory,
        itemName: String,
        itemDescription: String,
        pic: String,
        picture: File,
        notes: String,
        quantity: Int
    ): Result<InventoryEntity> {
        return try {
            val picUser = sessionManager.getLoggedInUser()
                ?: return Result.failure(Exception("Login session required to assign PIC"))
            val now = dateFormat.format(Date())
            val catDateSuffix = java.text.SimpleDateFormat("ddMMyy", Locale.getDefault()).format(Date())

            // Generate unique 12-char inventory code
            val existingCount = dao.countItemsForCodeGeneration(category.code, catDateSuffix)
            val inventoryCode = InventoryCodeGenerator.generate(category, existingCount)

            val entity = InventoryEntity(
                id = UUID.randomUUID().toString(),
                inventoryCode = inventoryCode,
                itemName = itemName.trim(),
                category = category.name,
                itemDescription = itemDescription.trim().ifBlank { null },
                quantity = quantity,
                status = ItemStatus.AVAILABLE.name,
                pic = picUser.displayName,
                picUserId = picUser.id,
                picture = picture.absolutePath,
                movement = "inbound",
                notes = notes.trim().ifBlank { null },
                createdAt = now,
                updatedAt = now
            )
            dao.insertInventoryItem(entity)

            // Write audit log
            insertLog(
                itemId = entity.id,
                inventoryCode = entity.inventoryCode,
                itemName = entity.itemName,
                action = "CHECK_IN",
                notes = notes.ifBlank { null }
            )

            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to add item"))
        }
    }

    // ── Checkout (outbound) ──────────────────────────────────────────────────

    override suspend fun checkoutInventory(
        id: String,
        picName: String,
        notes: String,
        photoFile: File?
    ): Result<Boolean> {
        return try {
            val picUser = sessionManager.getLoggedInUser()
                ?: return Result.failure(Exception("Login session required to assign PIC"))
            val item = dao.getInventoryItemById(id)
                ?: return Result.failure(Exception("Item not found"))

            val now = dateFormat.format(Date())
            dao.updateInventoryItem(
                item.copy(
                    movement = "outbound",
                    status = ItemStatus.CHECKED_OUT.name,
                    pic = picUser.displayName,
                    picUserId = picUser.id,
                    notes = notes.trim().ifBlank { item.notes },
                    updatedAt = now
                )
            )

            insertLog(
                itemId = item.id,
                inventoryCode = item.inventoryCode,
                itemName = item.itemName,
                action = "CHECK_OUT",
                notes = "Released to ${picUser.displayName}${if (notes.isNotBlank()) " - $notes" else ""}",
                photoPath = photoFile?.absolutePath
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Checkout error"))
        }
    }

    // ── Mark lost ────────────────────────────────────────────────────────────

    override suspend fun markItemLost(id: String, notes: String): Result<Boolean> {
        return try {
            val item = dao.getInventoryItemById(id)
                ?: return Result.failure(Exception("Item not found"))

            val now = dateFormat.format(Date())
            dao.updateInventoryItem(
                item.copy(
                    status = ItemStatus.LOST.name,
                    notes = notes.trim().ifBlank { item.notes },
                    updatedAt = now
                )
            )

            insertLog(
                itemId = item.id,
                inventoryCode = item.inventoryCode,
                itemName = item.itemName,
                action = "MARK_LOST",
                notes = notes.ifBlank { "Marked as lost" }
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to mark item as lost"))
        }
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    override suspend fun deleteInventory(id: String): Result<Boolean> {
        return try {
            val item = dao.getInventoryItemById(id)
            if (item != null) {
                insertLog(
                    itemId = item.id,
                    inventoryCode = item.inventoryCode,
                    itemName = item.itemName,
                    action = "DELETE",
                    notes = "Item deleted"
                )
            }
            dao.deleteInventoryItem(id)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Delete error"))
        }
    }

    // ── Logs ─────────────────────────────────────────────────────────────────

    override suspend fun getLogs(): Result<List<LogEntity>> {
        return try {
            // Kept for backward compat; prefer LogDao.getAllLogsFlow() directly
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to get logs"))
        }
    }

    // ── Private helper ───────────────────────────────────────────────────────

    private suspend fun insertLog(
        itemId: String,
        inventoryCode: String,
        itemName: String,
        action: String,
        notes: String? = null,
        photoPath: String? = null
    ) {
        val user = sessionManager.getLoggedInUser()
        logDao.insertLog(
            LogEntity(
                inventoryItemId = itemId,
                inventoryCode = inventoryCode,
                itemName = itemName,
                action = action,
                performedByUserId = user?.id ?: "system",
                performedByUsername = user?.username ?: "system",
                notes = notes,
                photoPath = photoPath,
                timestamp = dateFormat.format(Date())
            )
        )
    }
}
