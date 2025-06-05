package com.app.citytailor.data.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.app.citytailor.data.room.converter.DateConverter
import com.app.citytailor.model.ImageInfo
import java.util.*

@Entity(tableName = "saved_travel_plans")
data class SavedTravelPlan(
    @PrimaryKey
    val id: String,
    val location: String,
    val startDate: String,  
    val endDate: String,
    val creationDate: Date,
    val planData: String, // JSON string of TravelPlan
    val imageUrl: String? = null, 
    val imageDescription: String? = null,
    val photographerName: String? = null,
    val photographerLink: String? = null
) {
    fun getImageInfo(): ImageInfo? {
        return if (imageUrl != null && photographerName != null && photographerLink != null) {
            ImageInfo(
                url = imageUrl,
                description = imageDescription ?: "",
                photographer = photographerName,
                photographerLink = photographerLink
            )
        } else null
    }
}
