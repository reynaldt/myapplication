package com.example.myapplication.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.AddInventoryResponse
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

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _addInventoryState = MutableStateFlow<AddInventoryState>(AddInventoryState.Idle)
    val addInventoryState: StateFlow<AddInventoryState> = _addInventoryState.asStateFlow()

    fun addInventory(type: String, description: String, pic: String, picture: File) {
        viewModelScope.launch {
            _addInventoryState.value = AddInventoryState.Loading
            repository.addInventory(type, description, pic, picture)
                .onSuccess { _addInventoryState.value = AddInventoryState.Success(it) }
                .onFailure { _addInventoryState.value = AddInventoryState.Error(it.message ?: "Unknown error") }
        }
    }

    fun resetState() {
        _addInventoryState.value = AddInventoryState.Idle
    }
}
