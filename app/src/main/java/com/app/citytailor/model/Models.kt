package com.app.citytailor.model

import com.google.android.gms.maps.model.LatLng

data class City(
    val name: String,
    val emoji: String
) {
    companion object {
        val popularCities = listOf(
            City("Paris", "🗼"),
            City("London", "🎡"),
            City("New York", "🗽"),
            City("Tokyo", "🎌"),
            City("Rome", "🏛️"),
            City("Barcelona", "🎨"),
            City("Berlin", "🧸"),
            City("Amsterdam", "🚲"),
            City("Vienna", "🎼"),
            City("Prague", "🕰️")
        )
    }
}

data class Activity(
    val title: String,
    val description: String,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val duration: String,
    val category: String,
    val price: String?,
    val rating: Double?,
    val imageUrl: String?,
    val mapAddress: String = location,
    val displayAddress: String = location
) {
    val coordinate: LatLng
        get() = LatLng(latitude, longitude)
}

data class DailyPlan(
    val dayNumber: Int,
    val date: String,
    val activities: List<Activity>
)

data class TravelPlan(
    val id: String,
    val location: String,
    val startDate: String,
    val endDate: String,
    val dailyPlans: List<DailyPlan>?,
    val budgetLevel: BudgetLevel,
    val transportationType: TransportationType,
    val travelMode: TravelMode,
    val travelType: TravelType,
    val recommendations: Recommendations? = null,
    val info: String? = null
)

data class MapAnnotation(
    val coordinate: LatLng,
    val title: String,
    val subtitle: String
)

data class BackendResponse(
    val success: Boolean,
    val message: String,
    val data: TravelPlan
)

// Discover View Models
data class FeaturedItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val iconName: String,
    val color: androidx.compose.ui.graphics.Color
)

data class CategoryItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val titleKey: String,
    val iconName: String,
    val color: androidx.compose.ui.graphics.Color,
    val items: List<String>
)

data class Recommendations(
    val food: List<String>,
    val transport: List<String>,
    val tips: List<String>
)
