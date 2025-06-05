package com.app.citytailor.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.citytailor.model.*
import com.app.citytailor.user.UserManager
import com.app.citytailor.data.store.TravelPlanStore
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val application: android.app.Application
) : ViewModel() {
    
    private val _selectedTab = MutableStateFlow(2)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private val _region = MutableStateFlow(
        CameraPosition.Builder()
            .target(LatLng(52.520008, 13.404954))
            .zoom(10f)
            .build()
    )
    val region: StateFlow<CameraPosition> = _region.asStateFlow()
    
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    
    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()
    
    private val _showSuggestions = MutableStateFlow(false)
    val showSuggestions: StateFlow<Boolean> = _showSuggestions.asStateFlow()
    
    private val _selectedLocation = MutableStateFlow("")
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()
    
    private val _showDateSelectionView = MutableStateFlow(false)
    val showDateSelectionView: StateFlow<Boolean> = _showDateSelectionView.asStateFlow()
    
    private val _showTravelPlanView = MutableStateFlow(false)
    val showTravelPlanView: StateFlow<Boolean> = _showTravelPlanView.asStateFlow()
    
    private val _mapAnnotations = MutableStateFlow<List<MapAnnotation>>(emptyList())
    val mapAnnotations: StateFlow<List<MapAnnotation>> = _mapAnnotations.asStateFlow()
    
    private val _selectedAnnotation = MutableStateFlow<MapAnnotation?>(null)
    val selectedAnnotation: StateFlow<MapAnnotation?> = _selectedAnnotation.asStateFlow()
    
    private val _travelPlan = MutableStateFlow<TravelPlan?>(null)
    val travelPlan: StateFlow<TravelPlan?> = _travelPlan.asStateFlow()
    
    private val _selectedDayNumber = MutableStateFlow(1)
    val selectedDayNumber: StateFlow<Int> = _selectedDayNumber.asStateFlow()
    
    private val _showActivityDetails = MutableStateFlow(false)
    val showActivityDetails: StateFlow<Boolean> = _showActivityDetails.asStateFlow()
    
    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity.asStateFlow()
    
    private val _showSaveFeedback = MutableStateFlow(false)
    val showSaveFeedback: StateFlow<Boolean> = _showSaveFeedback.asStateFlow()
    
    private val _initialLocationSet = MutableStateFlow(false)
    val initialLocationSet: StateFlow<Boolean> = _initialLocationSet.asStateFlow()
    
    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }
    
    fun setSearchText(text: String) {
        _searchText.value = text
    }
    
    fun setIsSearching(searching: Boolean) {
        _isSearching.value = searching
    }
    
    fun setShowSuggestions(show: Boolean) {
        _showSuggestions.value = show
    }
    
    fun setShowTravelPlanView(show: Boolean) {
        _showTravelPlanView.value = show
    }
    
    fun setShowDateSelectionView(show: Boolean) {
        _showDateSelectionView.value = show
    }
    
    fun setSelectedLocation(location: String) {
        _selectedLocation.value = location
    }
    
    fun setRegion(newRegion: CameraPosition) {
        _region.value = newRegion
    }
    
    fun setTravelPlan(plan: TravelPlan?) {
        _travelPlan.value = plan
    }
    
    fun setMapAnnotations(annotations: List<MapAnnotation>) {
        _mapAnnotations.value = annotations
    }
    
    fun setSelectedAnnotation(annotation: MapAnnotation?) {
        _selectedAnnotation.value = annotation
    }
    
    fun setSelectedDayNumber(dayNumber: Int) {
        _selectedDayNumber.value = dayNumber
    }
    
    fun setShowActivityDetails(show: Boolean) {
        _showActivityDetails.value = show
    }
    
    fun setSelectedActivity(activity: Activity?) {
        _selectedActivity.value = activity
    }
    
    fun setShowSaveFeedback(show: Boolean) {
        _showSaveFeedback.value = show
    }
    
    fun setInitialLocationSet(set: Boolean) {
        _initialLocationSet.value = set
    }
    
    fun searchLocation(searchQuery: String) {
        viewModelScope.launch {
            // Update search text and hide suggestions
            setSearchText(searchQuery)
            setShowSuggestions(false)
            
            // Set the selected location
            setSelectedLocation(searchQuery)
            
            // Clear any existing travel plan and map annotations
            setTravelPlan(null)
            setMapAnnotations(emptyList())
            
            // TODO: In a real implementation, we would use Google Places API or Geocoding
            // to get the actual coordinates and update the map region
            
            // Show the DateSelectionView after a short delay (similar to iOS implementation)
            kotlinx.coroutines.delay(1000)
            setShowDateSelectionView(true)
        }
    }
    
    fun updateMapForSelectedDay(dailyPlans: List<DailyPlan>, city: String) {
        val selectedDayPlan = dailyPlans.find { it.dayNumber == _selectedDayNumber.value }
        if (selectedDayPlan == null) {
            setMapAnnotations(emptyList())
            return
        }
        
        val annotations = selectedDayPlan.activities.map { activity ->
            MapAnnotation(
                coordinate = activity.coordinate,
                title = activity.title,
                subtitle = activity.description
            )
        }
        
        setMapAnnotations(annotations)
        
        // Center map on first activity if available
        if (annotations.isNotEmpty()) {
            val firstActivity = annotations.first()
            val newPosition = CameraPosition.Builder()
                .target(firstActivity.coordinate)
                .zoom(12f)
                .build()
            setRegion(newPosition)
        }
    }
    
    fun saveTravelPlan(plan: TravelPlan?) = viewModelScope.launch {
        plan ?: return@launch
        
        try {
            val userManager = UserManager.getInstance(application)
            val travelPlanStore = TravelPlanStore.getInstance(application)
            
            if (travelPlanStore.canSaveTravelPlan()) {
                if (!userManager.isPremium()) {
                    userManager.decrementRemainingFreePlans()
                }
                travelPlanStore.saveTravelPlan(plan)
                setShowSaveFeedback(true)
            }
        } catch (e: Exception) {
            Log.e("MainViewModel", "Error saving travel plan", e)
        }
    }
    
    companion object {
        fun factory(application: android.app.Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                        return MainViewModel(application) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
