package com.example.myapplication.domain.model

enum class UserRole(val label: String) {
    ADMIN("Admin"),
    STAFF("Staff"),
    VIEWER("Viewer");

    fun canCheckIn(): Boolean = this == ADMIN || this == STAFF
    fun canCheckOut(): Boolean = this == ADMIN || this == STAFF
    fun canDeleteItem(): Boolean = this == ADMIN
    fun canManageUsers(): Boolean = this == ADMIN
    fun canExportReports(): Boolean = this == ADMIN || this == STAFF
    fun canViewLogs(): Boolean = true       // all roles
    fun canViewInventory(): Boolean = true  // all roles

    companion object {
        fun fromString(value: String): UserRole =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: VIEWER
    }
}
