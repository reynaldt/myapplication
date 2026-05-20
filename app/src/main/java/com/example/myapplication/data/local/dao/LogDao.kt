package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.LogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<LogEntity>>

    @Query("SELECT * FROM audit_logs WHERE inventoryItemId = :itemId ORDER BY timestamp DESC")
    fun getLogsForItem(itemId: String): Flow<List<LogEntity>>

    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY timestamp DESC")
    fun getLogsByAction(action: String): Flow<List<LogEntity>>

    @Query("SELECT COUNT(*) FROM audit_logs")
    fun countAll(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)
}
