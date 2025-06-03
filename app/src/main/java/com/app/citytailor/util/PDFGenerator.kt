package com.app.citytailor.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.app.citytailor.R
import com.app.citytailor.model.Activity
import com.app.citytailor.model.DailyPlan
import com.app.citytailor.model.TravelPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PDFGenerator(private val context: Context) {
    
    private val pageWidth = 595
    private val pageHeight = 842
    private val pageMargin = 50
    
    private val titleTextSize = 24f
    private val subtitleTextSize = 18f
    private val headerTextSize = 16f
    private val bodyTextSize = 12f
    private val smallTextSize = 10f
    
    private val titlePaint = Paint().apply {
        textSize = titleTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }
    
    private val subtitlePaint = Paint().apply {
        textSize = subtitleTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }
    
    private val headerPaint = Paint().apply {
        textSize = headerTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }
    
    private val bodyPaint = Paint().apply {
        textSize = bodyTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = android.graphics.Color.BLACK
    }
    
    private val bodyBoldPaint = Paint().apply {
        textSize = bodyTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.BLACK
    }
    
    private val smallPaint = Paint().apply {
        textSize = smallTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        color = android.graphics.Color.DKGRAY
    }
    
    private val linePaint = Paint().apply {
        color = android.graphics.Color.LTGRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    suspend fun generatePDF(travelPlan: TravelPlan): Uri = withContext(Dispatchers.IO) {
        val document = PdfDocument()
        
        // Create main info page
        createInfoPage(document, travelPlan)
        
        // Create daily plan pages
        travelPlan.dailyPlans?.forEachIndexed { index, dailyPlan ->
            createDailyPlanPage(document, dailyPlan, index + 2)
        }
        
        // Create recommendations page if available
        travelPlan.recommendations?.let {
            createRecommendationsPage(document, travelPlan, (travelPlan.dailyPlans?.size ?: 0) + 2)
        }
        
        // Save the document
        val fileName = "CityTailor_${travelPlan.location.replace(" ", "_")}.pdf"
        val file = File(context.filesDir, fileName)
        document.writeTo(FileOutputStream(file))
        document.close()
        
        // Return a content URI using FileProvider
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    private fun createInfoPage(document: PdfDocument, travelPlan: TravelPlan) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPosition = pageMargin + 50f
        
        // Title
        canvas.drawText("Travel Plan", pageMargin.toFloat(), yPosition, titlePaint)
        yPosition += 40f
        
        // Location and Date
        canvas.drawText(travelPlan.location, pageMargin.toFloat(), yPosition, subtitlePaint)
        yPosition += 30f
        
        val startDateFormatted = formatDateString(travelPlan.startDate)
        val endDateFormatted = formatDateString(travelPlan.endDate)
        canvas.drawText("$startDateFormatted - $endDateFormatted", pageMargin.toFloat(), yPosition, headerPaint)
        yPosition += 40f
        
        // Draw line
        canvas.drawLine(
            pageMargin.toFloat(),
            yPosition,
            (pageWidth - pageMargin).toFloat(),
            yPosition,
            linePaint
        )
        yPosition += 30f
        
        // Trip Info
        canvas.drawText("Trip Details", pageMargin.toFloat(), yPosition, headerPaint)
        yPosition += 25f
        
        canvas.drawText("Travel Type: ${travelPlan.travelType.name}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        canvas.drawText("Transportation: ${travelPlan.transportationType.name}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        canvas.drawText("Travel Mode: ${travelPlan.travelMode.name}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        canvas.drawText("Budget Level: ${travelPlan.budgetLevel.name}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 40f
        
        // Overview
        canvas.drawText("Trip Overview", pageMargin.toFloat(), yPosition, headerPaint)
        yPosition += 25f
        
        // Number of days
        val days = travelPlan.dailyPlans?.size ?: 0
        canvas.drawText("Duration: $days days", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        // Number of activities
        val totalActivities = travelPlan.dailyPlans?.sumOf { it.activities.size } ?: 0
        canvas.drawText("Total Activities: $totalActivities", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 40f
        
        // Additional info if available
        travelPlan.info?.let {
            canvas.drawText("Additional Information", pageMargin.toFloat(), yPosition, headerPaint)
            yPosition += 25f
            
            // Split long text into multiple lines
            val maxWidth = pageWidth - (2 * pageMargin)
            val lines = splitTextToFitWidth(it, maxWidth.toFloat(), bodyPaint)
            
            for (line in lines) {
                canvas.drawText(line, pageMargin.toFloat(), yPosition, bodyPaint)
                yPosition += 20f
            }
        }
        
        // Footer
        val footerText = "Generated by City Tailor App on ${
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        }"
        canvas.drawText(
            footerText,
            pageMargin.toFloat(),
            (pageHeight - pageMargin).toFloat(),
            smallPaint
        )
        
        document.finishPage(page)
    }
    
    private fun createDailyPlanPage(document: PdfDocument, dailyPlan: DailyPlan, pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        var canvas = page.canvas
        
        var yPosition = pageMargin + 50f
        
        // Day header
        val dayTitle = "Day ${dailyPlan.dayNumber}"
        canvas.drawText(dayTitle, pageMargin.toFloat(), yPosition, titlePaint)
        yPosition += 30f
        
        // Date
        val dateFormatted = formatDateString(dailyPlan.date)
        canvas.drawText(dateFormatted, pageMargin.toFloat(), yPosition, subtitlePaint)
        yPosition += 30f
        
        // Draw line
        canvas.drawLine(
            pageMargin.toFloat(),
            yPosition,
            (pageWidth - pageMargin).toFloat(),
            yPosition,
            linePaint
        )
        yPosition += 30f
        
        // Activities
        dailyPlan.activities.forEachIndexed { index, activity ->
            yPosition = drawActivity(canvas, activity, index + 1, yPosition)
            
            // Check if we need a new page
            if (yPosition > pageHeight - pageMargin - 100) {
                document.finishPage(page)
                
                // Create a new page
                val newPageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber + 1).create()
                val newPage = document.startPage(newPageInfo)
                canvas = newPage.canvas
                yPosition = pageMargin + 50f
                
                // Continuation header
                canvas.drawText("$dayTitle (continued)", pageMargin.toFloat(), yPosition, titlePaint)
                yPosition += 30f
                
                canvas.drawLine(
                    pageMargin.toFloat(),
                    yPosition,
                    (pageWidth - pageMargin).toFloat(),
                    yPosition,
                    linePaint
                )
                yPosition += 30f
            }
        }
        
        // Footer
        val footerText = "Page $pageNumber | Generated by City Tailor App"
        canvas.drawText(
            footerText,
            pageMargin.toFloat(),
            (pageHeight - pageMargin).toFloat(),
            smallPaint
        )
        
        document.finishPage(page)
    }
    
    private fun createRecommendationsPage(document: PdfDocument, travelPlan: TravelPlan, pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        
        var yPosition = pageMargin + 50f
        
        // Title
        canvas.drawText("Recommendations", pageMargin.toFloat(), yPosition, titlePaint)
        yPosition += 30f
        
        // Location
        canvas.drawText("For ${travelPlan.location}", pageMargin.toFloat(), yPosition, subtitlePaint)
        yPosition += 30f
        
        // Draw line
        canvas.drawLine(
            pageMargin.toFloat(),
            yPosition,
            (pageWidth - pageMargin).toFloat(),
            yPosition,
            linePaint
        )
        yPosition += 30f
        
        // Food recommendations
        travelPlan.recommendations?.food?.let {
            canvas.drawText("Food Recommendations", pageMargin.toFloat(), yPosition, headerPaint)
            yPosition += 25f
            
            it.forEach { food ->
                canvas.drawText("• $food", pageMargin.toFloat(), yPosition, bodyPaint)
                yPosition += 20f
            }
            
            yPosition += 15f
        }
        
        // Transport recommendations
        travelPlan.recommendations?.transport?.let {
            canvas.drawText("Transport Tips", pageMargin.toFloat(), yPosition, headerPaint)
            yPosition += 25f
            
            it.forEach { transport ->
                canvas.drawText("• $transport", pageMargin.toFloat(), yPosition, bodyPaint)
                yPosition += 20f
            }
            
            yPosition += 15f
        }
        
        // General tips
        travelPlan.recommendations?.tips?.let {
            canvas.drawText("Local Tips", pageMargin.toFloat(), yPosition, headerPaint)
            yPosition += 25f
            
            it.forEach { tip ->
                canvas.drawText("• $tip", pageMargin.toFloat(), yPosition, bodyPaint)
                yPosition += 20f
            }
        }
        
        // Footer
        val footerText = "Page $pageNumber | Generated by City Tailor App"
        canvas.drawText(
            footerText,
            pageMargin.toFloat(),
            (pageHeight - pageMargin).toFloat(),
            smallPaint
        )
        
        document.finishPage(page)
    }
    
    private fun drawActivity(canvas: Canvas, activity: Activity, index: Int, startYPosition: Float): Float {
        var yPosition = startYPosition
        
        // Activity number and title
        canvas.drawText("$index. ${activity.title}", pageMargin.toFloat(), yPosition, headerPaint)
        yPosition += 25f
        
        // Duration
        canvas.drawText("Duration: ${activity.duration}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        // Category
        canvas.drawText("Category: ${activity.category}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        // Location
        canvas.drawText("Location: ${activity.displayAddress}", pageMargin.toFloat(), yPosition, bodyPaint)
        yPosition += 20f
        
        // Price if available
        activity.price?.let {
            canvas.drawText("Price: $it", pageMargin.toFloat(), yPosition, bodyPaint)
            yPosition += 20f
        }
        
        // Rating if available
        activity.rating?.let {
            canvas.drawText("Rating: $it / 5.0", pageMargin.toFloat(), yPosition, bodyPaint)
            yPosition += 20f
        }
        
        // Description
        canvas.drawText("Description:", pageMargin.toFloat(), yPosition, bodyBoldPaint)
        yPosition += 20f
        
        // Split long description into multiple lines
        val maxWidth = pageWidth - (2 * pageMargin)
        val lines = splitTextToFitWidth(activity.description, maxWidth.toFloat(), bodyPaint)
        
        for (line in lines) {
            canvas.drawText(line, pageMargin.toFloat(), yPosition, bodyPaint)
            yPosition += 20f
        }
        
        // Add spacing before next activity
        yPosition += 20f
        
        return yPosition
    }
    
    private fun splitTextToFitWidth(text: String, maxWidth: Float, paint: Paint): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine.toString()} $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        
        return lines
    }
    
    private fun formatDateString(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE)
            date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        } catch (e: Exception) {
            dateString
        }
    }
}
