package com.example.myapplication.domain.model

/**
 * Inventory item categories.
 * [code] is the 2-character prefix used in auto-generated inventory codes.
 * Inventory code format: [2-char category][4-digit seq][6-digit ddmmyy] = 12 chars total
 * Example: GD000120052026
 */
enum class ItemCategory(val code: String, val label: String) {
    GOODS("GD", "Goods"),
    LETTER("LT", "Letter / Document"),
    CONSUMABLE("CS", "Consumable"),
    ASSET("AS", "Asset"),
    OTHER("OT", "Other");

    companion object {
        fun fromCode(code: String): ItemCategory =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: OTHER

        fun fromString(value: String): ItemCategory =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER
    }
}
