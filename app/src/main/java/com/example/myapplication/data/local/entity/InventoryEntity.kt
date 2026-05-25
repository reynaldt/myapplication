package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Inventory item entity.
 *
 * Inventory code format (12 chars): [2-char category code][4-digit seq][6-digit ddmmyy]
 * Example: GD000120052026 = Goods, item #1, created 20 May 2026
 *
 * DB version: introduced in version 3; picUserId added in version 4
 */
@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey val id: String,
    val inventoryCode: String,          // Auto-generated, e.g. "GD000120052026"
    val itemName: String,               // Human-readable item name
    val category: String,               // ItemCategory.name: GOODS/LETTER/CONSUMABLE/ASSET/OTHER
    val itemDescription: String?,       // Optional description / details
    val quantity: Int = 1,
    val status: String = "AVAILABLE",   // ItemStatus.name: AVAILABLE/CHECKED_OUT/LOST
    val pic: String?,                   // Person-in-charge display name who received / last handled
    val picUserId: String? = null,      // Stable user identity for the PIC, if matched to a profile
    val picture: String?,               // Absolute path to photo evidence
    val movement: String?,              // "inbound" | "outbound"
    val notes: String?,                 // Optional notes
    val createdAt: String?,
    val updatedAt: String?
)
