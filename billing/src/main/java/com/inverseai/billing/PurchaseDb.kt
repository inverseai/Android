/*
 *
 *  * Created by Ariful Jannat Arif on 11/26/20 3:01 PM
 *  * cse.ariful@gmail.com
 *  * No man is perfect so why codes? if anything went wrong please debug
 *  * Last modified 11/26/20 3:01 PM
 *
 */

package com.inverseai.billing

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ProductItem::class,PurchasedItemModel::class],version = 1,exportSchema = false)
//@TypeConverters(CompressStateConverter::class,FileFormatConverter::class)
abstract  class PurchaseDb: RoomDatabase(){

    abstract fun getPurchaseDao():PurchaseDao
    companion object{
        val DB_NAME = "purchases.db"
        @Volatile
        private var INSTANCE:PurchaseDb? = null

        fun getDatabse(context: Context):PurchaseDb{
            val tempInstance = INSTANCE
            if(tempInstance!=null){
                return tempInstance
            }
            synchronized(this){
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        PurchaseDb::class.java,
                        DB_NAME
                    ).fallbackToDestructiveMigration().build()
                INSTANCE=instance
                return instance
            }
        }
    }
}