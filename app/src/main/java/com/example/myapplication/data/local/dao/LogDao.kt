package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.local.entity.LogEntity

@Dao
interface LogDao {
    @Query("SELECT * FROM checkout_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<LogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: LogEntity)
}
