package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Audit log entry for every action performed on an inventory item.
 * User identity is denormalized (copied at write time) to ensure logs
 * remain accurate even if a user is later renamed or deleted.
 *
 * DB version: redesigned in version 3
 */
@Entity(tableName = "audit_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val inventoryItemId: String,        // FK reference to inventory_items.id
    val inventoryCode: String,          // Denormalized for display (e.g. "GD000120052026")
    val itemName: String,               // Denormalized for display
    val action: String,                 // CHECK_IN | CHECK_OUT | UPDATE | DELETE | MARK_LOST
    val performedByUserId: String,
    val performedByUsername: String,
    val notes: String?,
    val photoPath: String?,             // Optional evidence photo (absolute path)
    val timestamp: String
)
