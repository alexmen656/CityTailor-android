package com.app.citytailor.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class TravelType(val displayName: String, val icon: ImageVector) {
    SOLO("Solo", Icons.Default.Home),
    COUPLE("Couple", Icons.Default.Home),
    FAMILY("Family", Icons.Default.Home),
    FRIENDS("Friends", Icons.Default.Home),
    BUSINESS("Business", Icons.Default.Home);

    companion object {
        fun getLocalizedName(type: TravelType): String {
            // TODO: Implement localization
            return when (type) {
                SOLO -> "Solo"
                COUPLE -> "Paar"
                FAMILY -> "Familie"
                FRIENDS -> "Freunde"
                BUSINESS -> "Geschäftlich"
            }
        }
    }
}

enum class TransportationType(val displayName: String, val icon: ImageVector) {
    WALKING("Walking", Icons.Default.Home),
    PUBLIC_TRANSPORT("Public Transport", Icons.Default.Home),
    CAR("Car", Icons.Default.Home),
    BICYCLE("Bicycle", Icons.Default.Home),
    MIXED("Mixed", Icons.Default.Home);

    companion object {
        fun getLocalizedName(type: TransportationType): String {
            // TODO: Implement localization
            return when (type) {
                WALKING -> "Zu Fuß"
                PUBLIC_TRANSPORT -> "Öffentlich"
                CAR -> "Auto"
                BICYCLE -> "Fahrrad"
                MIXED -> "Gemischt"
            }
        }
    }
}

enum class TravelMode(val displayName: String, val icon: ImageVector) {
    RELAXED("Relaxed", Icons.Default.Home),
    MODERATE("Moderate", Icons.Default.Home),
    INTENSIVE("Intensive", Icons.Default.Home);

    companion object {
        fun getLocalizedName(mode: TravelMode): String {
            // TODO: Implement localization
            return when (mode) {
                RELAXED -> "Entspannt"
                MODERATE -> "Moderat"
                INTENSIVE -> "Intensiv"
            }
        }
    }
}

enum class BudgetLevel(val displayName: String, val symbol: String) {
    BUDGET("Budget", "€"),
    MEDIUM("Medium", "€€"),
    LUXURY("Luxury", "€€€");

    companion object {
        fun getLocalizedName(level: BudgetLevel): String {
            // TODO: Implement localization
            return when (level) {
                BUDGET -> "Budget"
                MEDIUM -> "Mittel"
                LUXURY -> "Luxus"
            }
        }
    }
}
