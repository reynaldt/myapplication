package com.example.myapplication.data.util

import com.example.myapplication.domain.model.ItemCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates an inventory code in the format:
 *   [2-char category code][4-digit sequence][6-digit ddmmyy]
 *   Total: 12 characters
 *   Example: GD000120052026
 *
 * @param category     The item category (determines the 2-char prefix)
 * @param existingCount The count of items already sharing the same category + date (used for sequence)
 */
object InventoryCodeGenerator {

    private val dateFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())

    fun generate(category: ItemCategory, existingCount: Int): String {
        val catCode = category.code                     // e.g. "GD"
        val sequence = "%04d".format(existingCount + 1) // e.g. "0001"
        val date = dateFormat.format(Date())             // e.g. "200526"
        return "$catCode$sequence$date"                  // e.g. "GD0001200526"
    }
}
