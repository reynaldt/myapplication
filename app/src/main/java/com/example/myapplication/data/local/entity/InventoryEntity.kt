package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey val id: String,
    val type: String?,
    val description: String?,
    val pic: String?,
    val picture: String?,
    val movement: String?,
    val createdAt: String?,
    val updatedAt: String?
)
