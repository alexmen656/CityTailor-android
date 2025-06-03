package com.app.citytailor.ui.screens

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.citytailor.R
import com.app.citytailor.model.*
import com.app.citytailor.user.UserManager
import com.app.citytailor.util.DateFormatterUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlanView(
    travelPlan: TravelPlan,
    selectedDayNumber: Int,
    onDaySelected: (Int) -> Unit,
    onActivitySelected: (Activity) -> Unit,
    onDismiss: () -> Unit
) {
    var showPremiumView by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    var webViewUrl by remember { mutableStateOf<String?>(null) }
    var showExportOptions by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val userManager = remember { UserManager.getInstance(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with location and dates
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = travelPlan.location,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${formatDateString(travelPlan.startDate)} - ${formatDateString(travelPlan.endDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Day selector
            if (travelPlan.dailyPlans != null && travelPlan.dailyPlans.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        travelPlan.dailyPlans.forEach { dailyPlan ->
                            DayButton(
                                dayNumber = dailyPlan.dayNumber,
                                date = formatDateShort(dailyPlan.date),
                                isSelected = selectedDayNumber == dailyPlan.dayNumber,
                                onClick = { onDaySelected(dailyPlan.dayNumber) }
                            )
                        }
                    }
                }

                // Activities for selected day
                val selectedDayPlan = travelPlan.dailyPlans.find { it.dayNumber == selectedDayNumber }
                if (selectedDayPlan != null) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedDayPlan.activities.forEach { activity ->
                            ActivityItem(
                                activity = activity,
                                onActivityClick = { onActivitySelected(activity) },
                                onNavigateClick = { selectedActivity = activity },
                                onTicketsClick = {
                                    if (isTicketable(activity)) {
                                        webViewUrl = getTicketUrl(activity)
                                        showWebView = true
                                    }
                                }
                            )
                        }
                    }
                }

                // Recommendations section if available
                travelPlan.recommendations?.let { recommendations ->
                    if (recommendations.food.isNotEmpty() || 
                        recommendations.transport.isNotEmpty() || 
                        recommendations.tips.isNotEmpty()) {
                        
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.recommendations),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            
                            Card {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (recommendations.food.isNotEmpty()) {
                                        ExpandableRecommendationSection(
                                            title = stringResource(R.string.food_recommendations),
                                            items = recommendations.food,
                                            icon = Icons.Default.Home
                                        )
                                    }
                                    
                                    if (recommendations.transport.isNotEmpty()) {
                                        ExpandableRecommendationSection(
                                            title = stringResource(R.string.transport_recommendations),
                                            items = recommendations.transport,
                                            icon = Icons.Default.Home
                                        )
                                    }
                                    
                                    if (recommendations.tips.isNotEmpty()) {
                                        ExpandableRecommendationSection(
                                            title = stringResource(R.string.local_tips),
                                            items = recommendations.tips,
                                            icon = Icons.Default.Home
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Export options
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            if (userManager.isPremium()) {
                                showExportOptions = true
                            } else {
                                showPremiumView = true
                            }
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export PDF")
                    }
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Bottom padding for modal sheet
        }
    }

    // Premium upgrade dialog
    if (showPremiumView) {
        PremiumView(
            onUpgrade = { showPremiumView = false },
            onRestore = { showPremiumView = false },
            onDismiss = { showPremiumView = false }
        )
    }

    // Webview for tickets
    if (showWebView && webViewUrl != null) {
        WebViewScreen(
            url = webViewUrl!!,
            onClose = { showWebView = false }
        )
    }

    // Export options dropdown
    if (showExportOptions) {
        DropdownMenu(
            expanded = showExportOptions,
            onDismissRequest = { showExportOptions = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.download_pdf)) },
                onClick = {
                    showExportOptions = false
                    // TODO: Implement PDF export
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null
                    )
                }
            )
            
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share_pdf)) },
                onClick = {
                    showExportOptions = false
                    // TODO: Implement PDF sharing
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun ExpandableRecommendationSection(
    title: String,
    items: List<String>,
    icon: ImageVector
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (!expanded) {
            Divider(
                modifier = Modifier.padding(top = 8.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
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
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityItem(
    activity: Activity,
    onActivityClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onTicketsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onActivityClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.duration,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = getCategoryColor(activity.category)
                ) {
                    Text(
                        text = activity.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = activity.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = activity.location,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isTicketable(activity)) {
                    Button(
                        onClick = onTicketsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Tickets")
                        }
                    }
                }
                
                FilledTonalButton(
                    onClick = onNavigateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Navigate")
                    }
                }
            }
        }
    }
}

// Helper functions
private fun formatDateString(dateString: String): String {
    return DateFormatterUtils.formatFullDate(dateString)
}

private fun formatDateShort(dateString: String): String {
    return DateFormatterUtils.formatShortDate(dateString)
}

private fun getCategoryColor(category: String): Color {
    val lowercasedCategory = category.lowercase()
    
    return when {
        lowercasedCategory.contains("art") || 
        lowercasedCategory.contains("museum") -> Color(0xFF9C27B0) // Purple
        
        lowercasedCategory.contains("food") || 
        lowercasedCategory.contains("restaurant") -> Color(0xFFF44336) // Red
        
        lowercasedCategory.contains("shopping") -> Color(0xFFE91E63) // Pink
        
        lowercasedCategory.contains("history") || 
        lowercasedCategory.contains("monument") -> Color(0xFF795548) // Brown
        
        lowercasedCategory.contains("nature") ||
        lowercasedCategory.contains("park") -> Color(0xFF4CAF50) // Green
        
        lowercasedCategory.contains("entertainment") ||
        lowercasedCategory.contains("nightlife") -> Color(0xFF9C27B0) // Purple
        
        lowercasedCategory.contains("architecture") ||
        lowercasedCategory.contains("landmark") -> Color(0xFF2196F3) // Blue
        
        lowercasedCategory.contains("sightseeing") -> Color(0xFF4CAF50) // Green
        
        else -> Color(0xFF607D8B) // Blue Grey
    }
}

private fun isTicketable(activity: Activity): Boolean {
    val ticketableCategories = listOf(
        "museum", "musée", "museo",
        "attraction", "attraktion", "atracción", 
        "tour", "führung",
        "exhibit", "ausstellung", "exposition",
        "art", "kunst", "arte",
        "theater", "théâtre", "teatro",
        "concert", "konzert", "concierto",
        "show", "aufführung", "spectacle",
        "park", "parque", 
        "garden", "garten", "jardin",
        "palace", "palast", "palacio",
        "castle", "schloss", "château",
        "monument", "denkmal"
    )
    
    val lowercasedTitle = activity.title.lowercase()
    val lowercasedCategory = activity.category.lowercase()
    
    return ticketableCategories.any { keyword ->
        lowercasedCategory.contains(keyword) || lowercasedTitle.contains(keyword)
    }
}

private fun getTicketUrl(activity: Activity): String {
    val baseUrl = "https://www.getyourguide.com"
    val locationComponents = activity.location.split(",")
    val locationName = if (locationComponents.size > 1) 
        locationComponents.last().trim() 
    else 
        activity.title
    
    val searchQuery = buildString {
        append(locationName)
        
        val significantWords = activity.title.split(" ").filter { word ->
            val lowercasedWord = word.lowercase()
            word.length > 3 && !listOf("the", "and", "oder", "des", "los", "las", "les", "il", "lo", "la").contains(lowercasedWord)
        }
        
        if (significantWords.isNotEmpty()) {
            append(" ")
            append(significantWords.joinToString(" "))
        }
    }
    
    val encodedQuery = java.net.URLEncoder.encode(searchQuery, "UTF-8")
    return "${baseUrl}/s/?q=${encodedQuery}&partner_id=EAULFPM&cmp=share_to_earn"
}
