package com.app.citytailor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.citytailor.model.DailyPlan
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DayButtonsView(
    dailyPlans: List<DailyPlan>,
    selectedDayNumber: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
        /*    Text(
                text = "Select Day",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            */ 
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dailyPlans) { dailyPlan ->
                    DayButton(
                        dayPlan = dailyPlan,
                        isSelected = dailyPlan.dayNumber == selectedDayNumber,
                        onClick = { onDaySelected(dailyPlan.dayNumber) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayButton(
    dayPlan: DailyPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonColors = if (isSelected) {
        ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    } else {
        ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
    
    if (isSelected) {
        FilledTonalButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors
        ) {
            DayButtonContent(dayPlan)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = buttonColors
        ) {
            DayButtonContent(dayPlan)
        }
    }
}

@Composable
private fun DayButtonContent(dayPlan: DailyPlan) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Text(
            text = "Day ${dayPlan.dayNumber}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formatDateShort(dayPlan.date),
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

private fun formatDateShort(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateString
    }
}
