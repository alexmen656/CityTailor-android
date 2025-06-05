package com.app.citytailor.data.room.dao

import androidx.room.*
import com.app.citytailor.data.room.entity.SavedTravelPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedTravelPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: SavedTravelPlan)
    
    @Delete
    suspend fun delete(plan: SavedTravelPlan)
    
    @Query("SELECT * FROM saved_travel_plans ORDER BY creationDate DESC")
    fun getAllPlans(): Flow<List<SavedTravelPlan>>
    
    @Query("SELECT COUNT(*) FROM saved_travel_plans")
    suspend fun getCount(): Int
    
    @Query("DELETE FROM saved_travel_plans WHERE id = :planId")
    suspend fun deleteById(planId: String)
    
    @Query("SELECT * FROM saved_travel_plans WHERE id = :planId")
    suspend fun getPlanById(planId: String): SavedTravelPlan?
}
