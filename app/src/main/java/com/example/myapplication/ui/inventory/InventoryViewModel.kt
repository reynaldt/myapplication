package com.example.myapplication.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AddInventoryResponse
import com.example.myapplication.data.model.InventoryListResponse
import com.example.myapplication.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class AddInventoryState {
    object Idle : AddInventoryState()
    object Loading : AddInventoryState()
    data class Success(val response: AddInventoryResponse) : AddInventoryState()
    data class Error(val message: String) : AddInventoryState()
}

sealed class InventoryListState {
    object Idle : InventoryListState()
    object Loading : InventoryListState()
    data class Success(val response: InventoryListResponse) : InventoryListState()
    data class Error(val message: String) : InventoryListState()
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _addInventoryState = MutableStateFlow<AddInventoryState>(AddInventoryState.Idle)
    val addInventoryState: StateFlow<AddInventoryState> = _addInventoryState.asStateFlow()

    private val _inventoryListState = MutableStateFlow<InventoryListState>(InventoryListState.Idle)
    val inventoryListState: StateFlow<InventoryListState> = _inventoryListState.asStateFlow()

    private val _checkoutState = MutableStateFlow<AddInventoryState>(AddInventoryState.Idle)
    val checkoutState: StateFlow<AddInventoryState> = _checkoutState.asStateFlow()

    private val _logsState = MutableStateFlow<List<com.example.myapplication.data.local.entity.LogEntity>>(emptyList())
    val logsState: StateFlow<List<com.example.myapplication.data.local.entity.LogEntity>> = _logsState.asStateFlow()

    init {
        loadInventory()
    }

    fun loadInventory() {
        viewModelScope.launch {
            _inventoryListState.value = InventoryListState.Loading
            repository.getInventory()
                .onSuccess { _inventoryListState.value = InventoryListState.Success(it) }
                .onFailure { _inventoryListState.value = InventoryListState.Error(it.message ?: "Unknown error") }
        }
    }

    fun addInventory(movementType: String, type: String, description: String, pic: String, picture: File) {
        viewModelScope.launch {
            _addInventoryState.value = AddInventoryState.Loading
            repository.addInventory(movementType, type, description, pic, picture)
                .onSuccess { 
                    _addInventoryState.value = AddInventoryState.Success(it)
                    loadInventory()
                }
                .onFailure { _addInventoryState.value = AddInventoryState.Error(it.message ?: "Unknown error") }
        }
    }

    fun checkoutItem(id: String, picName: String) {
        viewModelScope.launch {
            _checkoutState.value = AddInventoryState.Loading
            repository.checkoutInventory(id, picName)
                .onSuccess {
                    _checkoutState.value = AddInventoryState.Success(AddInventoryResponse(true, "Checkout successful"))
                    loadInventory()
                    loadLogs()
                }
                .onFailure { _checkoutState.value = AddInventoryState.Error(it.message ?: "Unknown error") }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            repository.getLogs()
                .onSuccess { _logsState.value = it }
        }
    }

    fun resetAddState() {
        _addInventoryState.value = AddInventoryState.Idle
    }
    
    fun resetCheckoutState() {
        _checkoutState.value = AddInventoryState.Idle
    }
}
