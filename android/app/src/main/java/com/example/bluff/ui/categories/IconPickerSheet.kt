package com.example.bluff.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.*

enum class IconCategory(val label: String, val icons: List<String>) {
    ALL("All", allMaterialIcons),
    MONEY("Money", listOf("payments", "account_balance", "savings", "credit_card", "attach_money", "monetization_on", "receipt", "wallet", "paid", "trending_up", "show_chart", "currency_exchange", "request_quote")),
    TRANSPORT("Transport", listOf("directions_car", "directions_bus", "flight", "train", "local_taxi", "bike_scooter", "directions_boat", "local_shipping", "gas_station", "ev_station", "parking", "traffic", "route", "map")),
    FOOD("Food", listOf("restaurant", "local_cafe", "local_bar", "bakery_dining", "lunch_dining", "dinner_dining", "icecream", "local_pizza", "ramen_dining", "liquor", "coffee", "egg_alt", "kebab_dining", "brunch_dining")),
    HOME("Home", listOf("home", "apartment", "villa", "cottage", "roofing", "plumbing", "electrical_services", "hardware", "cleaning_services", "laundry", "dry_cleaning", "pest_control", "yard")),
    HEALTH("Health", listOf("local_hospital", "medical_services", "medication", "vaccines", "health_and_safety", "fitness_center", "spa", "psychology", "visibility", "bloodtype", "monitor_heart", "healing", "self_improvement")),
    LEISURE("Leisure", listOf("sports_esports", "movie", "music_note", "theaters", "sports_soccer", "pool", "hiking", "camping", "skateboarding", "surfing", "sports_tennis", "sports_basketball", "emoji_events", "celebration")),
    SHOPPING("Shopping", listOf("shopping_cart", "shopping_bag", "store", "local_mall", "checkroom", "watch", "photo_camera", "devices", "phone_iphone", "laptop", "headphones", "toys")),
    OTHER("Other", listOf("school", "flight_takeoff", "beach_access", "pets", "child_care", "elderly", "groups", "volunteer_activism"))
}

private val allMaterialIcons: List<String> by lazy {
    IconCategory.entries
        .filter { it != IconCategory.ALL }
        .flatMap { it.icons }
        .distinct()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconPickerSheet(
    selectedIcon: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IconCategory.ALL) }

    val filteredIcons = remember(searchQuery, selectedCategory) {
        val icons = if (selectedCategory == IconCategory.ALL) allMaterialIcons
                    else selectedCategory.icons
        if (searchQuery.isBlank()) icons
        else icons.filter { it.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Choose Icon", color = TextPrimary, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search icons...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardColor,
                    unfocusedContainerColor = CardColor,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = Primary
                )
            )

            Spacer(Modifier.height(12.dp))

            // Category tabs
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconCategory.entries.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Icon grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier.heightIn(max = 400.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredIcons) { iconName ->
                    val isSelected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable {
                                onSelect(iconName)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CategoryIcon(
                            icon = iconName,
                            iconType = "material",
                            color = if (isSelected) "#6C63FF" else "#888888",
                            iconSize = 24.dp,
                            backgroundSize = 48.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
