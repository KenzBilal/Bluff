# Category UI Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace emoji icons with Material Symbols vector icons, add a categorized icon picker with search, expanded color palette, delete functionality, and polish the entire category UI.

**Architecture:** Add `iconType` field to Category model/entity for backward compat. Create reusable `CategoryIcon` composable. Build `IconPickerSheet` with categorized grid + search. Build `ColorPickerSection` with 40+ colors + hex input. Update CategoriesScreen, AddEditCategorySheet, and all display components.

**Tech Stack:** Kotlin 2.3.20, Jetpack Compose, Room 2.7.1, Material Symbols (already in material-icons.extended)

## Global Constraints

- Kotlin 2.3.20, Compose BOM 2026.03.01, Room 2.7.1
- Money stored as Long (minor units/paise)
- Entity ↔ Domain ↔ DTO tri-layer mapping
- Manual DI via AppContainer (no Hilt)
- Build requires: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk`
- Backward compatible: existing emoji categories must still work

---

## Phase 1: Data Layer

### Task 1: Add iconType Field to Category Model + Entity

**Files:**
- Modify: `domain/model/Category.kt`
- Modify: `data/local/entity/CategoryEntity.kt`

**Interfaces:**
- Produces: `Category.iconType` field, `CategoryEntity.iconType` column

- [ ] **Step 1: Add iconType to Category domain model**

```kotlin
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    val parentId: String? = null,
    val quickAmounts: List<Long> = emptyList(),
    val children: List<Category> = emptyList(),
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val iconType: String = "emoji", // "emoji" or "material"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Add iconType to CategoryEntity**

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String? = null,
    val quickAmounts: String = "[]",
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val iconType: String = "emoji",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 3: Update toModel() and fromModel() to include iconType**

In `toModel()` add: `iconType = iconType,`
In `fromModel()` add: `iconType = model.iconType,`

- [ ] **Step 4: Add DB migration v6→v7**

In `BluffDatabase.kt`:
```kotlin
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN iconType TEXT NOT NULL DEFAULT 'emoji'")
    }
}
```

Register migration and bump version to 7.

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add iconType field to Category model and entity"
```

---

### Task 2: Add DeleteCategoryUseCase + Repository Method

**Files:**
- Create: `domain/usecase/category/DeleteCategoryUseCase.kt`
- Modify: `data/repository/CategoryRepository.kt`
- Modify: `data/local/dao/CategoryDao.kt`
- Modify: `di/AppContainer.kt`

**Interfaces:**
- Consumes: `CategoryDao.deleteCategory(id)` (already exists)
- Produces: `DeleteCategoryUseCase`

- [ ] **Step 1: Add deleteCategory to repository interface**

```kotlin
interface CategoryRepository {
    // ... existing methods
    suspend fun deleteCategory(id: String): Result<Unit>
}
```

- [ ] **Step 2: Implement deleteCategory in repository**

```kotlin
override suspend fun deleteCategory(id: String): Result<Unit> {
    return try {
        db.categoryDao().deleteCategory(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

- [ ] **Step 3: Create DeleteCategoryUseCase**

```kotlin
package com.example.bluff.domain.usecase.category

import com.example.bluff.domain.repository.CategoryRepository

class DeleteCategoryUseCase(private val repository: CategoryRepository) {
    suspend operator fun invoke(categoryId: String): Result<Unit> {
        return repository.deleteCategory(categoryId)
    }
}
```

- [ ] **Step 4: Wire in AppContainer**

```kotlin
val deleteCategoryUseCase = DeleteCategoryUseCase(categoryRepository)
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add DeleteCategoryUseCase and repository method"
```

---

## Phase 2: Reusable Components

### Task 3: Create CategoryIcon Composable

**Files:**
- Create: `ui/categories/CategoryIcon.kt`

**Interfaces:**
- Consumes: `Category.icon`, `Category.iconType`, `Category.color`
- Produces: `CategoryIcon` composable

- [ ] **Step 1: Create CategoryIcon composable**

```kotlin
package com.example.bluff.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

private val materialIconMap: Map<String, ImageVector> = mapOf(
    // Money
    "payments" to Icons.Rounded.Payments,
    "account_balance" to Icons.Rounded.AccountBalance,
    "savings" to Icons.Rounded.Savings,
    "credit_card" to Icons.Rounded.CreditCard,
    "money" to Icons.Rounded.Payments,
    "attach_money" to Icons.Rounded.AttachMoney,
    "monetization_on" to Icons.Rounded.MonetizationOn,
    "receipt" to Icons.Rounded.Receipt,
    "wallet" to Icons.Rounded.AccountBalanceWallet,
    "paid" to Icons.Rounded.Paid,
    "trending_up" to Icons.Rounded.TrendingUp,
    "show_chart" to Icons.Rounded.ShowChart,
    "currency_exchange" to Icons.Rounded.CurrencyExchange,
    "request_quote" to Icons.Rounded.RequestQuote,
    // Transport
    "directions_car" to Icons.Rounded.DirectionsCar,
    "directions_bus" to Icons.Rounded.DirectionsBus,
    "flight" to Icons.Rounded.Flight,
    "train" to Icons.Rounded.Train,
    "local_taxi" to Icons.Rounded.LocalTaxi,
    "bike_scooter" to Icons.Rounded.BikeScooter,
    "directions_boat" to Icons.Rounded.DirectionsBoat,
    "local_shipping" to Icons.Rounded.LocalShipping,
    "gas_station" to Icons.Rounded.LocalGasStation,
    "ev_station" to Icons.Rounded.EvStation,
    "parking" to Icons.Rounded.Parking,
    "traffic" to Icons.Rounded.Traffic,
    "route" to Icons.Rounded.Route,
    "map" to Icons.Rounded.Map,
    // Food
    "restaurant" to Icons.Rounded.Restaurant,
    "local_cafe" to Icons.Rounded.LocalCafe,
    "local_bar" to Icons.Rounded.LocalBar,
    "bakery_dining" to Icons.Rounded.BakeryDining,
    "lunch_dining" to Icons.Rounded.LunchDining,
    "dinner_dining" to Icons.Rounded.DinnerDining,
    "icecream" to Icons.Rounded.Icecream,
    "local_pizza" to Icons.Rounded.LocalPizza,
    "ramen_dining" to Icons.Rounded.RamenDining,
    "liquor" to Icons.Rounded.Liquor,
    "coffee" to Icons.Rounded.Coffee,
    "egg_alt" to Icons.Rounded.EggAlt,
    "kebab_dining" to Icons.Rounded.KebabDining,
    "brunch_dining" to Icons.Rounded.BrunchDining,
    // Home
    "home" to Icons.Rounded.Home,
    "apartment" to Icons.Rounded.Apartment,
    "villa" to Icons.Rounded.Villa,
    "cottage" to Icons.Rounded.Cottage,
    "roofing" to Icons.Rounded.Roofing,
    "plumbing" to Icons.Rounded.Plumbing,
    "electrical_services" to Icons.Rounded.ElectricalServices,
    "hardware" to Icons.Rounded.Hardware,
    "cleaning_services" to Icons.Rounded.CleaningServices,
    "laundry" to Icons.Rounded.LocalLaundryService,
    "dry_cleaning" to Icons.Rounded.DryCleaning,
    "pest_control" to Icons.Rounded.PestControl,
    "yard" to Icons.Rounded.Yard,
    // Health
    "local_hospital" to Icons.Rounded.LocalHospital,
    "medical_services" to Icons.Rounded.MedicalServices,
    "medication" to Icons.Rounded.Medication,
    "vaccines" to Icons.Rounded.Vaccines,
    "health_and_safety" to Icons.Rounded.HealthAndSafety,
    "fitness_center" to Icons.Rounded.FitnessCenter,
    "spa" to Icons.Rounded.Spa,
    "psychology" to Icons.Rounded.Psychology,
    "visibility" to Icons.Rounded.Visibility,
    "bloodtype" to Icons.Rounded.Bloodtype,
    "monitor_heart" to Icons.Rounded.MonitorHeart,
    "healing" to Icons.Rounded.Healing,
    "self_improvement" to Icons.Rounded.SelfImprovement,
    // Leisure
    "sports_esports" to Icons.Rounded.SportsEsports,
    "movie" to Icons.Rounded.Movie,
    "music_note" to Icons.Rounded.MusicNote,
    "theaters" to Icons.Rounded.Theaters,
    "sports_soccer" to Icons.Rounded.SportsSoccer,
    "pool" to Icons.Rounded.Pool,
    "hiking" to Icons.Rounded.Hiking,
    "camping" to Icons.Rounded.Camping,
    "skateboarding" to Icons.Rounded.Skateboarding,
    "surfing" to Icons.Rounded.Surfing,
    "sports_tennis" to Icons.Rounded.SportsTennis,
    "sports_basketball" to Icons.Rounded.SportsBasketball,
    "emoji_events" to Icons.Rounded.EmojiEvents,
    "celebration" to Icons.Rounded.Celebration,
    // Shopping
    "shopping_cart" to Icons.Rounded.ShoppingCart,
    "shopping_bag" to Icons.Rounded.ShoppingBag,
    "store" to Icons.Rounded.Store,
    "local_mall" to Icons.Rounded.LocalMall,
    "checkroom" to Icons.Rounded.Checkroom,
    "watch" to Icons.Rounded.Watch,
    "photo_camera" to Icons.Rounded.PhotoCamera,
    "devices" to Icons.Rounded.Devices,
    "phone_iphone" to Icons.Rounded.PhoneIphone,
    "laptop" to Icons.Rounded.Laptop,
    "headphones" to Icons.Rounded.Headphones,
    "toys" to Icons.Rounded.Toys,
    // Other
    "school" to Icons.Rounded.School,
    "flight_takeoff" to Icons.Rounded.FlightTakeoff,
    "beach_access" to Icons.Rounded.BeachAccess,
    "pets" to Icons.Rounded.Pets,
    "child_care" to Icons.Rounded.ChildCare,
    "elderly" to Icons.Rounded.Elderly,
    "groups" to Icons.Rounded.Groups,
    "volunteer_activism" to Icons.Rounded.VolunteerActivism
)

@Composable
fun CategoryIcon(
    icon: String,
    iconType: String,
    color: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    backgroundSize: Dp = 48.dp,
    fontSize: TextUnit = 24.sp
) {
    val parsedColor = try {
        Color(color.toColorInt())
    } catch (e: Exception) {
        Color(0xFF888888)
    }

    Box(
        modifier = modifier
            .size(backgroundSize)
            .clip(CircleShape)
            .background(parsedColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (iconType == "material") {
            val vector = materialIconMap[icon]
            if (vector != null) {
                Icon(
                    imageVector = vector,
                    contentDescription = null,
                    tint = parsedColor,
                    modifier = Modifier.size(iconSize)
                )
            } else {
                Text(icon.take(2), fontSize = fontSize)
            }
        } else {
            Text(icon, fontSize = fontSize)
        }
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add CategoryIcon composable with Material Symbols + emoji fallback"
```

---

### Task 4: Create IconPickerSheet

**Files:**
- Create: `ui/categories/IconPickerSheet.kt`

**Interfaces:**
- Produces: `IconPickerSheet` composable, `IconCategory` enum

- [ ] **Step 1: Create IconPickerSheet**

```kotlin
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

private val allMaterialIcons: List<String> = IconCategory.entries
    .filter { it != ALL }
    .flatMap { it.icons }
    .distinct()

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
                            .background(if isSelected) Primary.copy(alpha = 0.2f) else Color.Transparent)
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
```

- [ ] **Step 2: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add IconPickerSheet with categorized grid and search"
```

---

### Task 5: Create ColorPickerSection

**Files:**
- Create: `ui/categories/ColorPickerSection.kt`

**Interfaces:**
- Produces: `ColorPickerSection` composable

- [ ] **Step 1: Create ColorPickerSection**

```kotlin
package com.example.bluff.ui.categories

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.bluff.theme.*

val categoryColors = listOf(
    // Reds
    "#E53935", "#D32F2F", "#C62828", "#FF5252",
    // Oranges
    "#FF9800", "#F57C00", "#EF6C00", "#FFB74D",
    // Yellows
    "#FDD835", "#FBC02D", "#F9A825", "#FFEE58",
    // Greens
    "#4CAF50", "#388E3C", "#2E7D32", "#66BB6A",
    // Teals
    "#009688", "#00796B", "#004D40", "#26A69A",
    // Blues
    "#2196F3", "#1976D2", "#1565C0", "#42A5F5",
    // Indigos
    "#3F51B5", "#303F9F", "#1A237E", "#5C6BC0",
    // Purples
    "#9C27B0", "#7B1FA2", "#6A1B9A", "#AB47BC",
    // Pinks
    "#E91E63", "#C2185B", "#AD1457", "#EC407A",
    // Grays
    "#9E9E9E", "#757575", "#616161", "#424242", "#212121"
)

@Composable
fun ColorPickerSection(
    selectedColor: String,
    onSelect: (String) -> Unit
) {
    var showHexInput by remember { mutableStateOf(false) }
    var hexValue by remember { mutableStateOf(selectedColor.removePrefix("#")) }

    Column {
        Text("Color", color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        // Color grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // First row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                categoryColors.take(20).chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { hex ->
                            val parsedColor = try {
                                Color(hex.toColorInt())
                            } catch (e: Exception) {
                                Primary
                            }
                            Surface(
                                shape = CircleShape,
                                color = parsedColor,
                                modifier = Modifier
                                    .size(32.dp)
                                    .then(
                                        if (selectedColor == hex) Modifier.border(3.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { onSelect(hex) }
                            ) {}
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Custom hex input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview circle
            val previewColor = try {
                Color("#$hexValue".toColorInt())
            } catch (e: Exception) {
                Primary
            }
            Surface(
                shape = CircleShape,
                color = previewColor,
                modifier = Modifier.size(32.dp)
            ) {}

            OutlinedTextField(
                value = hexValue,
                onValueChange = { newHex ->
                    hexValue = newHex
                    if (newHex.length == 6) {
                        onSelect("#$newHex")
                    }
                },
                placeholder = { Text("FF5722", color = TextSecondary) },
                prefix = { Text("#", color = TextSecondary) },
                modifier = Modifier.weight(1f),
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
        }
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add ColorPickerSection with 40+ colors and hex input"
```

---

## Phase 3: UI Overhaul

### Task 6: Update CategoriesScreen with Polish + Delete

**Files:**
- Modify: `ui/categories/CategoriesScreen.kt`
- Modify: `ui/categories/CategoriesViewModel.kt`

**Interfaces:**
- Consumes: `DeleteCategoryUseCase` from Task 2, `CategoryIcon` from Task 3

- [ ] **Step 1: Add deleteCategory to CategoriesViewModel**

Add to ViewModel:
```kotlin
fun deleteCategory(categoryId: String) {
    viewModelScope.launch {
        deleteCategoryUseCase(categoryId)
        loadCategoryTree()
    }
}
```

Update Factory to include `deleteCategoryUseCase`.

- [ ] **Step 2: Update CategoriesScreen UI**

Replace emoji `Text(category.icon)` with `CategoryIcon(category.icon, category.iconType, category.color)`.

Add long-press detection with `combinedClickable` and delete confirmation dialog.

Add polished empty state with icon and button.

- [ ] **Step 3: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: polish CategoriesScreen with vector icons and delete"
```

---

### Task 7: Update AddEditCategorySheet with New Pickers

**Files:**
- Modify: `ui/categories/AddEditCategorySheet.kt`

**Interfaces:**
- Consumes: `IconPickerSheet` from Task 4, `ColorPickerSection` from Task 5, `CategoryIcon` from Task 3

- [ ] **Step 1: Replace emoji grid with IconPickerSheet trigger**

Replace the 8-column emoji grid with a tappable icon preview that opens `IconPickerSheet`.

- [ ] **Step 2: Replace color FlowRow with ColorPickerSection**

Replace the 20-color FlowRow with the new `ColorPickerSection`.

- [ ] **Step 3: Add delete button (edit mode only)**

Add a red "Delete" button at the bottom when editing an existing category.

- [ ] **Step 4: Add iconType tracking**

Track `iconType` state. When user selects from IconPickerSheet, set `iconType = "material"`. Default for new categories: `"material"`.

- [ ] **Step 5: Update parent dropdown to use CategoryIcon**

Replace `Text("${rootCat.icon} ${rootCat.name}")` with `CategoryIcon` + name.

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: update AddEditCategorySheet with new icon/color pickers"
```

---

### Task 8: Update Display Components + Seed Data

**Files:**
- Modify: `ui/components/TransactionRow.kt`
- Modify: `ui/components/CategoryTreePicker.kt`
- Modify: `ui/components/CategoryPickerGrid.kt`
- Modify: `data/seed/DefaultDataSeeder.kt`

**Interfaces:**
- Consumes: `CategoryIcon` from Task 3

- [ ] **Step 1: Update TransactionRow to use CategoryIcon**

Replace `Text(text = transaction.categoryIcon ?: "💸", ...)` with `CategoryIcon(...)`.

- [ ] **Step 2: Update CategoryTreePicker to use CategoryIcon**

Replace emoji text with `CategoryIcon(...)`.

- [ ] **Step 3: Update CategoryPickerGrid to use CategoryIcon**

Replace emoji text with `CategoryIcon(...)`.

- [ ] **Step 4: Update DefaultDataSeeder to use Material Symbols**

Replace all emoji icons in seed data with Material Symbol names and set `iconType = "material"`.

Mapping:
- "🍽️" → "restaurant", "🍛" → "lunch_dining", "🌅" → "wb_sunny", "🌙" → "dark_mode"
- "☕" → "coffee", "🧃" → "local_cafe", "🍿" → "popcorn"
- "🚌" → "directions_bus", "🛺" → "local_taxi", "🚕" → "local_taxi"
- "🛍️" → "shopping_bag", "🛒" → "shopping_cart", "👕" → "checkroom"
- "📚" → "school", "📖" → "menu_book", "✏️" → "edit"
- "🎮" → "sports_esports", "🎬" → "movie", "🎵" → "music_note"
- "💡" → "lightbulb", "⚡" → "bolt", "🌐" → "language", "📱" → "phone_iphone"
- "🏥" → "local_hospital", "💊" → "medication", "👨‍⚕️" → "medical_services"
- "💰" → "payments", "💻" → "laptop", "📦" → "inventory_2"

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: update display components and seed data with Material Symbols"
```

---

### Task 9: Final Build + Device Test

**Files:**
- Various

- [ ] **Step 1: Run full build**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Install on device**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`

- [ ] **Step 3: Manual test checklist**

- [ ] Categories screen shows Material Symbol icons
- [ ] Tap category → edit sheet opens with icon picker
- [ ] Icon picker → search works, category tabs filter
- [ ] Color picker → 40+ colors shown, hex input works
- [ ] Create new category with Material Symbol icon
- [ ] Edit existing category → can change icon to Material Symbol
- [ ] Long-press delete → confirmation dialog appears
- [ ] System category delete → shows warning
- [ ] Transaction rows show correct icons
- [ ] Add transaction → category picker shows vector icons

- [ ] **Step 4: Push to GitHub**

```bash
git push origin main
```
