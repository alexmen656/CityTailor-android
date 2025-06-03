package com.app.citytailor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import android.util.Log
import com.app.citytailor.model.*
import com.app.citytailor.network.TravelPlanService
import com.app.citytailor.user.UserManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionView(
    locationName: String,
    onTravelPlanReceived: (TravelPlan) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(3)) }
    var selectedTravelType by remember { mutableStateOf(TravelType.SOLO) }
    var selectedTransportationType by remember { mutableStateOf(TransportationType.WALKING) }
    var selectedTravelMode by remember { mutableStateOf(TravelMode.MODERATE) }
    var selectedBudgetLevel by remember { mutableStateOf(BudgetLevel.MEDIUM) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSelectingStartDate by remember { mutableStateOf(true) }
    var generationProgress by remember { mutableFloatStateOf(0f) }
    var generationStatus by remember { mutableStateOf("Bereite Generierung vor...") }
    
    val context = LocalContext.current
    val userManager = remember { UserManager.getInstance(context) }
    val isPremium = userManager.isPremium()
    val remainingFreePlans = userManager.getRemainingFreePlans()
    val tripLengthInDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    
    // Helper function to update generation status with simulated delay
    suspend fun updateGenerationStatus(currentStep: Int, totalSteps: Int) {
        val targetProgress = currentStep.toFloat() / totalSteps.toFloat()
        
        // Simulate gradual progress to the target
        val startProgress = generationProgress
        val progressDifference = targetProgress - startProgress
        val stepCount = 20
        
        for (i in 1..stepCount) {
            generationProgress = startProgress + (progressDifference * i / stepCount)
            kotlinx.coroutines.delay(100)
        }
    }

    // Real travel plan generation
    LaunchedEffect(isLoading) {
        if (isLoading) {
            try {
                // Initialize progress
                generationProgress = 0f
                generationStatus = "Bereite Generierung vor..."
                
                val totalSteps = tripLengthInDays + 2 // Preparation + days + finalization
                
                // Preparation step
                updateGenerationStatus(1, totalSteps)
                
                // Days steps
                for (day in 1..tripLengthInDays) {
                    generationStatus = "Erstelle Tagesplan $day von $tripLengthInDays..."
                    updateGenerationStatus(day + 1, totalSteps)
                }
                
                // Finalization step
                generationStatus = "Finalisiere Empfehlungen..."
                updateGenerationStatus(totalSteps, totalSteps)
                
                // Test connection first
                generationStatus = "Teste Verbindung zum Server..."
                val connectionResult = TravelPlanService.shared.testConnection()
                Log.d("DateSelectionView", "Connection test: $connectionResult")
                
                // Actual API call
                generationStatus = "Sende Anfrage an den Server..."
                val travelPlan = TravelPlanService.shared.generateTravelPlan(
                    location = locationName,
                    startDate = startDate,
                    endDate = endDate,
                    travelType = selectedTravelType,
                    transportationType = selectedTransportationType,
                    travelMode = selectedTravelMode,
                    budgetLevel = selectedBudgetLevel,
                    isPremium = isPremium
                )
                
                onTravelPlanReceived(travelPlan)
                onDismiss()
            } catch (e: Exception) {
                // Handle error
                generationStatus = "Fehler: ${e.message}"
                isLoading = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top App Bar
                TopAppBar(
                    title = { 
                        Text(
                            "Reiseplanung",
                            style = MaterialTheme.typography.titleLarge
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Schließen"
                            )
                        }
                    }
                )
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Destination Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Reiseziel",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reiseziel",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                ElevatedCard(
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = locationName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Ausgewählt",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Travel Period Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Reisezeitraum",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Start Date
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isSelectingStartDate = true
                                            showDatePicker = true
                                        },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Anreisedatum",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Anreise")
                                        }
                                        Text(
                                            text = startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // End Date
                                OutlinedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isSelectingStartDate = false
                                            showDatePicker = true
                                        },
                                    colors = CardDefaults.outlinedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DateRange,
                                                contentDescription = "Abreisedatum",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Abreise")
                                        }
                                        Text(
                                            text = endDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Duration
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = "Reisedauer",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Dauer",
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Text(
                                            text = "$tripLengthInDays ${if (tripLengthInDays == 1) "Tag" else "Tage"}",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Travel Type Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Reiseart",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Reiseart",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                TravelTypeSelector(
                                    selectedTravelType = selectedTravelType,
                                    onTravelTypeSelected = { selectedTravelType = it }
                                )
                            }
                        }
                    }
                    
                    // Advanced Settings Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showAdvancedSettings = !showAdvancedSettings }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Erweiterte Einstellungen",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = if (showAdvancedSettings) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (showAdvancedSettings) "Weniger anzeigen" else "Mehr anzeigen",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = showAdvancedSettings,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.padding(top = 16.dp)
                                    ) {
                                        // Transportation Type
                                        Column {
                                            Text(
                                                text = "Transportmittel",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TransportationTypeSelector(
                                                selectedTransportationType = selectedTransportationType,
                                                onTransportationTypeSelected = { selectedTransportationType = it }
                                            )
                                        }
                                        
                                        val isPremium = userManager.isPremium()
                                        
                                        if (isPremium) {
                                            // Travel Mode (Premium only)
                                            Column {
                                                Text(
                                                    text = "Reisemodus",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                TravelModeSelector(
                                                    selectedTravelMode = selectedTravelMode,
                                                    onTravelModeSelected = { selectedTravelMode = it }
                                                )
                                            }
                                            
                                            // Budget Level (Premium only)
                                            Column {
                                                Text(
                                                    text = "Budget",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                BudgetLevelSelector(
                                                    selectedBudgetLevel = selectedBudgetLevel,
                                                    onBudgetLevelSelected = { selectedBudgetLevel = it }
                                                )
                                            }
                                        } else {
                                            // Premium Upgrade Prompt
                                            ElevatedCard(
                                                colors = CardDefaults.elevatedCardColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                                ),
                                                elevation = CardDefaults.elevatedCardElevation(4.dp)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(16.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = "Premium-Features",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "Reisemodus und Budget-Optionen sind in der Premium-Version verfügbar.",
                                                        style = MaterialTheme.typography.bodySmall
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    FilledTonalButton(
                                                        onClick = { /* TODO: Show premium upgrade */ },
                                                        modifier = Modifier.align(Alignment.End)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.Star,
                                                            contentDescription = null
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Upgrade zu Premium")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Generate Button Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                val isPremium = userManager.isPremium()
                                val remainingFreePlans = userManager.getRemainingFreePlans()
                                
                                ElevatedButton(
                                    onClick = {
                                        if (!isPremium && remainingFreePlans <= 0) {
                                            // Show premium upgrade dialog
                                        } else {
                                            isLoading = true
                                            // TODO: Start generation process
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isLoading,
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    if (isLoading) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Text("Generiere Reiseplan...")
                                        }
                                    } else {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Home,
                                                contentDescription = null
                                            )
                                            Text("Plan generieren")
                                        }
                                    }
                                }
                                
                                if (isLoading) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    LinearProgressIndicator(
                                        progress = { generationProgress },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = generationStatus,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    
                    // Free Account Limits (if not premium)
                    if (!isPremium) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    val remainingFreePlans = userManager.getRemainingFreePlans()
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Kostenloser Account",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Verbleibende kostenlose Pläne")
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Text(
                                                text = "$remainingFreePlans von 3",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    FilledTonalButton(
                                        onClick = { /* TODO: Show premium upgrade */ },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Upgrade zu Premium")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (isSelectingStartDate) {
                startDate.toEpochDay() * 24 * 60 * 60 * 1000
            } else {
                endDate.toEpochDay() * 24 * 60 * 60 * 1000
            }
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val localDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            if (isSelectingStartDate) {
                                startDate = localDate
                                if (endDate.isBefore(localDate) || endDate.isEqual(localDate)) {
                                    endDate = localDate.plusDays(1)
                                }
                            } else {
                                if (localDate.isAfter(startDate) || localDate.isEqual(startDate)) {
                                    endDate = localDate
                                }
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            },
            tonalElevation = 8.dp
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = if (isSelectingStartDate) "Anreisedatum wählen" else "Abreisedatum wählen",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                headline = {
                    // Custom headline hidden, using title instead
                }
            )
        }
    }
}

@Composable
private fun TravelTypeSelector(
    selectedTravelType: TravelType?,
    onTravelTypeSelected: (TravelType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(TravelType.values()) { travelType ->
                MaterialSelectionChip(
                    travelType = travelType,
                    isSelected = selectedTravelType == travelType,
                    onClick = { onTravelTypeSelected(travelType) }
                )
            }
        }
    }
}

@Composable
private fun TransportationTypeSelector(
    selectedTransportationType: TransportationType?,
    onTransportationTypeSelected: (TransportationType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(TransportationType.values()) { transportationType ->
                MaterialSelectionChip(
                    transportationType = transportationType,
                    isSelected = selectedTransportationType == transportationType,
                    onClick = { onTransportationTypeSelected(transportationType) }
                )
            }
        }
    }
}

@Composable
private fun TravelModeSelector(
    selectedTravelMode: TravelMode?,
    onTravelModeSelected: (TravelMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(TravelMode.values()) { travelMode ->
                MaterialSelectionChip(
                    travelMode = travelMode,
                    isSelected = selectedTravelMode == travelMode,
                    onClick = { onTravelModeSelected(travelMode) }
                )
            }
        }
    }
}

@Composable
private fun BudgetLevelSelector(
    selectedBudgetLevel: BudgetLevel?,
    onBudgetLevelSelected: (BudgetLevel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(BudgetLevel.values()) { budgetLevel ->
                MaterialSelectionChip(
                    budgetLevel = budgetLevel,
                    isSelected = selectedBudgetLevel == budgetLevel,
                    onClick = { onBudgetLevelSelected(budgetLevel) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MaterialSelectionChip(
    travelType: TravelType? = null,
    transportationType: TransportationType? = null,
    travelMode: TravelMode? = null,
    budgetLevel: BudgetLevel? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (text, icon) = when {
        travelType != null -> TravelType.getLocalizedName(travelType) to travelType.icon
        transportationType != null -> TransportationType.getLocalizedName(transportationType) to transportationType.icon
        travelMode != null -> TravelMode.getLocalizedName(travelMode) to travelMode.icon
        budgetLevel != null -> BudgetLevel.getLocalizedName(budgetLevel) to null
        else -> "Unknown" to Icons.Default.Home
    }

    val textStyle = MaterialTheme.typography.labelMedium
    val fontSize = with(LocalDensity.current) { textStyle.fontSize.toPx() }
    
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = if (budgetLevel != null) {
            {
                Text(
                    text = budgetLevel.symbol,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            {
                Icon(
                    imageVector = icon ?: Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        shape = MaterialTheme.shapes.medium,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            iconColor = MaterialTheme.colorScheme.primary,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.primary,
            selectedTrailingIconColor = MaterialTheme.colorScheme.primary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderWidth = 1.dp,
            selectedBorderWidth = 0.dp
        ),
        modifier = Modifier.height(40.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    tonalElevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = tonalElevation,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                content()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    dismissButton()
                    Spacer(modifier = Modifier.width(8.dp))
                    confirmButton()
                }
            }
        }
    }
}
