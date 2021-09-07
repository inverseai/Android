package com.inverseai.adhelper

import android.content.Context
import android.view.ViewGroup
import com.inverseai.adhelper.util.AdCallback

interface BannerAd {

    enum class AdSize {
        SMART, LARGE, MEDIUM
    }

    fun getAdSize(): Int

    fun loadAd(context: Context, adContainer: ViewGroup)
    fun setListener(listener:AdCallback)

    fun onDestroy(context: Context)
}