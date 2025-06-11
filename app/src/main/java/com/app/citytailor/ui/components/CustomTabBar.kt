package com.app.citytailor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomTabBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(), // Background extends to bottom edge
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Only content respects safe area
                .padding(top = 8.dp, bottom = 4.dp)
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabItem(
                icon = Icons.Default.List,
                label = "Plans",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            TabItem(
                icon = Icons.Default.Search,
                label = "Discover",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
            TabItem(
                icon = Icons.Default.LocationOn,
                label = "Map",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) }
            )
            TabItem(
                icon = Icons.Default.Home,
                label = "Community",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) }
            )
            TabItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) }
            )
        }
    }
}

@Composable
private fun TabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
