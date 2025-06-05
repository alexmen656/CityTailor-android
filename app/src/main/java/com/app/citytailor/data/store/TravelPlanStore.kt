package com.app.citytailor.data.store

import android.content.Context
import com.app.citytailor.data.room.entity.SavedTravelPlan
import com.app.citytailor.data.room.AppDatabase
import com.app.citytailor.model.TravelPlan
import com.app.citytailor.user.UserManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.*

class TravelPlanStore private constructor(context: Context) {
    private val database: AppDatabase = AppDatabase.getDatabase(context)
    private val savedTravelPlanDao = database.savedTravelPlanDao()
    private val userManager = UserManager.getInstance(context)
    private val gson = Gson()

    companion object {
        const val FREE_PLAN_LIMIT = 3
        
        @Volatile
        private var instance: TravelPlanStore? = null

        fun getInstance(context: Context): TravelPlanStore {
            return instance ?: synchronized(this) {
                instance ?: TravelPlanStore(context.applicationContext).also { instance = it }
            }
        }
    }

    suspend fun saveTravelPlan(travelPlan: TravelPlan): Boolean {
        if (!canSaveTravelPlan()) return false

        val savedPlan = SavedTravelPlan(
            id = UUID.randomUUID().toString(),
            location = travelPlan.location,
            startDate = travelPlan.startDate,
            endDate = travelPlan.endDate,
            creationDate = Date(),
            planData = gson.toJson(travelPlan)
            // imageUrl and photographerName can be added later if needed
        )

        savedTravelPlanDao.insert(savedPlan)
        return true
    }

    suspend fun deleteTravelPlan(planId: String) {
        savedTravelPlanDao.deleteById(planId)
    }

    fun getAllPlans(): Flow<List<SavedTravelPlan>> {
        return savedTravelPlanDao.getAllPlans()
    }

    suspend fun canSaveTravelPlan(): Boolean {
        if (userManager.isPremium()) return true
        val count = savedTravelPlanDao.getCount()
        return count < FREE_PLAN_LIMIT
    }

    suspend fun getRemainingFreePlans(): Int {
        if (userManager.isPremium()) return Int.MAX_VALUE
        val count = savedTravelPlanDao.getCount()
        return kotlin.math.max(0, FREE_PLAN_LIMIT - count)
    }
}
