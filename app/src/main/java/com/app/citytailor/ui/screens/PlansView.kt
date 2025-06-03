package com.app.citytailor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.app.citytailor.model.TravelPlan
import com.app.citytailor.user.UserManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.app.citytailor.viewmodel.MainViewModel

@Composable
fun PlansView(viewModel: MainViewModel = viewModel()) {
    var showPremiumView by remember { mutableStateOf(false) }
    var savedPlans by remember { mutableStateOf<List<TravelPlan>>(emptyList()) }
    val context = LocalContext.current
    val userManager = remember { UserManager.getInstance(context) }
    val remainingFreePlans = userManager.getRemainingFreePlans()
    val isPremium = userManager.isPremium()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Show premium upgrade card if user is not premium and has no remaining free plans
        if (!isPremium && remainingFreePlans == 0 && savedPlans.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(                            imageVector = Icons.Default.Star,
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
                        onClick = { showPremiumView = true },
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
                    .padding(bottom = 16.dp),
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
                    TextButton(onClick = { showPremiumView = true }) {
                        Text("Upgrade to Premium")
                    }
                }
            }
        }

        // Travel Plans List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (savedPlans.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Home,
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
                items(savedPlans) { plan ->
                    TravelPlanCard(
                        plan = plan,
                        onPlanClick = { /* TODO: Navigate to plan details */ },
                        onDeleteClick = { /* TODO: Delete plan */ }
                    )
                }
            }
        }
    }

    if (showPremiumView) {
        // TODO: Show PremiumView dialog
        showPremiumView = false
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
