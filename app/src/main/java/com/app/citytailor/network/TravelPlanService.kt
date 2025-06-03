package com.app.citytailor.network

import android.util.Log
import com.app.citytailor.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class TravelPlanService private constructor() {
    private val baseUrl = "https://city-tailor-backend-k9s6rk7eu-alexmen656s-projects.vercel.app/api/trips"
    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    companion object {
        val shared = TravelPlanService()
    }
    
    // Use this function to test the connection to the backend
    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(baseUrl)
                .get()
                .build()
            
            client.newCall(request).execute().use { response ->
                val responseCode = response.code
                Log.d("TravelPlanService", "Connection test result: Response code $responseCode")
                return@withContext "Connection test result: Response code $responseCode"
            }
        } catch (e: Exception) {
            Log.e("TravelPlanService", "Connection test failed: ${e.message}", e)
            return@withContext "Connection test failed: ${e.message}"
        }
    }

    suspend fun generateTravelPlan(
        location: String,
        startDate: LocalDate,
        endDate: LocalDate,
        travelType: TravelType,
        transportationType: TransportationType,
        travelMode: TravelMode,
        budgetLevel: BudgetLevel,
        isPremium: Boolean = false,
        language: String = "de"
    ): TravelPlan = withContext(Dispatchers.IO) {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val durationInDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

        val jsonObject = JSONObject().apply {
            put("location", location)
            put("startDate", startDate.format(dateFormatter))
            put("endDate", endDate.format(dateFormatter))
            put("durationInDays", durationInDays)
            put("travelType", travelType.name.lowercase())
            put("transportationType", transportationType.name.lowercase())
            put("travelMode", travelMode.name.lowercase())
            put("language", language)

            if (isPremium) {
                put("budgetLevel", budgetLevel.name.lowercase())
            }

            // Dummy interests - could be replaced with actual user interests
            val interests = JSONArray()
            val dummyInterest = JSONObject().apply {
                put("name", "Sightseeing")
                put("rating", 5)
            }
            interests.put(dummyInterest)
            put("interests", interests)
        }

        val requestBody = jsonObject.toString()
        Log.d("TravelPlanService", "Sending request to: $baseUrl")
        Log.d("TravelPlanService", "Request body: $requestBody")
        
        try {
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url(baseUrl)
                .post(requestBody.toRequestBody(mediaType))
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("X-Premium-Status", if (isPremium) "true" else "false")
                .build()
            
            Log.d("TravelPlanService", "Sending HTTP request with headers: ${request.headers}")
            
            client.newCall(request).execute().use { response ->
                val responseCode = response.code
                Log.d("TravelPlanService", "Response code: $responseCode")
                
                if (responseCode == 200 || responseCode == 201) {
                    val responseBody = response.body?.string()
                    Log.d("TravelPlanService", "Response body: $responseBody")
                    
                    // Parse response to TravelPlan
                    val responseJson = JSONObject(responseBody)
                    val success = responseJson.getBoolean("success")
                    val message = responseJson.getString("message")

                    if (success) {
                        val data = responseJson.getJSONObject("data")
                        parseTravelPlan(data, location, startDate, endDate, travelType, transportationType, travelMode, budgetLevel)
                    } else {
                        throw Exception(message)
                    }
                } else {
                    val errorMessage = "HTTP Error: $responseCode - ${response.message}"
                    Log.e("TravelPlanService", "Error response: $errorMessage")
                    throw Exception(errorMessage)
                }
            }
        } catch (e: Exception) {
            Log.e("TravelPlanService", "Exception during API call: ${e.message}", e)
            e.printStackTrace()
            throw e
        }
    }

    private fun parseTravelPlan(
        json: JSONObject,
        location: String,
        startDate: LocalDate,
        endDate: LocalDate,
        travelType: TravelType,
        transportationType: TransportationType,
        travelMode: TravelMode,
        budgetLevel: BudgetLevel
    ): TravelPlan {
        val dailyPlans = if (json.has("dailyPlans")) {
            val dailyPlansArray = json.getJSONArray("dailyPlans")
            val plans = mutableListOf<DailyPlan>()
            
            for (i in 0 until dailyPlansArray.length()) {
                val dayJson = dailyPlansArray.getJSONObject(i)
                val date = dayJson.getString("date")
                val dayNumber = dayJson.getInt("dayNumber")
                
                val activitiesArray = dayJson.getJSONArray("activities")
                val activities = mutableListOf<Activity>()
                
                for (j in 0 until activitiesArray.length()) {
                    val activityJson = activitiesArray.getJSONObject(j)
                    val activity = Activity(
                        title = activityJson.getString("title"),
                        description = activityJson.getString("description"),
                        location = activityJson.optString("mapAddress", ""),
                        latitude = activityJson.optDouble("latitude", 0.0),
                        longitude = activityJson.optDouble("longitude", 0.0),
                        duration = activityJson.optString("time", "1 hour"),
                        category = activityJson.getString("category"),
                        price = if (activityJson.has("price")) activityJson.getString("price") else null,
                        rating = if (activityJson.has("rating")) activityJson.getDouble("rating") else null,
                        imageUrl = if (activityJson.has("imageUrl")) activityJson.getString("imageUrl") else null
                    )
                    activities.add(activity)
                }
                
                plans.add(DailyPlan(dayNumber, date, activities))
            }
            plans
        } else {
            null
        }

        // Create a TravelPlan object from the parsed data
        return TravelPlan(
            id = "plan-${System.currentTimeMillis()}",
            location = location,
            startDate = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            dailyPlans = dailyPlans,
            travelType = travelType,
            transportationType = transportationType,
            travelMode = travelMode,
            budgetLevel = budgetLevel
        )
    }
}