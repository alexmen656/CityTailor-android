package com.app.citytailor.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

class LanguageManager {
    var currentLanguage by mutableStateOf("de") // Default to German like iOS app
    
    private val translations = mapOf(
        "de" to mapOf(
            "discover" to "Entdecken",
            "featured" to "Empfohlen",
            "categories" to "Kategorien",
            "landmarks" to "Sehenswürdigkeiten",
            "food" to "Essen",
            "activities" to "Aktivitäten",
            "events" to "Veranstaltungen",
            "trending" to "Trending",
            "local_tips" to "Geheimtipps",
            "items" to "Elemente"
        ),
        "en" to mapOf(
            "discover" to "Discover",
            "featured" to "Featured",
            "categories" to "Categories",
            "landmarks" to "Landmarks",
            "food" to "Food",
            "activities" to "Activities",
            "events" to "Events",
            "trending" to "Trending",
            "local_tips" to "Local Tips",
            "items" to "Items"
        )
    )
    
    fun localize(key: String): String {
        return translations[currentLanguage]?.get(key) ?: key
    }
    
    fun setLanguage(language: String) {
        currentLanguage = language
    }
    
    companion object {
        private var instance: LanguageManager? = null
        
        fun getInstance(): LanguageManager {
            if (instance == null) {
                instance = LanguageManager()
            }
            return instance!!
        }
    }
}

@Composable
fun LocalizedString(key: String): String {
    return LanguageManager.getInstance().localize(key)
}
