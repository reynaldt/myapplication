package com.example.myapplication.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.LogDao
import com.example.myapplication.data.local.entity.LogEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LogViewModel(private val logDao: LogDao) : ViewModel() {

    val allLogs: StateFlow<List<LogEntity>> = logDao.getAllLogsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun getLogsForItem(itemId: String) = logDao.getLogsForItem(itemId)

    fun getLogsByAction(action: String) = logDao.getLogsByAction(action)
}
