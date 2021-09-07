package com.example.modules

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.size
import com.inverseai.adhelper.AdAgent
import com.inverseai.adhelper.BannerAd
import com.inverseai.adhelper.InterstitialAd
import com.inverseai.adhelper.NativeAd
import com.inverseai.adhelper.util.AdCallback
import com.inverseai.adhelper.util.AdType

class MainActivity : AppCompatActivity(), AdCallback {
    lateinit var bannerHolder: FrameLayout
    lateinit var nativeHolder: FrameLayout
    private val adAgent: AdAgent by lazy {
        AdAgent(this, AdAgent.ADMOB)
    }
    lateinit var bannerad: BannerAd
    lateinit var nativeAd: NativeAd
    lateinit var interstitialAd: InterstitialAd
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bannerHolder = findViewById(R.id.bannerHolder)
        nativeHolder = findViewById(R.id.nativeAdHolder)
        bannerad = adAgent.getBannerAd(this, BannerAd.AdSize.SMART)
        nativeAd = adAgent.getNativeAd(this)
        interstitialAd = adAgent.interstitialAd
        interstitialAd.setListener(this)
        findViewById<View>(R.id.showInterstitial).setOnClickListener {
            interstitialAd.show(this)
        }
        bannerad.loadAd(this,bannerHolder)
        bannerHolder.layoutParams.height = bannerad.getAdSize()
        nativeAd.loadAndShow(this,nativeHolder)

    }

    override fun onAdLoaded(type: AdType) {
        Toast.makeText(this, "Ad Loaded", Toast.LENGTH_SHORT).show()
    }

    override fun onFailedToLoad(message: AdType) {
        Toast.makeText(this, "Ad Load Failed", Toast.LENGTH_SHORT).show()
     }
}