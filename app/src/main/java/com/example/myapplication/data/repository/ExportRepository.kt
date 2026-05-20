package com.example.myapplication.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepository(
    private val inventoryDao: InventoryDao,
    private val logDao: LogDao
) {

    private val dateStamp get() = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

    /**
     * Exports all inventory items as CSV.
     * Returns a content URI safe to share or open externally.
     */
    suspend fun exportInventoryCsv(context: Context): Result<Uri> {
        return try {
            val items = inventoryDao.getAllInventoryFlow().first()
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "inventory_export_$dateStamp.csv"
            )

            file.bufferedWriter().use { w ->
                // Header
                w.write("Inventory Code,Item Name,Category,Description,Quantity,Status,Movement,PIC,Notes,Created At,Updated At")
                w.newLine()
                // Rows
                items.forEach { item ->
                    w.write(buildCsvRow(
                        item.inventoryCode,
                        item.itemName,
                        ItemCategory.fromString(item.category).label,
                        item.itemDescription ?: "",
                        item.quantity.toString(),
                        ItemStatus.fromString(item.status).label,
                        item.movement ?: "",
                        item.pic ?: "",
                        item.notes ?: "",
                        item.createdAt ?: "",
                        item.updatedAt ?: ""
                    ))
                    w.newLine()
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(Exception("Export failed: ${e.message}"))
        }
    }

    /**
     * Exports all audit logs as CSV.
     */
    suspend fun exportLogsCsv(context: Context): Result<Uri> {
        return try {
            val logs = logDao.getAllLogsFlow().first()
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "logs_export_$dateStamp.csv"
            )

            file.bufferedWriter().use { w ->
                w.write("ID,Inventory Code,Item Name,Action,Performed By,Notes,Timestamp")
                w.newLine()
                logs.forEach { log ->
                    w.write(buildCsvRow(
                        log.id.toString(),
                        log.inventoryCode,
                        log.itemName,
                        log.action,
                        log.performedByUsername,
                        log.notes ?: "",
                        log.timestamp
                    ))
                    w.newLine()
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(Exception("Log export failed: ${e.message}"))
        }
    }

    /** Builds a safe CSV row, escaping commas and quotes inside values. */
    private fun buildCsvRow(vararg values: String): String =
        values.joinToString(",") { v ->
            val escaped = v.replace("\"", "\"\"")
            if (escaped.contains(',') || escaped.contains('"') || escaped.contains('\n'))
                "\"$escaped\""
            else escaped
        }

    /** Convenience: share the CSV via Android's share sheet. */
    fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share CSV"))
    }
}
