package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY createdAt DESC")
    suspend fun getAllInventoryItems(): List<InventoryEntity>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: String): InventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteInventoryItem(id: String)

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()
}
