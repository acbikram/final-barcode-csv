package com.industrial.barcodescanner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.industrial.barcodescanner.BarcodeApplication
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BarcodeApplication).repository

    private val _scanSuccess = MutableLiveData<String?>()
    val scanSuccess: LiveData<String?> = _scanSuccess

    private val _duplicateWarning = MutableLiveData<String?>()
    val duplicateWarning: LiveData<String?> = _duplicateWarning

    suspend fun checkDuplicate(barcode: String, tagType: String, unitType: String): BarcodeEntity? {
        return repository.findDuplicate(barcode, tagType, unitType)
    }

    suspend fun saveBarcode(barcode: String, tagType: String, unitType: String, copies: Int) {
        repository.saveBarcode(barcode, tagType, unitType, copies)
        _scanSuccess.postValue(barcode)
    }

    suspend fun mergeCopies(barcode: String, tagType: String, unitType: String, additionalCopies: Int) {
        val existing = repository.findDuplicate(barcode, tagType, unitType)
        if (existing != null) {
            val newCopies = existing.copies + additionalCopies
            val updated = existing.copy(copies = newCopies)
            repository.updateBarcode(updated)
            _scanSuccess.postValue(barcode)
        }
    }

    fun resetEvents() {
        _scanSuccess.value = null
        _duplicateWarning.value = null
    }
}
