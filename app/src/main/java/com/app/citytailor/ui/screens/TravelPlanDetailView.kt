package com.app.citytailor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.citytailor.model.*
import com.app.citytailor.user.UserManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanDetailView(
    travelPlan: TravelPlan,
    selectedDayNumber: Int,
    onDaySelected: (Int) -> Unit,
    onActivitySelected: (Activity) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = travelPlan.location,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "${formatDate(travelPlan.startDate)} - ${formatDate(travelPlan.endDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                // Day selector
                Surface(
                    tonalElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        travelPlan.dailyPlans?.forEach { dailyPlan ->
                            DayButton(
                                dayNumber = dailyPlan.dayNumber,
                                date = formatDateShort(dailyPlan.date),
                                isSelected = selectedDayNumber == dailyPlan.dayNumber,
                                onClick = { onDaySelected(dailyPlan.dayNumber) }
                            )
                        }
                    }
                }
            }

            // Activities for selected day
            val selectedDayPlan = travelPlan.dailyPlans?.find { it.dayNumber == selectedDayNumber }
            if (selectedDayPlan != null) {
                items(selectedDayPlan.activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { onActivitySelected(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayButton(
    dayNumber: Int,
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.width(72.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Day $dayNumber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activity: Activity,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = activity.duration,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                CategoryChip(category = activity.category)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = activity.displayAddress,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(category: String) {
    Surface(
        color = getCategoryColor(category),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

private fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "sightseeing" -> Color(0xFF4CAF50)
        "history" -> Color(0xFFFFA000)
        "art" -> Color(0xFFE91E63)
        "food" -> Color(0xFFf44336)
        "shopping" -> Color(0xFF9C27B0)
        "entertainment" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
}

private fun formatDate(dateStr: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return try {
        val date = LocalDate.parse(dateStr, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateStr
    }
}

private fun formatDateShort(dateStr: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputFormatter = DateTimeFormatter.ofPattern("dd.MM")
    return try {
        val date = LocalDate.parse(dateStr, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateStr
    }
}
