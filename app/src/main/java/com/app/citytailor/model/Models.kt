package com.app.citytailor.model

import com.google.android.gms.maps.model.LatLng

enum class BudgetLevel(val key: String, val symbol: String) {
    LOW("budget_level_low", "$"),
    MEDIUM("budget_level_medium", "$$"),
    HIGH("budget_level_high", "$$$"),
    LUXURY("budget_level_luxury", "$$$$")
}

enum class TransportationType(val key: String, val icon: String) {
    WALKING("transport_type_walking", "directions_walk"),
    PUBLIC_TRANSPORT("transport_type_public", "directions_bus"),
    BICYCLE("transport_type_bicycle", "directions_bike"),
    CAR("transport_type_car", "directions_car"),
    MIXED("transport_type_mix", "swap_horiz")
}

enum class TravelMode(val key: String, val icon: String) {
    RELAXING("travel_mode_relaxing", "spa"),
    MODERATE("travel_mode_moderate", "directions_walk"),
    ACTIVE("travel_mode_active", "hiking")
}

enum class TravelType(val key: String, val icon: String) {
    SOLO("travel_type_solo", "person"),
    COUPLE("travel_type_couple", "favorite"),
    FAMILY("travel_type_family", "group"),
    FRIENDS("travel_type_friends", "groups"),
    BUSINESS("travel_type_business", "business_center")
}

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
    val imageUrl: String?
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
    val travelType: TravelType
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
