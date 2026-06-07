package com.industrial.barcodescanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.industrial.barcodescanner.data.local.dao.BarcodeDao
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity

@Database(entities = [BarcodeEntity::class], version = 2, exportSchema = false)
abstract class BarcodeDatabase : RoomDatabase() {
    abstract fun barcodeDao(): BarcodeDao

    companion object {
        @Volatile
        private var INSTANCE: BarcodeDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE scanned_barcodes ADD COLUMN copies INTEGER NOT NULL DEFAULT 1")
            }
        }

        fun getDatabase(context: Context): BarcodeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BarcodeDatabase::class.java,
                    "barcode_database"
                ).addMigrations(MIGRATION_1_2)
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
