package com.app.citytailor.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Utility class for formatting dates in the app
 */
object DateFormatterUtils {
    
    private val fullDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.")
    private val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    private val weekdayFormatter = DateTimeFormatter.ofPattern("EEEE")
    private val iso8601Formatter = DateTimeFormatter.ISO_DATE
    
    /**
     * Format a date string (ISO format: yyyy-MM-dd) to a user-friendly format (dd.MM.yyyy)
     */
    fun formatFullDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, iso8601Formatter)
            date.format(fullDateFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Format a date string (ISO format: yyyy-MM-dd) to a short format (dd.MM.)
     */
    fun formatShortDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, iso8601Formatter)
            date.format(shortDateFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Format a date string to show only month and year
     */
    fun formatMonthYear(dateString: String, locale: Locale = Locale.getDefault()): String {
        return try {
            val date = LocalDate.parse(dateString, iso8601Formatter)
            date.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
        } catch (e: Exception) {
            dateString
        }
    }
    
    /**
     * Get the weekday name for a date
     */
    fun getWeekday(dateString: String, locale: Locale = Locale.getDefault()): String {
        return try {
            val date = LocalDate.parse(dateString, iso8601Formatter)
            date.format(DateTimeFormatter.ofPattern("EEEE", locale))
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Parse a string date to LocalDate
     */
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString, iso8601Formatter)
        } catch (e: Exception) {
            null
        }
    }
}