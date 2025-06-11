package com.app.citytailor.network

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class DateTypeAdapter : JsonSerializer<Date>, JsonDeserializer<Date> {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return if (src != null) {
            JsonPrimitive(dateFormat.format(src))
        } else {
            JsonNull.INSTANCE
        }
    }
    
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date? {
        return if (json != null && !json.isJsonNull) {
            try {
                dateFormat.parse(json.asString)
            } catch (e: Exception) {
                // Try alternative formats
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(json.asString)
                } catch (e2: Exception) {
                    Date() // Return current date as fallback
                }
            }
        } else {
            null
        }
    }
}
