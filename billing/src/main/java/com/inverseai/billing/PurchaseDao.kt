/*
 *
 *  * Created by Ariful Jannat Arif on 11/26/20 3:03 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 11/26/20 3:03 PM
 *
 */

package com.inverseai.billing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.*
import com.android.billingclient.api.Purchase

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addPurchaseItem(productItem: ProductItem)

    suspend fun onPurchaseListFetched(list: List<ProductItem>){
        clearItems()
        addAllProduct(list)
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllProduct(items:List<ProductItem>)

    @Query("DELETE FROM product_table WHERE 1")
    fun clearItems()

    @Delete
    fun deletePurchaseItem(productItem: ProductItem)

    @Query("SELECT * FROM product_table ORDER BY id ASC")
    fun getAllProducts(): LiveData<List<ProductItem>>

    suspend fun updateUserSubscriptions(purchaseList: List<PurchasedItemModel>){
        clearPurchaseItems()
        addAllPurchases(purchaseList)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllPurchases(items:List<PurchasedItemModel>)

    @Query("DELETE FROM purchase_table WHERE 1")
    fun clearPurchaseItems()

    @Query("SELECT * FROM purchase_table  ORDER BY purchaseTime ASC")
    fun getAllPurchases() :  LiveData< List<PurchasedItemModel> >
}
