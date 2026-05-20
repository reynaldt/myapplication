package com.example.myapplication.domain.model

data class SearchFilterState(
    val query: String = "",
    val category: ItemCategory? = null,
    val status: ItemStatus? = null,
    val movement: String? = null  // "inbound" | "outbound" | null = all
)
