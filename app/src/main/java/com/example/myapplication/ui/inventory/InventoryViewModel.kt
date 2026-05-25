package com.example.myapplication.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.entity.InventoryEntity
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import com.example.myapplication.domain.model.SearchFilterState
import com.example.myapplication.domain.repository.InventoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

// ── UI State ──────────────────────────────────────────────────────────────────

sealed class OperationState {
    object Idle : OperationState()
    object Loading : OperationState()
    data class Success(val message: String) : OperationState()
    data class Error(val message: String) : OperationState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalCoroutinesApi::class)
class InventoryViewModel(
    private val repository: InventoryRepository,
    private val sessionManager: com.example.myapplication.data.local.SessionManager
) : ViewModel() {

    // ── Filter state ─────────────────────────────────────────────────────────

    private val _filter = MutableStateFlow(SearchFilterState())
    val filter: StateFlow<SearchFilterState> = _filter.asStateFlow()
    val currentUser = sessionManager.currentUser

    // ── Reactive inventory list (updates on filter change OR DB change) ───────

    val inventoryList: StateFlow<List<InventoryEntity>> = _filter
        .flatMapLatest { f ->
            repository.searchInventory(
                query    = f.query,
                category = f.category?.name ?: "",
                status   = f.status?.name ?: "",
                movement = f.movement ?: ""
            )
        }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Convenience: inbound only
    val inboundList: StateFlow<List<InventoryEntity>> = inventoryList
        .map { it.filter { item -> item.movement == "inbound" && item.status == ItemStatus.AVAILABLE.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Convenience: outbound only
    val outboundList: StateFlow<List<InventoryEntity>> = inventoryList
        .map { it.filter { item -> item.movement == "outbound" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Operation state ───────────────────────────────────────────────────────

    private val _operationState = MutableStateFlow<OperationState>(OperationState.Idle)
    val operationState: StateFlow<OperationState> = _operationState.asStateFlow()

    // ── Filter actions ────────────────────────────────────────────────────────

    fun updateSearchQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun updateCategoryFilter(category: ItemCategory?) {
        _filter.value = _filter.value.copy(category = category)
    }

    fun updateStatusFilter(status: ItemStatus?) {
        _filter.value = _filter.value.copy(status = status)
    }

    fun updateMovementFilter(movement: String?) {
        _filter.value = _filter.value.copy(movement = movement)
    }

    fun clearFilters() {
        _filter.value = SearchFilterState()
    }

    // ── CRUD operations ───────────────────────────────────────────────────────

    fun addInventory(
        category: ItemCategory,
        itemName: String,
        itemDescription: String,
        pic: String,
        picture: File,
        notes: String = "",
        quantity: Int = 1
    ) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.addInventory(category, itemName, itemDescription, pic, picture, notes, quantity)
                .onSuccess { entity ->
                    _operationState.value = OperationState.Success(
                        "Item '${entity.itemName}' added with code ${entity.inventoryCode}"
                    )
                }
                .onFailure { _operationState.value = OperationState.Error(it.message ?: "Error") }
        }
    }

    fun checkoutItem(id: String, picName: String, notes: String = "", photoFile: File? = null) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.checkoutInventory(id, picName, notes, photoFile)
                .onSuccess { _operationState.value = OperationState.Success("Checkout successful") }
                .onFailure { _operationState.value = OperationState.Error(it.message ?: "Error") }
        }
    }

    fun markItemLost(id: String, notes: String = "") {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.markItemLost(id, notes)
                .onSuccess { _operationState.value = OperationState.Success("Item marked as lost") }
                .onFailure { _operationState.value = OperationState.Error(it.message ?: "Error") }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            _operationState.value = OperationState.Loading
            repository.deleteInventory(id)
                .onSuccess { _operationState.value = OperationState.Success("Item deleted") }
                .onFailure { _operationState.value = OperationState.Error(it.message ?: "Error") }
        }
    }

    fun resetOperationState() {
        _operationState.value = OperationState.Idle
    }

    // Kept for legacy callers
    fun loadInventory() { /* No-op — list is now reactive via Flow */ }

    // Legacy state getters for screens still using old pattern
    val addInventoryState get() = operationState
    val checkoutState get() = operationState
}
