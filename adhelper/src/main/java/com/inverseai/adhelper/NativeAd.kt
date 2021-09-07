package com.inverseai.adhelper

import android.content.Context
import android.view.ViewGroup
import com.inverseai.adhelper.util.AdCallback

interface NativeAd {
    fun setListener(adCallback: AdCallback)
    fun loadAndShow(context: Context, container: ViewGroup)

    fun onDestroy(context: Context)
}