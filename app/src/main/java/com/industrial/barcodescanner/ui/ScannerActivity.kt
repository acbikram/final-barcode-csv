package com.industrial.barcodescanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.industrial.barcodescanner.R
import com.industrial.barcodescanner.databinding.ActivityScannerBinding
import com.industrial.barcodescanner.ui.viewmodel.ScannerViewModel
import com.industrial.barcodescanner.util.BarcodeAnalyzer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var viewModel: ScannerViewModel
    private lateinit var tvLastBarcode: TextView
    private lateinit var tvTagDisplay: TextView
    private lateinit var tvUnitDisplay: TextView

    private var tagType: String = "A4"
    private var unitType: String = "PCS"
    private var preventDuplicates: Boolean = true

    private var autoDismissJob: Job? = null
    private var copiesDialog: AlertDialog? = null
    private var pendingBarcode: String? = null
    private var pendingMergeBarcode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[ScannerViewModel::class.java]

        tagType = intent.getStringExtra("TAG_TYPE") ?: "A4"
        unitType = intent.getStringExtra("UNIT_TYPE") ?: "PCS"
        preventDuplicates = intent.getBooleanExtra("PREVENT_DUPLICATES", true)

        tvLastBarcode = findViewById(R.id.tvLastBarcode)
        tvTagDisplay = findViewById(R.id.tvTagDisplay)
        tvUnitDisplay = findViewById(R.id.tvUnitDisplay)

        // Set initial values
        tvTagDisplay.text = tagType
        tvUnitDisplay.text = unitType

        // Manual entry button
        findViewById<Button>(R.id.btnManualEntry).setOnClickListener {
            showManualEntryDialog()
        }

        // Close button
        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }

        viewModel.duplicateWarning.observe(this) { barcode ->
            barcode?.let {
                pendingMergeBarcode = barcode
                showDuplicateResolutionDialog(barcode)
                viewModel.resetEvents()
            }
        }

        viewModel.scanSuccess.observe(this) { barcode ->
            barcode?.let {
                tvLastBarcode.text = it
                tvTagDisplay.text = tagType
                tvUnitDisplay.text = unitType
                Toast.makeText(this, "Saved: $it", Toast.LENGTH_SHORT).show()
                viewModel.resetEvents()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(this),
                    BarcodeAnalyzer(this) { barcode ->
                        pendingBarcode = barcode
                        if (preventDuplicates) {
                            lifecycleScope.launch {
                                viewModel.processBarcode(barcode, tagType, unitType, true, 1)
                            }
                        } else {
                            showCopiesDialog(barcode, isMerge = false)
                        }
                    }
                )
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                // Start scan line animation
                startScanLineAnimation()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun startScanLineAnimation() {
        val scanLine = findViewById<View>(R.id.scanLine)
        if (scanLine != null) {
            // Wait for layout to be measured
            scanLine.post {
                val previewHeight = binding.viewFinder.height
                if (previewHeight > 0) {
                    val animation = TranslateAnimation(
                        0f, 0f, 0f, (previewHeight - 20).toFloat()
                    ).apply {
                        duration = 2000
                        repeatMode = TranslateAnimation.REVERSE
                        repeatCount = TranslateAnimation.INFINITE
                    }
                    scanLine.startAnimation(animation)
                } else {
                    // fallback animation
                    val animation = TranslateAnimation(0f, 0f, 0f, 180f).apply {
                        duration = 2000
                        repeatMode = TranslateAnimation.REVERSE
                        repeatCount = TranslateAnimation.INFINITE
                    }
                    scanLine.startAnimation(animation)
                }
            }
        }
    }

    private fun showManualEntryDialog() {
        val input = EditText(this).apply {
            hint = "Enter barcode number"
            inputType = InputType.TYPE_CLASS_NUMBER
            setTextColor(android.graphics.Color.WHITE)
            setHintTextColor(android.graphics.Color.GRAY)
            background = ContextCompat.getDrawable(this@ScannerActivity, android.R.drawable.editbox_background)
        }
        AlertDialog.Builder(this)
            .setTitle("Manual Barcode Entry")
            .setMessage("Type or paste the barcode number")
            .setView(input)
            .setPositiveButton("Scan") { _, _ ->
                val barcode = input.text.toString().trim()
                if (barcode.isNotEmpty()) {
                    if (preventDuplicates) {
                        lifecycleScope.launch {
                            viewModel.processBarcode(barcode, tagType, unitType, true, 1)
                        }
                    } else {
                        showCopiesDialog(barcode, isMerge = false)
                    }
                } else {
                    Toast.makeText(this, "Please enter a barcode", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDuplicateResolutionDialog(barcode: String) {
        AlertDialog.Builder(this)
            .setTitle("Duplicate found")
            .setMessage("Barcode $barcode already exists with same Tag & Unit.\nAdd copies to existing entry?")
            .setPositiveButton("Yes, add copies") { _, _ ->
                showCopiesDialog(barcode, isMerge = true)
            }
            .setNegativeButton("No, cancel") { _, _ ->
                pendingBarcode = null
                pendingMergeBarcode = null
                Toast.makeText(this, "Scan discarded", Toast.LENGTH_SHORT).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun showCopiesDialog(barcode: String, isMerge: Boolean) {
        autoDismissJob?.cancel()
        copiesDialog?.dismiss()

        val buttons = listOf(1, 2, 3, 4, 5).map { copies ->
            Button(this).apply {
                text = copies.toString()
                textSize = 28f
                layoutParams = android.widget.LinearLayout.LayoutParams(
                    0,
                    android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(8, 0, 8, 0)
                }
                setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                setOnClickListener {
                    autoDismissJob?.cancel()
                    copiesDialog?.dismiss()
                    if (isMerge) {
                        lifecycleScope.launch {
                            viewModel.mergeCopies(barcode, tagType, unitType, copies)
                        }
                    } else {
                        lifecycleScope.launch {
                            viewModel.processBarcode(barcode, tagType, unitType, preventDuplicates, copies)
                        }
                    }
                    pendingBarcode = null
                    pendingMergeBarcode = null
                }
            }
        }

        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(50, 50, 50, 50)
            buttons.forEach { addView(it) }
        }

        copiesDialog = AlertDialog.Builder(this)
            .setTitle(if (isMerge) "Add how many copies?" else "Select number of copies")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        copiesDialog?.show()

        autoDismissJob = lifecycleScope.launch {
            delay(3000)
            if (copiesDialog?.isShowing == true) {
                copiesDialog?.dismiss()
                if (isMerge) {
                    viewModel.mergeCopies(barcode, tagType, unitType, 1)
                } else {
                    viewModel.processBarcode(barcode, tagType, unitType, preventDuplicates, 1)
                }
                pendingBarcode = null
                pendingMergeBarcode = null
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        autoDismissJob?.cancel()
        copiesDialog?.dismiss()
        // Stop scan line animation
        findViewById<View>(R.id.scanLine)?.clearAnimation()
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 101
    }
}
