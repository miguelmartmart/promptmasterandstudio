package com.promptmaster.data.ads

data class Ad(
    val productId: String, // New field for product ID
    val imageUrl: String,
    val title: String,
    val description: String,
    val links: Map<String, String> // Changed to a map for geo-localized links
)
