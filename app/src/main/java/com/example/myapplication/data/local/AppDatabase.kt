package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.data.local.dao.UserDao
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.data.local.entity.LogEntity
import com.example.myapplication.data.local.entity.UserEntity
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Database(
    entities = [InventoryEntity::class, LogEntity::class, UserEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao
    abstract fun logDao(): LogDao
    abstract fun userDao(): UserDao

    companion object {

        /**
         * Migration from v2 → v3:
         * 1. Drop old checkout_logs table (incompatible schema)
         * 2. Recreate inventory_items with new columns
         * 3. Create new audit_logs table
         * 4. Create users table
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ── Drop old tables ───────────────────────────────────────────
                db.execSQL("DROP TABLE IF EXISTS `checkout_logs`")
                db.execSQL("DROP TABLE IF EXISTS `inventory_items`")

                // ── Re-create inventory_items with new schema ─────────────────
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `inventory_items` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `inventoryCode` TEXT NOT NULL DEFAULT '',
                        `itemName` TEXT NOT NULL DEFAULT '',
                        `category` TEXT NOT NULL DEFAULT 'GOODS',
                        `itemDescription` TEXT,
                        `quantity` INTEGER NOT NULL DEFAULT 1,
                        `status` TEXT NOT NULL DEFAULT 'AVAILABLE',
                        `pic` TEXT,
                        `picture` TEXT,
                        `movement` TEXT,
                        `notes` TEXT,
                        `createdAt` TEXT,
                        `updatedAt` TEXT
                    )
                """.trimIndent())

                // ── Create audit_logs ─────────────────────────────────────────
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `inventoryItemId` TEXT NOT NULL,
                        `inventoryCode` TEXT NOT NULL,
                        `itemName` TEXT NOT NULL,
                        `action` TEXT NOT NULL,
                        `performedByUserId` TEXT NOT NULL,
                        `performedByUsername` TEXT NOT NULL,
                        `notes` TEXT,
                        `photoPath` TEXT,
                        `timestamp` TEXT NOT NULL
                    )
                """.trimIndent())

                // ── Create users table ────────────────────────────────────────
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `users` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `username` TEXT NOT NULL,
                        `passwordHash` TEXT NOT NULL,
                        `role` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `createdAt` TEXT NOT NULL
                    )
                """.trimIndent())

                // ── Seed default users ────────────────────────────────────────
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val adminHash = sha256("admin")
                val userHash = sha256("user")

                db.execSQL("""
                    INSERT OR IGNORE INTO `users` (id, username, passwordHash, role, displayName, createdAt)
                    VALUES ('${UUID.randomUUID()}', 'admin', '$adminHash', 'ADMIN', 'Administrator', '$now')
                """.trimIndent())

                db.execSQL("""
                    INSERT OR IGNORE INTO `users` (id, username, passwordHash, role, displayName, createdAt)
                    VALUES ('${UUID.randomUUID()}', 'user', '$userHash', 'STAFF', 'Staff User', '$now')
                """.trimIndent())
            }
        }

        /**
         * Migration from v3 -> v4:
         * Adds stable identity tracking for the person-in-charge while preserving
         * the existing free-text PIC display value for old rows.
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `inventory_items` ADD COLUMN `picUserId` TEXT")
            }
        }

        /** Callback to seed users on fresh database creation (no prior version). */
        val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                val adminHash = sha256("admin")
                val userHash = sha256("user")

                db.execSQL("""
                    INSERT OR IGNORE INTO `users` (id, username, passwordHash, role, displayName, createdAt)
                    VALUES ('${UUID.randomUUID()}', 'admin', '$adminHash', 'ADMIN', 'Administrator', '$now')
                """.trimIndent())

                db.execSQL("""
                    INSERT OR IGNORE INTO `users` (id, username, passwordHash, role, displayName, createdAt)
                    VALUES ('${UUID.randomUUID()}', 'user', '$userHash', 'STAFF', 'Staff User', '$now')
                """.trimIndent())
            }
        }

        fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
