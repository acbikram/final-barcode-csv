package com.industrial.barcodescanner.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.industrial.barcodescanner.R
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import com.industrial.barcodescanner.ui.adapter.HistoryAdapter
import com.industrial.barcodescanner.ui.viewmodel.HistoryViewModel
import com.industrial.barcodescanner.util.CsvExportUtility
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                exportCsvToUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]

        val recyclerHistory = findViewById<RecyclerView>(R.id.recyclerHistory)
        val btnExportCsv = findViewById<Button>(R.id.btnExportCsv)

        adapter = HistoryAdapter(
            onPriceRowInteraction = { entity -> showEditPriceDialog(entity) },
            onDeleteInteraction = { entity -> viewModel.deleteItem(entity) }
        )

        recyclerHistory.layoutManager = LinearLayoutManager(this)
        recyclerHistory.adapter = adapter

        viewModel.scanHistory.observe(this) { list ->
            adapter.submitList(list)
        }

        btnExportCsv.setOnClickListener {
            openFilePicker()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "barcodes_${System.currentTimeMillis()}.csv")
        }
        filePickerLauncher.launch(intent)
    }

    private fun exportCsvToUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val items = viewModel.getBarcodesList()
                if (items.isEmpty()) {
                    Toast.makeText(this@HistoryActivity, "No data to export", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                CsvExportUtility.writeCollectionToUri(this@HistoryActivity, uri, items)
                Toast.makeText(this@HistoryActivity, "CSV saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@HistoryActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditPriceDialog(entity: BarcodeEntity) {
        val input = EditText(this).apply {
            hint = "Enter price (e.g., 12.99)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(entity.price ?: "")
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
            background = androidx.core.content.ContextCompat.getDrawable(this@HistoryActivity, android.R.drawable.editbox_background)
        }
        AlertDialog.Builder(this)
            .setTitle("Edit Price")
            .setMessage("Set price for barcode: ${entity.barcode}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPrice = input.text.toString().trim()
                if (newPrice.isNotEmpty()) {
                    viewModel.updatePrice(entity, newPrice)
                    Toast.makeText(this, "Price updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Price cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
