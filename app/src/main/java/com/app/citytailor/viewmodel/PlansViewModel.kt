package com.app.citytailor.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.citytailor.data.room.entity.SavedTravelPlan
import com.app.citytailor.data.store.TravelPlanStore
import com.app.citytailor.model.TravelPlan
import com.app.citytailor.user.UserManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlansViewModel(application: Application) : AndroidViewModel(application) {
    private val travelPlanStore = TravelPlanStore.getInstance(application)
    private val userManager = UserManager.getInstance(application)
    private val gson = Gson()

    private val _showPremiumDialog = MutableStateFlow(false)
    val showPremiumDialog: StateFlow<Boolean> = _showPremiumDialog.asStateFlow()

    private val _remainingFreePlans = MutableStateFlow(0)
    val remainingFreePlans: StateFlow<Int> = _remainingFreePlans.asStateFlow()

    private val _showSavedMessage = MutableStateFlow(false)
    val showSavedMessage: StateFlow<Boolean> = _showSavedMessage.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _currentPlan = MutableStateFlow<TravelPlan?>(null)
    val currentPlan: StateFlow<TravelPlan?> = _currentPlan.asStateFlow()

    private val _showTravelPlanView = MutableStateFlow(false)
    val showTravelPlanView: StateFlow<Boolean> = _showTravelPlanView.asStateFlow()

    private val _selectedDayNumber = MutableStateFlow(1)
    val selectedDayNumber: StateFlow<Int> = _selectedDayNumber.asStateFlow()

    val savedPlans: Flow<List<SavedTravelPlan>> = travelPlanStore.getAllPlans()

    init {
        updateRemainingFreePlans()
        updatePremiumStatus()
    }

    private fun updatePremiumStatus() {
        _isPremium.value = userManager.isPremium()
    }

    fun getParsedTravelPlan(savedPlan: SavedTravelPlan): TravelPlan {
        return gson.fromJson(savedPlan.planData, TravelPlan::class.java)
    }

    fun dismissPremiumDialog() {
        _showPremiumDialog.value = false
    }

    fun dismissSavedMessage() {
        _showSavedMessage.value = false
    }

    fun deletePlan(planId: String) {
        viewModelScope.launch {
            travelPlanStore.deleteTravelPlan(planId)
            updateRemainingFreePlans()
        }
    }

    private fun updateRemainingFreePlans() {
        viewModelScope.launch {
            _remainingFreePlans.value = travelPlanStore.getRemainingFreePlans()
        }
    }

    // Called when a plan should be saved
    suspend fun handleSavePlan(plan: TravelPlan): Boolean {
        if (!travelPlanStore.canSaveTravelPlan()) {
            _showPremiumDialog.value = true
            return false
        }

        val saved = travelPlanStore.saveTravelPlan(plan)
        if (saved) {
            _showSavedMessage.value = true
            updateRemainingFreePlans()
        }
        return saved
    }

    fun navigateToPremium() {
        _showPremiumDialog.value = false
        // Navigation to PremiumView will be handled by the UI layer
    }

    suspend fun onPremiumPurchased() {
        userManager.setPremium(true)
        updatePremiumStatus()
        updateRemainingFreePlans()
    }

    /**
     * Used to observe plan limits when app comes to foreground
     */
    fun refreshPlanStatus() {
        updatePremiumStatus()
        updateRemainingFreePlans()
    }

    fun setCurrentPlan(plan: TravelPlan) {
        _currentPlan.value = plan
    }

    fun setShowTravelPlanView(show: Boolean) {
        _showTravelPlanView.value = show
    }

    fun setSelectedDayNumber(dayNumber: Int) {
        _selectedDayNumber.value = dayNumber
    }

    companion object {
        class Factory(private val application: Application) : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PlansViewModel::class.java)) {
                    return PlansViewModel(application) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
