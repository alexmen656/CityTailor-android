package com.app.citytailor.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import com.app.citytailor.model.TravelPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PDFExporter(private val context: Context) {

    fun exportTravelPlanToPDF(travelPlan: TravelPlan, onSuccess: (Uri) -> Unit, onError: (String) -> Unit) {
        val pdfGenerator = PDFGenerator(context)
        
        // Launch in a coroutine because PDF generation can be time-consuming
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pdfUri = pdfGenerator.generatePDF(travelPlan)
                
                withContext(Dispatchers.Main) {
                    onSuccess(pdfUri)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to generate PDF: ${e.message}")
                }
            }
        }
    }
    
    fun sharePDF(pdfUri: Uri, travelPlanName: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_SUBJECT, "Travel Plan: $travelPlanName")
            putExtra(Intent.EXTRA_TEXT, "Here's my travel plan for $travelPlanName created with City Tailor app.")
            type = "application/pdf"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooserIntent = Intent.createChooser(shareIntent, "Share Travel Plan PDF")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
    
    fun viewPDF(pdfUri: Uri) {
        val viewIntent = Intent().apply {
            action = Intent.ACTION_VIEW
            setDataAndType(pdfUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(viewIntent)
        } catch (e: Exception) {
            // If no PDF viewer app is installed, try to open it in a browser
            val webIntent = Intent(Intent.ACTION_VIEW, pdfUri)
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(webIntent)
        }
    }
}
