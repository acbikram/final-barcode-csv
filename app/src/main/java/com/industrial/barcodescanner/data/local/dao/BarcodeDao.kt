package com.industrial.barcodescanner.data.local.dao

import androidx.room.*
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarcode(barcodeEntity: BarcodeEntity): Long

    @Update
    suspend fun updateBarcode(barcodeEntity: BarcodeEntity)

    @Delete
    suspend fun deleteBarcode(barcodeEntity: BarcodeEntity)

    @Query("SELECT * FROM scanned_barcodes ORDER BY timestamp DESC")
    fun getAllScannedBarcodes(): Flow<List<BarcodeEntity>>

    @Query("SELECT * FROM scanned_barcodes WHERE barcode = :barcode AND tagType = :tagType AND unitType = :unitType LIMIT 1")
    suspend fun findDuplicate(barcode: String, tagType: String, unitType: String): BarcodeEntity?

    @Query("DELETE FROM scanned_barcodes")
    suspend fun clearAll()
}
