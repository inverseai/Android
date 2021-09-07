package com.inverseai.adhelper.banner

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.inverseai.adhelper.BannerAd
import com.inverseai.adhelper.BuildConfig
import com.inverseai.adhelper.R
import com.inverseai.adhelper.util.AdCallback
import com.inverseai.adhelper.util.AdType
import com.google.android.gms.ads.MobileAds

import com.google.android.gms.ads.RequestConfiguration




class AdmobBannerAd(private val context: Context, private val size: BannerAd.AdSize) : BannerAd {

    private var callback: AdCallback? = null
    override fun setListener(listener: AdCallback) {
        this.callback = listener
    }

    override fun getAdSize(): Int {
        return convertToAdmobAdSize().getHeightInPixels(context)
    }

    override fun loadAd(context: Context, adContainer: ViewGroup) {
        Log.d(TAG, "loadAd: ")
        val adId =if(BuildConfig.DEBUG)"" else context.getString(
            context.resources.getIdentifier(
                "admob_banner",
                "string",
                context.applicationContext.packageName
            )
        )

        val adView = AdView(context)

        adView.adSize = convertToAdmobAdSize()

        adView.adUnitId =
            if (BuildConfig.DEBUG) context.resources.getString(R.string.admob_banner_id_test) else adId
        adContainer.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded: ")
                callback?.onAdLoaded(AdType.TYPE_BANNER)
            }

            override fun onAdFailedToLoad(adError: LoadAdError?) {
                super.onAdFailedToLoad(adError)
                Log.d(TAG, "onAdFailedToLoad: ${adError?.message}")
                callback?.onFailedToLoad(AdType.TYPE_BANNER)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG, "onAdClosed: ")
            }
        }
    }


    override fun onDestroy(context: Context) {
    }

    private fun convertToAdmobAdSize(): AdSize {
        when (size) {
            BannerAd.AdSize.LARGE -> return AdSize.LARGE_BANNER
            BannerAd.AdSize.MEDIUM -> return AdSize.MEDIUM_RECTANGLE
            else -> return AdSize.SMART_BANNER
        }
    }

    companion object {
        private const val TAG = "AdmobBannerAd"
    }
}