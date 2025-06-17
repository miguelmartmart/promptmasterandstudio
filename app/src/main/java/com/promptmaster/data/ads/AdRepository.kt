package com.promptmaster.data.ads

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
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

        val affiliateLinks = readAffiliateLinks()
        Log.d("AdRepository", "Read ${affiliateLinks.size} affiliate links: $affiliateLinks")
        val fetchedAds = mutableListOf<Ad>()

        for (link in affiliateLinks) {
            Log.d("AdRepository", "Fetching details for link: $link")
            val ad = fetchAdDetails(link)
            if (ad != null) {
                fetchedAds.add(ad)
                Log.d("AdRepository", "Successfully fetched ad for $link: Title='${ad.title}', Image='${ad.imageUrl}'")
            } else {
                Log.w("AdRepository", "Failed to fetch ad details for $link. Ad object is null.")
            }
        }
        cachedAds.clear()
        cachedAds.addAll(fetchedAds)
        Log.d("AdRepository", "Finished fetching ads. Total fetched: ${fetchedAds.size}")
        return@withContext fetchedAds
    }

    private fun readAffiliateLinks(): List<String> {
        return try {
            context.assets.open("affiliate_links.txt").bufferedReader().useLines { it.toList() }
        } catch (e: Exception) {
            Log.e("AdRepository", "Error reading affiliate_links.txt", e)
            emptyList()
        }
    }

    private fun fetchAdDetails(url: String): Ad? {
        var scrapedTitle: String? = null
        var scrapedImageUrl: String? = null
        var scrapedDescription: String? = null

        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            val html = if (response.isSuccessful) {
                response.body?.string()
            } else {
                Log.e("AdRepository", "Unsuccessful HTTP response for $url: ${response.code}")
                null
            }

            if (html != null) {
                val document = Jsoup.parse(html)
                Log.d("AdRepository", "HTML parsed for $url. Attempting scraping.")

                scrapedTitle = document.select("meta[property=og:title]").attr("content").ifEmpty {
                    document.title().ifEmpty { "" }
                }
                scrapedImageUrl = document.select("meta[property=og:image]").attr("content").ifEmpty {
                    document.select("img[src]").first()?.attr("src").orEmpty()
                }
                scrapedDescription = document.select("meta[property=og:description]").attr("content").ifEmpty {
                    document.select("meta[name=description]").attr("content").ifEmpty {
                        document.select("div[itemprop=description]").first()?.text()?.trim().orEmpty()
                    }
                }

                Log.d("AdRepository", "Scraped (initial): Title='${scrapedTitle}', Image='${scrapedImageUrl}', Desc='${scrapedDescription}' for $url")

                if (scrapedImageUrl?.startsWith("//") == true) {
                    scrapedImageUrl = "https:$scrapedImageUrl"
                    Log.d("AdRepository", "Image URL updated to absolute: $scrapedImageUrl for $url")
                }

                // Fallback for Amazon specific title if generic fails and it's an Amazon link
                if (scrapedTitle.isNullOrEmpty() && (url.contains("amazon.com") || url.contains("amazon.es"))) {
                    val amazonTitle = document.select("#productTitle").first()?.text()?.trim().orEmpty()
                    if (amazonTitle.isNotEmpty()) {
                        scrapedTitle = amazonTitle
                        Log.d("AdRepository", "Applied Amazon title fallback: $scrapedTitle for $url")
                    }
                }

                // Fallback for Amazon specific image if generic fails and it's an Amazon link
                if (scrapedImageUrl.isNullOrEmpty() && (url.contains("amazon.com") || url.contains("amazon.es"))) {
                    val amazonDynamicImage = document.select("#landingImage").first()?.attr("data-a-dynamic-image")
                        ?.let { parseAmazonDynamicImageUrl(it) }
                        .orEmpty()
                    if (amazonDynamicImage.isNotEmpty()) {
                        scrapedImageUrl = amazonDynamicImage
                        Log.d("AdRepository", "Applied Amazon dynamic image fallback: $scrapedImageUrl for $url")
                    } else {
                        val amazonFallbackImage = document.select("#imgBlkFront").first()?.attr("src").orEmpty()
                        if (amazonFallbackImage.isNotEmpty()) {
                            scrapedImageUrl = amazonFallbackImage
                            Log.d("AdRepository", "Applied Amazon #imgBlkFront fallback: $scrapedImageUrl for $url")
                        }
                    }
                }
            } else {
                Log.e("AdRepository", "HTML content is null for $url. Cannot scrape.")
            }
        } catch (e: Exception) {
            Log.e("AdRepository", "Error during scraping for $url", e)
        }

        // Apply manual overrides for missing fields, or if scraped data is generic/undesirable
        val manualData = manualAdsConfig[url]
        Log.d("AdRepository", "Checking manual overrides for $url. Manual data found: ${manualData != null}")

        val finalTitle = manualData?.title.takeIf { !it.isNullOrEmpty() } ?: scrapedTitle.takeIf { !it.isNullOrEmpty() } ?: "Product Title Not Found"
        val finalImageUrl = manualData?.imageUrl.takeIf { !it.isNullOrEmpty() } ?: scrapedImageUrl.takeIf { !it.isNullOrEmpty() } ?: "https://via.placeholder.com/150/CCCCCC/000000?text=No+Image"
        val finalDescription = manualData?.description.takeIf { !it.isNullOrEmpty() } ?: scrapedDescription.takeIf { !it.isNullOrEmpty() } ?: "No description available."

        Log.d("AdRepository", "Final Ad Data for $url: Title='${finalTitle}', Image='${finalImageUrl}', Desc='${finalDescription}'")

        // Return Ad object if at least one field is not generic, or if manual data was provided
        if (finalTitle != "Product Title Not Found" || finalImageUrl != "https://via.placeholder.com/150/CCCCCC/000000?text=No+Image" || finalDescription != "No description available.") {
            Log.d("AdRepository", "Returning valid Ad object for $url.")
            return Ad(finalImageUrl, finalTitle, finalDescription, url)
        } else {
            Log.w("AdRepository", "Could not get any meaningful data for $url, displaying generic ad.")
            return Ad(finalImageUrl, finalTitle, finalDescription, url) // Return generic ad
        }
    }

    // Helper to parse Amazon's dynamic image JSON string
    private fun parseAmazonDynamicImageUrl(jsonString: String): String? {
        val regex = Regex("\"(https?://[^\"]+)\":\\[\\d+,\\d+\\]")
        return regex.find(jsonString)?.groups?.get(1)?.value
    }
}

// Data class to represent the structure of manual ad data in JSON
data class ManualAdData(
    val imageUrl: String?,
    val title: String?,
    val description: String?
)
