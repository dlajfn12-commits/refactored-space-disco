package com.example.qrscanner.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.qrscanner.data.ScanRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CSVExporter {

    fun exportToCSV(context: Context, records: List<ScanRecord>): Uri? {
        return try {
            val fileName = "QR_Scans_${getCurrentDate()}.csv"
            val file = File(context.cacheDir, fileName)

            file.bufferedWriter().use { writer ->
                // Header
                writer.write("번호,Cell ID,스캔 시간\n")

                // Data
                records.forEachIndexed { index, record ->
                    writer.write("${index + 1},${record.cellId},${record.getFormattedFullTime()}\n")
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareCSV(context: Context, uri: Uri) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "text/csv"
        }
        context.startActivity(Intent.createChooser(shareIntent, "CSV 공유"))
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault())
        return sdf.format(Date())
    }
}
