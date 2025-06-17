package com.promptmaster.ui.ads

import android.util.Log // Import Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.promptmaster.data.ads.Ad
import com.promptmaster.data.ads.AdRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(
    private val adRepository: AdRepository
) : ViewModel() {

    private val _currentAd = MutableStateFlow<Ad?>(null)
    val currentAd: StateFlow<Ad?> = _currentAd

    private var allAds: List<Ad> = emptyList()
    private var currentAdIndex = 0

    init {
        Log.d("AdViewModel", "AdViewModel initialized. Calling loadAds().")
        loadAds()
    }

    private fun loadAds() {
        viewModelScope.launch {
            Log.d("AdViewModel", "loadAds() called. Fetching ads from repository.")
            allAds = adRepository.getAds()
            Log.d("AdViewModel", "Ads fetched from repository. Total ads: ${allAds.size}")
            if (allAds.isNotEmpty()) {
                _currentAd.value = allAds[currentAdIndex]
                Log.d("AdViewModel", "Initial ad set: Title='${_currentAd.value?.title}'")
            } else {
                Log.w("AdViewModel", "No ads fetched from repository. _currentAd remains null.")
            }
        }
    }

    fun showNextAd() {
        Log.d("AdViewModel", "showNextAd() called. Current index: $currentAdIndex, Total ads: ${allAds.size}")
        if (allAds.isNotEmpty()) {
            currentAdIndex = (currentAdIndex + 1) % allAds.size
            _currentAd.value = allAds[currentAdIndex]
            Log.d("AdViewModel", "Next ad set: Title='${_currentAd.value?.title}', New index: $currentAdIndex")
        } else {
            Log.w("AdViewModel", "Cannot show next ad, allAds list is empty.")
        }
    }
}
