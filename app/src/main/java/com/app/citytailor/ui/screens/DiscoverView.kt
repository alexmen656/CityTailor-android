package com.app.citytailor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.app.citytailor.model.CategoryItem
import com.app.citytailor.model.FeaturedItem
import com.app.citytailor.util.LocalizedString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverView() {
    val featuredItems = listOf(
        FeaturedItem(
            title = "Brandenburger Tor",
            description = "Berlins bekanntestes Wahrzeichen",
            iconName = "building_columns",
            color = Color(0xFF2196F3)
        ),
        FeaturedItem(
            title = "Schloss Neuschwanstein",
            description = "Märchenschloss in den Alpen",
            iconName = "house",
            color = Color(0xFF9C27B0)
        ),
        FeaturedItem(
            title = "Hamburger Hafen",
            description = "Größter Seehafen Deutschlands",
            iconName = "ferry",
            color = Color(0xFF009688)
        )
    )

    val categories = listOf(
        CategoryItem(
            titleKey = "landmarks",
            iconName = "building_columns",
            color = Color(0xFF2196F3),
            items = listOf("Brandenburger Tor", "Kölner Dom", "Frauenkirche")
        ),
        CategoryItem(
            titleKey = "food",
            iconName = "restaurant",
            color = Color(0xFFFF9800),
            items = listOf("Currywurst", "Schnitzel", "Bretzel")
        ),
        CategoryItem(
            titleKey = "activities",
            iconName = "hiking",
            color = Color(0xFF4CAF50),
            items = listOf("Wandern", "Radfahren", "Segeln")
        ),
        CategoryItem(
            titleKey = "events",
            iconName = "calendar_today",
            color = Color(0xFFF44336),
            items = listOf("Oktoberfest", "Karneval", "Christkindlmarkt")
        ),
        CategoryItem(
            titleKey = "trending",
            iconName = "local_fire_department",
            color = Color(0xFFE91E63),
            items = listOf("Street Art Tour", "Food Markets", "Rooftop Bars")
        ),
        CategoryItem(
            titleKey = "local_tips",
            iconName = "star",
            color = Color(0xFFFFEB3B),
            items = listOf("Geheime Spots", "Insider Cafés", "Local Markets")
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LocalizedString("discover"),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            // Featured Section
            Text(
                text = LocalizedString("featured"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(featuredItems) { item ->
                    FeaturedCard(item = item)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Categories Section
            Text(
                text = LocalizedString("categories"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp) // Fixed height for grid inside scroll view
            ) {
                items(categories) { category ->
                    CategoryCard(category = category)
                }
            }
        }
    }
}

@Composable
private fun FeaturedCard(item: FeaturedItem) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForName(item.iconName),
                    contentDescription = item.title,
                    modifier = Modifier.size(48.dp),
                    tint = item.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun CategoryCard(category: CategoryItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForName(category.iconName),
                    contentDescription = category.titleKey,
                    modifier = Modifier.size(24.dp),
                    tint = category.color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = LocalizedString(category.titleKey),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${category.items.size} ${LocalizedString("items")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun getIconForName(iconName: String): ImageVector {
    return when (iconName) {
        "building_columns" -> Icons.Default.Home
        "house" -> Icons.Default.Home
        "ferry" -> Icons.Default.Home
        "restaurant" -> Icons.Default.Home
        "hiking" -> Icons.Default.Home
        "calendar_today" -> Icons.Default.Home
        "local_fire_department" -> Icons.Default.Home
        "star" -> Icons.Default.Home
        else -> Icons.Default.Home
    }
}
