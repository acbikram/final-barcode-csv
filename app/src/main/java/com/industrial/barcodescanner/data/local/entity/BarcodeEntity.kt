package com.industrial.barcodescanner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_barcodes")
data class BarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val price: String? = null,
    val tagType: String = "Standard",
    val unitType: String = "PCS",
    val timestamp: Long = System.currentTimeMillis(),
    val copies: Int = 1
)
