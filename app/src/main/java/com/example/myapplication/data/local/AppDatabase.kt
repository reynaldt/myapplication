package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.data.local.entity.LogEntity

@Database(entities = [InventoryEntity::class, LogEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun logDao(): LogDao
}
