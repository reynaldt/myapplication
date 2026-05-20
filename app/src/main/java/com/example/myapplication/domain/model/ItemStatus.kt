package com.example.myapplication.domain.model

enum class ItemStatus(val value: String, val label: String) {
    AVAILABLE("available", "Available"),
    CHECKED_OUT("checked_out", "Checked Out"),
    LOST("lost", "Lost");

    companion object {
        fun fromString(value: String): ItemStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: AVAILABLE
    }
}
