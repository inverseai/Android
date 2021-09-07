package com.inverseai.adhelper.util

interface AdCallback {
    fun onAdLoaded(type: AdType)
    fun onFailedToLoad(message: AdType)
}

enum class AdType {
    TYPE_BANNER,
    TYPE_INTERSTITIAL,
    TYPE_NATIVE,
    TYPE_REWARDED
}