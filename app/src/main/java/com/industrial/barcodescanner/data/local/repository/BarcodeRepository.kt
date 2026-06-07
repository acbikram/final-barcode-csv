package com.industrial.barcodescanner.data.local.repository

import com.industrial.barcodescanner.data.local.dao.BarcodeDao
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import kotlinx.coroutines.flow.Flow

class BarcodeRepository(private val barcodeDao: BarcodeDao) {
    fun getAllScannedBarcodes(): Flow<List<BarcodeEntity>> = barcodeDao.getAllScannedBarcodes()

    suspend fun insertBarcode(barcodeEntity: BarcodeEntity) = barcodeDao.insertBarcode(barcodeEntity)

    suspend fun deleteBarcode(barcodeEntity: BarcodeEntity) = barcodeDao.deleteBarcode(barcodeEntity)

    suspend fun updateBarcode(barcodeEntity: BarcodeEntity) = barcodeDao.updateBarcode(barcodeEntity)

    suspend fun findDuplicate(barcode: String, tagType: String, unitType: String): BarcodeEntity? =
        barcodeDao.findDuplicate(barcode, tagType, unitType)

    suspend fun saveBarcode(barcode: String, tagType: String, unitType: String, copies: Int = 1) {
        val entity = BarcodeEntity(
            barcode = barcode,
            price = null,
            tagType = tagType,
            unitType = unitType,
            copies = copies
        )
        insertBarcode(entity)
    }
}
