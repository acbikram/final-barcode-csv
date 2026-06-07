package com.industrial.barcodescanner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.industrial.barcodescanner.BarcodeApplication
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as BarcodeApplication).repository
    val scanHistory: LiveData<List<BarcodeEntity>> = repository.getAllScannedBarcodes().asLiveData()

    fun deleteItem(item: BarcodeEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteBarcode(item)
        }
    }

    fun updatePrice(item: BarcodeEntity, newPrice: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = item.copy(price = newPrice)
            repository.updateBarcode(updated)
        }
    }

    suspend fun getBarcodesList(): List<BarcodeEntity> {
        return repository.getAllScannedBarcodes().asLiveData().value ?: emptyList()
    }
}
