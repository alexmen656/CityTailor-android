package com.app.citytailor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.citytailor.model.City
import com.app.citytailor.ui.components.*
import com.app.citytailor.viewmodel.MainViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

@Composable
fun ContentView(
    viewModel: MainViewModel = viewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val showSuggestions by viewModel.showSuggestions.collectAsState()
    val region by viewModel.region.collectAsState()
    val mapAnnotations by viewModel.mapAnnotations.collectAsState()
    val selectedAnnotation by viewModel.selectedAnnotation.collectAsState()
    val travelPlan by viewModel.travelPlan.collectAsState()
    val selectedDayNumber by viewModel.selectedDayNumber.collectAsState()
    val showDateSelectionView by viewModel.showDateSelectionView.collectAsState()
    val showActivityDetails by viewModel.showActivityDetails.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState()
    val showSaveFeedback by viewModel.showSaveFeedback.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content based on selected tab
        when (selectedTab) {
            0 -> PlansView()
            1 -> com.app.citytailor.ui.screens.DiscoverView()
            2 -> MainMapView(
                viewModel = viewModel,
                region = region,
                searchText = searchText,
                isSearching = isSearching,
                showSuggestions = showSuggestions,
                mapAnnotations = mapAnnotations,
                selectedAnnotation = selectedAnnotation,
                travelPlan = travelPlan,
                selectedDayNumber = selectedDayNumber,
                onMapClick = {
                    if (!isSearching) {
                        viewModel.setShowSuggestions(false)
                        keyboardController?.hide()
                    }
                }
            )
            3 -> MediaView()
            4 -> SettingsView()
        }
        
        // Custom Tab Bar at the bottom - extends to screen edge
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            CustomTabBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setSelectedTab(it) }
                // No navigationBarsPadding here - handled inside CustomTabBar
            )
        }
    }
    
    // Modal sheets and dialogs
    if (showDateSelectionView) {
        // TODO: Implement DateSelectionView as a modal
    }
    
    if (showActivityDetails && selectedActivity != null) {
        // TODO: Implement ActivityDetailView as a modal
    }
    
    if (showSaveFeedback) {
        LaunchedEffect(showSaveFeedback) {
            // TODO: Show snackbar or alert dialog
            viewModel.setShowSaveFeedback(false)
        }
    }
}

@Composable
private fun MainMapView(
    viewModel: MainViewModel,
    region: CameraPosition,
    searchText: String,
    isSearching: Boolean,
    showSuggestions: Boolean,
    mapAnnotations: List<com.app.citytailor.model.MapAnnotation>,
    selectedAnnotation: com.app.citytailor.model.MapAnnotation?,
    travelPlan: com.app.citytailor.model.TravelPlan?,
    selectedDayNumber: Int,
    onMapClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Map as background
        MapView(
            region = region,
            annotations = mapAnnotations,
            selectedAnnotation = selectedAnnotation,
            onMapClick = onMapClick,
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Add padding for status bar
                .padding(bottom = 100.dp) // Account for tab bar + navigation bar (increased from 80dp)
        ) {
            
            // Search Bar
            SearchBar(
                searchText = searchText,
                onSearchTextChange = { viewModel.setSearchText(it) },
                isSearching = isSearching,
                onIsSearchingChange = { viewModel.setIsSearching(it) },
                onSearchAction = { viewModel.searchLocation(searchText) },
                onShowSuggestions = { viewModel.setShowSuggestions(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Search suggestions (if any)
            if (showSuggestions && searchText.isNotEmpty()) {
                // TODO: Implement SearchSuggestionsView
            }
            
            // Popular cities when no search text and no travel plan
            if (searchText.isEmpty() && travelPlan == null) {
                SuggestedCityTags(
                    cities = City.popularCities,
                    onCitySelected = { city ->
                        viewModel.setSearchText(city.name)
                        viewModel.searchLocation(city.name)
                    }
                )
            }
            
            // Day buttons when travel plan exists
            if (travelPlan != null && !travelPlan.dailyPlans.isNullOrEmpty()) {
                DayButtonsView(
                    dailyPlans = travelPlan.dailyPlans,
                    selectedDayNumber = selectedDayNumber,
                    onDaySelected = { dayNumber ->
                        viewModel.setSelectedDayNumber(dayNumber)
                        viewModel.updateMapForSelectedDay(travelPlan.dailyPlans, travelPlan.location)
                    }
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Travel Plan Summary at bottom
            if (travelPlan != null) {
                TravelPlanSummaryView(
                    plan = travelPlan,
                    onTap = {
                        viewModel.setShowDateSelectionView(true)
                    }
                )
            }
        }
    }
}

// Placeholder composables for other tabs
@Composable
private fun PlansView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text("Plans View - Coming Soon")
    }
}

@Composable
private fun MediaView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text("Media View - Coming Soon")
    }
}

@Composable
private fun SettingsView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings View - Coming Soon")
    }
}
