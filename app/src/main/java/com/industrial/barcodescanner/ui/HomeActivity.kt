package com.industrial.barcodescanner.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.industrial.barcodescanner.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("ScannerPrefs", Context.MODE_PRIVATE)
        loadSavedConfigurations()

        binding.btnStartScan.setOnClickListener {
            saveConfigurationsAndStart()
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun loadSavedConfigurations() {
        val tagType = sharedPreferences.getString("TAG_TYPE", "A4")
        val unitType = sharedPreferences.getString("UNIT_TYPE", "PCS")
        val preventDuplicates = sharedPreferences.getBoolean("PREVENT_DUPLICATES", true)

        when (tagType) {
            "A4" -> binding.rbTagA4.isChecked = true
            "4PCS" -> binding.rbTag4pcs.isChecked = true
            "VEG" -> binding.rbTagVeg.isChecked = true
            "4PCS_DATE" -> binding.rbTag4pcsDate.isChecked = true
            "4PCS_SAME" -> binding.rbTag4pcsSame.isChecked = true
        }

        when (unitType) {
            "PCS" -> binding.rbUnitPcs.isChecked = true
            "PKT" -> binding.rbUnitPkt.isChecked = true
            "CTN" -> binding.rbUnitCtn.isChecked = true
            "KGS" -> binding.rbUnitKgs.isChecked = true
        }

        binding.switchDuplicateChecking.isChecked = preventDuplicates
    }

    private fun saveConfigurationsAndStart() {
        val selectedTag = when {
            binding.rbTagA4.isChecked -> "A4"
            binding.rbTag4pcs.isChecked -> "4PCS"
            binding.rbTagVeg.isChecked -> "VEG"
            binding.rbTag4pcsDate.isChecked -> "4PCS_DATE"
            else -> "4PCS_SAME"
        }
        val selectedUnit = when {
            binding.rbUnitPcs.isChecked -> "PCS"
            binding.rbUnitPkt.isChecked -> "PKT"
            binding.rbUnitCtn.isChecked -> "CTN"
            else -> "KGS"
        }
        val preventDuplicates = binding.switchDuplicateChecking.isChecked

        sharedPreferences.edit().apply {
            putString("TAG_TYPE", selectedTag)
            putString("UNIT_TYPE", selectedUnit)
            putBoolean("PREVENT_DUPLICATES", preventDuplicates)
            apply()
        }

        val intent = Intent(this, ScannerActivity::class.java).apply {
            putExtra("TAG_TYPE", selectedTag)
            putExtra("UNIT_TYPE", selectedUnit)
            putExtra("PREVENT_DUPLICATES", preventDuplicates)
        }
        startActivity(intent)
    }
}
