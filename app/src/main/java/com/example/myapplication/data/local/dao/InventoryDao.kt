package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    /**
     * Reactive filtered query. Empty string parameters act as "no filter" wildcards.
     * Emits a new list every time matching rows change.
     */
    @Query("""
        SELECT * FROM inventory_items
        WHERE (:query = '' OR itemName LIKE '%' || :query || '%'
                           OR inventoryCode LIKE '%' || :query || '%'
                           OR itemDescription LIKE '%' || :query || '%')
        AND (:category = '' OR category = :category)
        AND (:status = '' OR status = :status)
        AND (:movement = '' OR movement = :movement)
        ORDER BY createdAt DESC
    """)
    fun searchInventory(
        query: String,
        category: String,
        status: String,
        movement: String
    ): Flow<List<InventoryEntity>>

    /** All items as a reactive stream (for Dashboard / full list). */
    @Query("SELECT * FROM inventory_items ORDER BY createdAt DESC")
    fun getAllInventoryFlow(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: String): InventoryEntity?

    // ── Dashboard counts ─────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM inventory_items")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE status = :status")
    fun countByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE category = :category")
    fun countByCategory(category: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE movement = 'inbound'")
    fun countInbound(): Flow<Int>

    @Query("SELECT COUNT(*) FROM inventory_items WHERE movement = 'outbound'")
    fun countOutbound(): Flow<Int>

    // ── Inventory code generation ─────────────────────────────────────────────

    /**
     * Returns the highest sequential number used for a given category code + date suffix.
     * Used when auto-generating the next inventory code.
     */
    @Query("""
        SELECT COUNT(*) FROM inventory_items
        WHERE inventoryCode LIKE :categoryCode || '%' || :dateSuffix
    """)
    suspend fun countItemsForCodeGeneration(categoryCode: String, dateSuffix: String): Int

    // ── Mutations ─────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity)

    @Update
    suspend fun updateInventoryItem(item: InventoryEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItem(id: String)

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()
}

