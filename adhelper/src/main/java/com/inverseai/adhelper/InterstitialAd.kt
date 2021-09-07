package com.inverseai.adhelper

import android.content.Context
import com.inverseai.adhelper.util.AdCallback

interface InterstitialAd {
    fun setListener(listener: AdCallback)
    fun loadAd(context: Context)

    fun show(context: Context)
    fun isLoaded(): Boolean

    fun onDestroy(context: Context)
}