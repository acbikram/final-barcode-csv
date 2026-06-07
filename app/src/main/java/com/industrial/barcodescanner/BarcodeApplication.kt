package com.industrial.barcodescanner

import android.app.Application
import com.industrial.barcodescanner.data.local.BarcodeDatabase
import com.industrial.barcodescanner.data.local.repository.BarcodeRepository

class BarcodeApplication : Application() {
    val database: BarcodeDatabase by lazy { BarcodeDatabase.getDatabase(this) }
    val repository: BarcodeRepository by lazy { BarcodeRepository(database.barcodeDao()) }
}
