/*
 *
 *  * Created by Ariful Jannat Arif on 11/26/20 2:26 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 11/26/20 2:26 PM
 *
 */

package com.inverseai.billing

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import com.android.billingclient.api.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException

class BillingManager(
    private val purchaseDao: PurchaseDao,
    private val clientSecret: BillingClientInfoCallbacks
) : PurchasesUpdatedListener {
    private var billingConnectionInProgress: Boolean = false
    private var needToStartConnection: Boolean = false
    private var billingClient: BillingClient? = null
    private var retryJob: Job? = null
    private var queryOnGoing = false
    private var purchaseQueryRuning = false

    private val _products = purchaseDao.getAllProducts()

    val availableProducts: LiveData<List<ProductItem>>
        get() = _products

    private val _purchases = purchaseDao.getAllPurchases()

    val userPurchases: LiveData<List<PurchasedItemModel>>
        get() = _purchases


    private fun initializeBilling(context: Context) {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        callForBillingConnection()
    }

    fun refreshAgain() {
        CoroutineScope(IO).launch {
            queryPurchases()
            queryProducts()
        }
    }

    /**
     * Billing Manager needs to be first establish connection to its server before querying any purchases
     */
    private fun callForBillingConnection() {
        if (billingConnectionInProgress) return
        billingConnectionInProgress = true
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                billingConnectionInProgress = false
                Log.d(TAG, "onBillingSetupFinished: ${billingResult.responseCode}")
                CoroutineScope(IO).launch {
                    needToStartConnection = false
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryPurchases()
                        queryProducts()

                    } else {
                        retryAfterDelay(RETRY_DELAY)
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected: ")
                needToStartConnection = true
                CoroutineScope(IO).launch {
                    retryAfterDelay(RETRY_DELAY)
                }
            }
        })
    }

    private fun queryPurchases() {
        Log.d(TAG, "queryPurchases: 94")
        if (needToStartConnection) {
            if (needToStartConnection) {
                callForBillingConnection()
                return
            }
        }
        if (purchaseQueryRuning) return
        purchaseQueryRuning = true
        Log.d(TAG, "queryPurchases: 101")
        val list = ArrayList<Purchase>()
        val iapResult: Purchase.PurchasesResult? =
            billingClient?.queryPurchases(BillingClient.SkuType.INAPP)
        if (iapResult != null && iapResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "queryPurchases: inapp size ${iapResult.purchasesList?.size}")
            iapResult.purchasesList?.forEach {
                list.add(it)
            }
            // onPurchasesUpdated(iapResult.billingResult,iapResult.purchasesList)
        }
        val subsResult: Purchase.PurchasesResult? =
            billingClient?.queryPurchases(BillingClient.SkuType.SUBS)
        if (subsResult != null && subsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "queryPurchases: subs size ${subsResult.purchasesList?.size}")
            subsResult.purchasesList?.forEach {
                list.add(it)
            }
            // onPurchasesUpdated(subsResult.billingResult,subsResult.purchasesList)
            onPurchasesUpdated(subsResult.billingResult, list)
        }
        purchaseQueryRuning = false

    }

    /**
     *  Be sure you call this method if and only if you are sure billingclient onSetupFinished called before
     */
    private suspend fun queryProducts() {
        Log.d(TAG, "QueryProducts: ")
        if (queryOnGoing) return
        if (needToStartConnection) {
            callForBillingConnection()
            retryAfterDelay(RETRY_DELAY)
            return
        }

        queryOnGoing = true

        val items = arrayListOf<ProductItem>()

        val inAppQuery: Runnable = kotlinx.coroutines.Runnable {
            val subsSkuList = getSkuList(ProductType.INAPP)
            val params = SkuDetailsParams.newBuilder()
            params.setSkusList(subsSkuList).setType(BillingClient.SkuType.INAPP)
            billingClient!!.querySkuDetailsAsync(params.build()) { billingResult, skudetails ->
                Log.d(TAG, "product fetched inapp ${skudetails?.size}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    for (item in skudetails ?: arrayListOf()) {
                        try {
                            val skuDetails = Gson().toJson(item)
                            val purchaseItem = ProductItem(
                                item.title,
                                item.type,
                                item.sku,
                                item.freeTrialPeriod,
                                item.subscriptionPeriod,
                                item.price,
                                item.originalPrice,
                                item.priceCurrencyCode,
                                item.introductoryPrice,
                                item.introductoryPricePeriod,
                                item.introductoryPriceCycles,
                                item.originalPriceAmountMicros,
                                item.introductoryPriceAmountMicros,
                                originalJson = item.originalJson,
                                iconUrl = item.iconUrl,
                                description = item.description,
                                skuDetails = skuDetails
                            )
                            items.add(purchaseItem)
                        } catch (ex: Exception) {
                        }
                    }
                    CoroutineScope(IO).launch {
                        purchaseDao.onPurchaseListFetched(items)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                    needToStartConnection = true
                    Log.d(
                        TAG,
                        "product fetch failed :service disconnected: retrying ${billingResult.responseCode}"
                    )
                    callForBillingConnection()
                } else {
                    Log.d(TAG, "product fetch failed :responsecode ${billingResult.responseCode}")
                }
            }
        }

        val subsSku = getSkuList(ProductType.SUBSCRIPTION)
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(subsSku).setType(BillingClient.SkuType.SUBS)
        billingClient!!.querySkuDetailsAsync(params.build()) { billingResult, skudetails ->
            Log.d(TAG, "product fetched subs ${skudetails?.size}")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (item in skudetails ?: arrayListOf()) {
                    try {
                        val skuDetails = Gson().toJson(item)
                        val purchaseItem = ProductItem(
                            item.title,
                            item.type,
                            item.sku,
                            item.freeTrialPeriod,
                            item.subscriptionPeriod,
                            item.price,
                            item.originalPrice,
                            item.priceCurrencyCode,
                            item.introductoryPrice,
                            item.introductoryPricePeriod,
                            item.introductoryPriceCycles,
                            item.originalPriceAmountMicros,
                            item.introductoryPriceAmountMicros,
                            originalJson = item.originalJson,
                            iconUrl = item.iconUrl,
                            description = item.description,
                            skuDetails = skuDetails
                        )
                        items.add(purchaseItem)
                    } catch (ex: Exception) {
                    }
                }
                inAppQuery.run()
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED) {
                Log.d(TAG, "queryPurchases: billing disconnected ")
            } else {
                Log.d(TAG, "product fetch failed :responsecode ${billingResult.responseCode}")
            }
        }

    }

    fun initiatePurchase(activity: Activity, skuDetails: SkuDetails): Boolean {
        Log.d(TAG, "initiatePurchase: ")
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        if (billingClient == null) {
            CoroutineScope(IO).launch {
                retryAfterDelay(RETRY_DELAY)
            }
            return false
        }
        val responseCode = billingClient!!.launchBillingFlow(activity, flowParams).responseCode
        return true
    }

    /**
     * it's obvious sometime normal flow will encounter problems so here is the mechanism
     * here we can retry this process if anything not working okay
     */
    private suspend fun retryAfterDelay(delay: Long) {
        Log.d(TAG, "retryAfterDelay: starts ${System.currentTimeMillis()}")
        delay(delay)
        queryProducts()
        Log.d(TAG, "retryAfterDelay: end ${System.currentTimeMillis()}")
    }

    /**
     * This is the place where callback reaches when the users purchase list is finished querying or any update occurred to the list
     */
    override fun onPurchasesUpdated(result: BillingResult, purchaseList: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated:responseCode ${result.responseCode}")
        Log.d(TAG, "onPurchasesUpdated: ${purchaseList.toString()}")
        val purchases = mutableListOf<PurchasedItemModel>()
        purchaseList?.forEach {
            handlePurchase(it)
            var originalJson: String? = null
            try {
                originalJson = Gson().toJson(it)
            } catch (ex: Exception) {

            }
            val purchaseItem = PurchasedItemModel(
                it.orderId,
                it.packageName,
                it.sku,
                it.purchaseTime,
                it.purchaseState,
                it.purchaseToken,
                it.isAutoRenewing,
                it.isAcknowledged,
                originalJson
            )
            purchases.add(purchaseItem)
        }
        CoroutineScope(IO).launch {
            purchaseDao.updateUserSubscriptions(purchases.toList())
        }
    }

    fun consumePurchase(purchase: Purchase) {
        val consumeParams =
            ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build()
        Log.d(TAG, "consumePurchase: ${purchase.orderId}")

        billingClient?.consumeAsync(consumeParams) { billingResult, outToken ->
            Log.d(
                TAG,
                "consumePurchase: responseCode ${billingResult.responseCode} messge ${billingResult.debugMessage}"
            )
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            }
        }
    }


    private fun handlePurchase(purchase: Purchase) {
        if (!verifyValidSignature(purchase.originalJson, purchase.signature)) {
            Log.i(
                TAG,
                "Got a purchase: $purchase; but signature is bad. Skipping..."
            )
            return
        }
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                CoroutineScope(IO).launch {
                    billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build())
                }
            }
        }
    }

    private fun verifyValidSignature(
        signedData: String,
        signature: String
    ): Boolean {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        return try {
            val verificationStatus: Boolean =
                com.inverseai.billing.Security.verifyPurchase(
                    clientSecret.getBase64Key(),
                    signedData,
                    signature
                )
            try {
                if (!verificationStatus) {
                    //showExceptionToast(activity)
                    val params = Bundle()
                    // params.putBoolean(BillingUtils.PURCHASE_VALIDATION_STATUS, false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            verificationStatus
        } catch (e: IOException) {
            Log.e(TAG, "Got an exception trying to validate a purchase: $e")
            try {
                // showExceptionToast(activity)
                //  val params = Bundle()
                // params.putString(BillingUtils.PURCHASE_EXCEPTION_MSG, e.message)
            } catch (error: Exception) {
                error.printStackTrace()
            }
            false
        }
    }


    /**
     * Sku-details contains the information that play store need when we make a query to the play server
     */
    private fun getSkuList(skuType: ProductType): List<String> {
        return when (skuType) {
            ProductType.INAPP -> clientSecret.getInAppSkuIds()
            ProductType.SUBSCRIPTION -> clientSecret.getSubscriptionSkuIds()
        }
    }

    /**
     * base 64 key is provided by the play console and used to verify the products
     */
    private fun getBase64Key(): String {
        return clientSecret.getBase64Key()
    }

    /**
     * constants value
     * made this class singleton as we may need it from multiple screens
     * it's not wise to initialize a frequently used utility class every time we use it in a new place
     */
    companion object {
        private const val TAG = "BillingManager"
        private const val RETRY_DELAY: Long = 3000L
        private var initialized = false
        private var billingManager: BillingManager? = null

        /**
         * This is the function to creating the singleton class from your apps Application class on-create
         * Please don't use it frequently outside of application class
         */

        fun initialize(context: Context, clientSecret: BillingClientInfoCallbacks) {
            if (initialized) return
            billingManager =
                BillingManager(PurchaseDb.getDatabse(context).getPurchaseDao(), clientSecret)
            billingManager!!.initializeBilling(context)

        }

        fun getInstance(): BillingManager {
            if (billingManager == null) {
                throw IllegalStateException("Billing manager is not initialized. Initialize it from application class onCreate ");
            }
            return billingManager!!
        }
    }
}