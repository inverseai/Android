/*
 *
 *  * Created by Ariful Jannat Arif on 12/3/20 1:01 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 12/3/20 1:01 PM
 *
 */

package com.inverseai.billing

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.billingclient.api.Purchase
import java.util.stream.LongStream

@Entity(tableName = "purchase_table")
data class PurchasedItemModel(
    val orderId: String,
    val packageName: String,
    val productId: String,
    val purchaseTime: Long,
    val purchaseState: Int,
    @PrimaryKey
    val purchaseToken: String,
    val autoRenewing: Boolean,
    val acknowledged: Boolean,
    val originalJson:String?
){
    /* @PrimaryKey(autoGenerate = true)
     var id: Int? = null*/
}