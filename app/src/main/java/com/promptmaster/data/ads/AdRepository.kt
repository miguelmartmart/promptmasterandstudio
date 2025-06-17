package com.promptmaster.data.ads

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.Locale // Import Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient, // Keep OkHttpClient for potential future scraping or other network needs
    private val gson: Gson
) {
    private val cachedAds = mutableListOf<Ad>()
    private var manualAdsConfig: Map<String, ManualAdData> = emptyMap()

    init {
        loadManualAdsConfig()
    }

    private fun loadManualAdsConfig() {
        try {
            val jsonString = context.assets.open("manual_ads_config.json").bufferedReader().use { it.readText() }
            // Updated TypeToken to match the new JSON structure with productId as key
            val type = object : TypeToken<Map<String, ManualAdData>>() {}.type
            manualAdsConfig = gson.fromJson(jsonString, type)
            Log.d("AdRepository", "Manual ads config loaded: ${manualAdsConfig.size} entries.")
        } catch (e: Exception) {
            Log.e("AdRepository", "Error loading manual_ads_config.json", e)
            manualAdsConfig = emptyMap()
        }
    }

    suspend fun getAds(): List<Ad> = withContext(Dispatchers.IO) {
        Log.d("AdRepository", "getAds() called. Cached ads size: ${cachedAds.size}")
        if (cachedAds.isNotEmpty()) {
            Log.d("AdRepository", "Returning cached ads.")
            return@withContext cachedAds
        }

        val fetchedAds = mutableListOf<Ad>()
        val userCountryCode = getUserCountryCode()
        Log.d("AdRepository", "Detected user country code: $userCountryCode")

        // Iterate through manual ads config to create Ad objects
        for ((productId, manualData) in manualAdsConfig) {
            val selectedLink = manualData.links[userCountryCode] ?: manualData.links["default"]
            if (selectedLink != null) {
                val ad = Ad(
                    productId = productId,
                    imageUrl = manualData.imageUrl ?: "https://via.placeholder.com/150/CCCCCC/000000?text=No+Image",
                    title = manualData.title ?: "Product Title Not Found",
                    description = manualData.description ?: "No description available.",
                    links = manualData.links // Pass the entire links map
                )
                fetchedAds.add(ad)
                Log.d("AdRepository", "Successfully created ad for $productId (Country: $userCountryCode, Link: $selectedLink)")
            } else {
                Log.w("AdRepository", "No suitable link found for product $productId in country $userCountryCode. Skipping ad.")
            }
        }

        cachedAds.clear()
        cachedAds.addAll(fetchedAds)
        Log.d("AdRepository", "Finished fetching ads. Total fetched: ${fetchedAds.size}")
        return@withContext fetchedAds
    }

    // Helper function to get the user's country code
    private fun getUserCountryCode(): String {
        return Locale.getDefault().country.uppercase(Locale.ROOT)
    }
}

// Data class to represent the structure of manual ad data in JSON
data class ManualAdData(
    val imageUrl: String?,
    val title: String?,
    val description: String?,
    val links: Map<String, String> // New field for geo-localized links
)
