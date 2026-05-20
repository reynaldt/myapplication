package com.example.myapplication.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.InventoryDao
import com.example.myapplication.domain.model.ItemCategory
import com.example.myapplication.domain.model.ItemStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardStats(
    val totalItems: Int = 0,
    val availableItems: Int = 0,
    val checkedOutItems: Int = 0,
    val lostItems: Int = 0,
    val inboundCount: Int = 0,
    val outboundCount: Int = 0,
    val categoryBreakdown: Map<ItemCategory, Int> = emptyMap()
)

class DashboardViewModel(private val dao: InventoryDao) : ViewModel() {

    val stats: StateFlow<DashboardStats> = combine(
        dao.countAll(),
        dao.countByStatus(ItemStatus.AVAILABLE.name),
        dao.countByStatus(ItemStatus.CHECKED_OUT.name),
        dao.countByStatus(ItemStatus.LOST.name),
        dao.countInbound(),
        dao.countOutbound(),
        dao.countByCategory(ItemCategory.GOODS.name),
        dao.countByCategory(ItemCategory.LETTER.name),
        dao.countByCategory(ItemCategory.CONSUMABLE.name),
        dao.countByCategory(ItemCategory.ASSET.name),
        dao.countByCategory(ItemCategory.OTHER.name)
    ) { values ->
        val total        = values[0]
        val available    = values[1]
        val checkedOut   = values[2]
        val lost         = values[3]
        val inbound      = values[4]
        val outbound     = values[5]
        val goodsCount   = values[6]
        val letterCount  = values[7]
        val consumCount  = values[8]
        val assetCount   = values[9]
        val otherCount   = values[10]

        DashboardStats(
            totalItems = total,
            availableItems = available,
            checkedOutItems = checkedOut,
            lostItems = lost,
            inboundCount = inbound,
            outboundCount = outbound,
            categoryBreakdown = mapOf(
                ItemCategory.GOODS      to goodsCount,
                ItemCategory.LETTER     to letterCount,
                ItemCategory.CONSUMABLE to consumCount,
                ItemCategory.ASSET      to assetCount,
                ItemCategory.OTHER      to otherCount
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardStats()
    )
}
