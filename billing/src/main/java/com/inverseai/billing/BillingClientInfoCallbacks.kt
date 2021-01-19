/*
 *
 *  * Created by Ariful Jannat Arif on 11/26/20 3:45 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 11/26/20 3:45 PM
 *
 */

package com.inverseai.billing

interface BillingClientInfoCallbacks {
    fun getSubscriptionSkuIds():List<String>
    fun getInAppSkuIds():List<String>
    fun getBase64Key():String
}