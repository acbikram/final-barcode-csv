package com.industrial.barcodescanner.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.industrial.barcodescanner.BarcodeApplication
import kotlinx.coroutines.launch

class ScannerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as BarcodeApplication).repository

    private val _scanSuccess = MutableLiveData<String?>()
    val scanSuccess: LiveData<String?> = _scanSuccess

    private val _duplicateWarning = MutableLiveData<String?>()
    val duplicateWarning: LiveData<String?> = _duplicateWarning

    fun processBarcode(barcode: String, tagType: String, unitType: String, preventDuplicates: Boolean, copies: Int = 1) {
        viewModelScope.launch {
            if (preventDuplicates) {
                val existing = repository.findDuplicate(barcode, tagType, unitType)
                if (existing != null) {
                    _duplicateWarning.value = barcode
                    return@launch
                }
            }
            repository.saveBarcode(barcode, tagType, unitType, copies)
            _scanSuccess.value = barcode
        }
    }

    suspend fun mergeCopies(barcode: String, tagType: String, unitType: String, additionalCopies: Int) {
        val existing = repository.findDuplicate(barcode, tagType, unitType)
        if (existing != null) {
            val newCopies = existing.copies + additionalCopies
            val updated = existing.copy(copies = newCopies)
            repository.updateBarcode(updated)
            _scanSuccess.value = barcode
        }
    }

    fun resetEvents() {
        _scanSuccess.value = null
        _duplicateWarning.value = null
    }
}
