package com.industrial.barcodescanner.util

import android.content.Context
import android.net.Uri
import com.industrial.barcodescanner.data.local.entity.BarcodeEntity
import java.io.BufferedWriter
import java.io.OutputStreamWriter

object CsvExportUtility {

    fun writeCollectionToUri(context: Context, targetUri: Uri, records: List<BarcodeEntity>) {
        try {
            context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                outputStream.write(0xEF)
                outputStream.write(0xBB)
                outputStream.write(0xBF)

                BufferedWriter(OutputStreamWriter(outputStream, "UTF-8")).use { writer ->
                    writer.write("Barcode,Price,TagType,UnitType,Copies")
                    writer.newLine()

                    records.forEach { item ->
                        val barcode = cleanValue(item.barcode)
                        val price = cleanValue(item.price ?: "")
                        val tag = cleanValue(item.tagType)
                        val unit = cleanValue(item.unitType)
                        val copies = item.copies.toString()

                        writer.write("$barcode,$price,$tag,$unit,$copies")
                        writer.newLine()
                    }
                    writer.flush()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cleanValue(value: String): String {
        var result = value.replace("\"", "\"\"")
        if (result.contains(",") || result.contains("\n") || result.contains("\"")) {
            result = "\"$result\""
        }
        return result
    }
}
