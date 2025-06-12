package com.promptmaster.data

data class PromptFilters(
    val searchQuery: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val showFavoritesOnly: Boolean = false,
    val page: Int = 0
)
