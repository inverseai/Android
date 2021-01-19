/*
 *
 *  * Created by Ariful Jannat Arif on 11/26/20 3:05 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 11/26/20 3:05 PM
 *
 */

package com.inverseai.billing

import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.billingclient.api.SkuDetails

@Entity(tableName = "product_table")
data class ProductItem(
    val title: String,
    val type: String,
    val sku: String,

    val freeTrialPeriod: String,
    val subscriptionPeriod:String,

    val price: String,
    val originalPRice: String,
    val priceCurrencyCode:String,

    val introductoryPrice: String,
    val introductoryPricePeriod: String,
    val introductoryPriceCycle: Int,


    val originalPriceAmountMicros: Long,
    val introductoryPriceAmountMacros: Long,

    val originalJson: String,
    val iconUrl: String,
    val description:String,
    var selected:Boolean = false,
    val skuDetails: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

}