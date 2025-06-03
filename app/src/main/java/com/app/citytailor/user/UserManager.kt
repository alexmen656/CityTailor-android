package com.app.citytailor.user

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user-related functionality including premium status and free plan tracking.
 */
class UserManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private var instance: UserManager? = null
        
        fun getInstance(context: Context): UserManager {
            if (instance == null) {
                instance = UserManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    /**
     * Checks if the user has premium status.
     * @return true if the user is premium, false otherwise
     */
    fun isPremium(): Boolean {
        return sharedPreferences.getBoolean("is_premium", false)
    }
    
    /**
     * Sets the premium status for the user.
     * @param isPremium the premium status to set
     */
    fun setPremium(isPremium: Boolean) {
        sharedPreferences.edit().putBoolean("is_premium", isPremium).apply()
    }
    
    /**
     * Gets the number of remaining free travel plans the user can generate.
     * @return the number of remaining free plans
     */
    fun getRemainingFreePlans(): Int {
        return sharedPreferences.getInt("remaining_free_plans", 3)
    }
    
    /**
     * Decrements the number of remaining free travel plans by 1.
     * @return the updated number of remaining free plans
     */
    fun decrementRemainingFreePlans(): Int {
        val currentPlans = getRemainingFreePlans()
        if (currentPlans > 0) {
            val newValue = currentPlans - 1
            sharedPreferences.edit().putInt("remaining_free_plans", newValue).apply()
            return newValue
        }
        return 0
    }
    
    /**
     * Resets the number of remaining free travel plans to the default value (3).
     */
    fun resetRemainingFreePlans() {
        sharedPreferences.edit().putInt("remaining_free_plans", 3).apply()
    }
}