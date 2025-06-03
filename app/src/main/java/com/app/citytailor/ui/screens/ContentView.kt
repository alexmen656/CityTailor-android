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
    val showTravelPlanView by viewModel.showTravelPlanView.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main content based on selected tab
        when (selectedTab) {
            0 -> PlansScreen()
            1 -> com.app.citytailor.ui.screens.DiscoverView()
            2 -> MainMapScreen(
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
            3 -> MediaScreen()
            4 -> SettingsScreen()
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
        DateSelectionView(
            locationName = viewModel.selectedLocation.collectAsState().value,
            onTravelPlanReceived = { travelPlan ->
                viewModel.setTravelPlan(travelPlan)
                viewModel.setSelectedDayNumber(1)
                
                // Update map for the first day
                travelPlan?.let { plan ->
                    plan.dailyPlans?.let { dailyPlans ->
                        viewModel.updateMapForSelectedDay(dailyPlans, plan.location)
                    }
                }
                
                // TODO: Save travel plan if user has remaining free plans
                // TravelPlanStore.shared.canSaveTravelPlan(isPremium: false, context: context)
                // TravelPlanStore.shared.saveTravelPlan(travelPlan, context: context)
                // viewModel.setShowSaveFeedback(true)
                
                viewModel.setShowDateSelectionView(false)
            },
            onDismiss = {
                viewModel.setShowDateSelectionView(false)
            }
        )
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
    
    // Add TravelPlanView modal
    if (showTravelPlanView) {
        travelPlan?.let { plan ->
            TravelPlanView(
                travelPlan = plan,
                selectedDayNumber = selectedDayNumber,
                onDaySelected = { dayNumber ->
                    viewModel.setSelectedDayNumber(dayNumber)
                    plan.dailyPlans?.let { dailyPlans ->
                        viewModel.updateMapForSelectedDay(dailyPlans, plan.location)
                    }
                },
                onActivitySelected = { activity ->
                    viewModel.setSelectedActivity(activity)
                    viewModel.setShowActivityDetails(true)
                },
                onDismiss = {
                    viewModel.setShowTravelPlanView(false)
                }
            )
        }
    }
}

@Composable
fun MainMapScreen(
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
                .statusBarsPadding()
                .padding(bottom = 100.dp)
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
            travelPlan?.dailyPlans?.let { dailyPlans ->
                if (dailyPlans.isNotEmpty()) {
                    DayButtonsView(
                        dailyPlans = dailyPlans,
                        selectedDayNumber = selectedDayNumber,
                        onDaySelected = { dayNumber ->
                            viewModel.setSelectedDayNumber(dayNumber)
                            viewModel.updateMapForSelectedDay(dailyPlans, travelPlan.location)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Travel Plan Summary at bottom
            if (travelPlan != null) {
                TravelPlanSummaryView(
                    plan = travelPlan,
                    onTap = {
                        viewModel.setShowTravelPlanView(true)
                    }
                )
            }
        }
        
        // Search suggestions if any
        if (showSuggestions && searchText.isNotEmpty()) {
            // TODO: Implement SearchSuggestionsView
        }
    }
}

@Composable
fun PlansScreen() {
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
fun MediaScreen() {
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
fun SettingsScreen() {
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
