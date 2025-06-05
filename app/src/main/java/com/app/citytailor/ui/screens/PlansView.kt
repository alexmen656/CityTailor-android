package com.app.citytailor.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.app.citytailor.data.room.entity.SavedTravelPlan
import com.app.citytailor.data.store.TravelPlanStore
import com.app.citytailor.model.TravelPlan
import com.app.citytailor.viewmodel.PlansViewModel
import com.app.citytailor.user.UserManager
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PlansView(
    viewModel: PlansViewModel = viewModel(
        factory = PlansViewModel.Companion.Factory(LocalContext.current.applicationContext as Application)
    )
) {
    val plans by viewModel.savedPlans.collectAsState(initial = emptyList())
    val showPremiumDialog by viewModel.showPremiumDialog.collectAsState()
    val showSavedMessage by viewModel.showSavedMessage.collectAsState()
    val remainingFreePlans by viewModel.remainingFreePlans.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()
    val currentPlan by viewModel.currentPlan.collectAsState()
    val showTravelPlanView by viewModel.showTravelPlanView.collectAsState()
    val selectedDayNumber by viewModel.selectedDayNumber.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Travel Plans List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (plans.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Travel Plans Yet",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Your saved travel plans will appear here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(plans) { savedPlan ->
                    val plan = viewModel.getParsedTravelPlan(savedPlan)
                    TravelPlanCard(
                        plan = plan,
                        onPlanClick = { 
                            viewModel.setCurrentPlan(plan)
                            viewModel.setShowTravelPlanView(true)
                        },
                        onDeleteClick = { viewModel.deletePlan(savedPlan.id) }
                    )
                }
            }
        }

        // Show premium upgrade card if user is not premium and has no remaining free plans
        if (!isPremium && remainingFreePlans == 0 && plans.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Limit Reached",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "You have reached the maximum of 3 travel plans for the free version. Upgrade to Premium for unlimited plans.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Button(
                        onClick = { viewModel.navigateToPremium() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upgrade to Premium")
                    }
                }
            }
        }
        
        // Show remaining free plans if user is not premium
        else if (!isPremium && remainingFreePlans > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Remaining Free Plans: $remainingFreePlans",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.navigateToPremium() }) {
                        Text("Upgrade to Premium")
                    }
                }
            }
        }
    }

    if (showPremiumDialog) {
        PremiumDialog(
            onDismiss = { viewModel.dismissPremiumDialog() }
        )
    }
    
    if (showSavedMessage) {
        LaunchedEffect(showSavedMessage) {
            viewModel.dismissSavedMessage()
        }
    }

    // TravelPlanDetailView
    if (showTravelPlanView) {
        currentPlan?.let { plan ->
            TravelPlanDetailView(
                travelPlan = plan,
                selectedDayNumber = selectedDayNumber,
                onDaySelected = { dayNumber -> viewModel.setSelectedDayNumber(dayNumber) },
                onActivitySelected = { activity -> /* TODO: Implement activity selection */ },
                onNavigateBack = { viewModel.setShowTravelPlanView(false) }
            )
        }
    }
}

@Composable
private fun TravelPlanCard(
    plan: TravelPlan,
    onPlanClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        onClick = onPlanClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.location,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${plan.startDate} - ${plan.endDate}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete plan"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val totalDays = plan.dailyPlans?.size ?: 0
                val totalActivities = plan.dailyPlans?.sumOf { it.activities.size } ?: 0
                
                AssistChip(
                    onClick = { },
                    label = { Text("$totalDays days") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text("$totalActivities activities") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun PremiumDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Upgrade to Premium",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text("Get unlimited travel plans and more with Premium:")
                Spacer(modifier = Modifier.height(8.dp))
                BulletPoint("Unlimited travel plans")
                BulletPoint("Custom budget levels")
                BulletPoint("Enhanced AI suggestions")
                BulletPoint("Offline access")
            }
        },
        confirmButton = {
            Button(
                onClick = { /* TODO: Navigate to premium view */ }
            ) {
                Text("Upgrade Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(text)
    }
}
