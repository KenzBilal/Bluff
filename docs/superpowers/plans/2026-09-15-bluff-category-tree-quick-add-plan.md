# Category Tree + Quick Add Transaction Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add 3-level subcategory support, quick-add transaction flow, and smart suggestions to the BLUFF personal finance app.

**Architecture:** Add `parentId` and `quickAmounts` fields to the existing Category model. Create a hierarchical category picker component. Enhance the add transaction flow with time-based suggestions, recent categories, and monthly spend display. Hook into the existing global FAB for quick-add.

**Tech Stack:** Kotlin, Jetpack Compose (Material 3), Room database with migration v1→v2, Coroutines/Flow, Manual DI via AppContainer.

## Global Constraints

- Keep manual DI (AppContainer) — no Hilt migration
- Money stored as Long (minor units/paise) — never use Double for money
- Follow existing entity ↔ domain ↔ DTO tri-layer mapping
- Dark/OLED theme — use existing color constants from theme/Color.kt (Background, TextPrimary, CardColor, Primary, etc.)
- Package: com.example.bluff
- Min SDK 26, Target SDK 36
- Room database version 1 → 2 with migration (no fallbackToDestructiveMigration)
- No search feature — keep UI clean and professional
- Preserve existing Supabase sync architecture

---

## File Structure

| File | Action | Purpose |
|------|--------|---------|
| `data/local/entity/CategoryEntity.kt` | Modify | Add `parentId`, `quickAmounts` fields |
| `data/local/dao/CategoryDao.kt` | Modify | Add `getByParentId()`, `getChildrenOf()`, `getRecentCategories()`, `getMonthlySpendByCategory()` |
| `data/local/BluffDatabase.kt` | Modify | Bump to version 2, add migration |
| `domain/model/Category.kt` | Modify | Add `parentId: String?`, `quickAmounts: List<Long>` |
| `data/remote/dto/CategoryDto.kt` | Modify | Add `parentId`, `quickAmounts` fields |
| `data/repository/CategoryRepository.kt` | Modify | Add `getChildren()`, `getRecentCategories()`, `getMonthlySpendByCategory()`, update seed with subcategories |
| `domain/usecase/category/GetCategoriesUseCase.kt` | Modify | Add `getCategoryTree()`, `getQuickSuggestions()` |
| `ui/components/CategoryTreePicker.kt` | Create | New hierarchical category picker component |
| `ui/addtransaction/AddTransactionViewModel.kt` | Modify | Add smart suggestions, category tree state |
| `ui/addtransaction/AddTransactionSheet.kt` | Modify | Use new CategoryTreePicker, show quick suggestions |
| `di/AppContainer.kt` | Modify | Wire any new use cases |

---

## Task 1: Database Migration — Add parentId and quickAmounts to Category

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/domain/model/Category.kt:6-18`
- Modify: `android/app/src/main/java/com/example/bluff/data/local/entity/CategoryEntity.kt:9-50`
- Modify: `android/app/src/main/java/com/example/bluff/data/local/BluffDatabase.kt:24-44`
- Modify: `android/app/src/main/java/com/example/bluff/data/remote/dto/CategoryDto.kt`

**Interfaces:**
- Consumes: Nothing (this is the first task)
- Produces: Updated `Category` model with `parentId: String?` and `quickAmounts: List<Long>`, Room migration v1→v2

- [ ] **Step 1: Update Category domain model**

Add two new fields to `Category.kt`:

```kotlin
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    val parentId: String? = null,        // NEW
    val quickAmounts: List<Long> = emptyList(), // NEW: pre-set amounts in minor units
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

- [ ] **Step 2: Update CategoryEntity**

Add `parentId` and `quickAmounts` columns. The `quickAmounts` is stored as a JSON string in Room (use the existing `Converters` class pattern or store as comma-separated string).

```kotlin
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val icon: String,
    val color: String,
    val parentId: String? = null,        // NEW
    val quickAmounts: String = "[]",     // NEW: JSON array string
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

Update `toModel()` and `fromModel()` to handle the new fields. Parse `quickAmounts` JSON string to `List<Long>`.

- [ ] **Step 3: Update CategoryDto**

Add `parentId` and `quickAmounts` to the DTO:

```kotlin
@Serializable
data class CategoryDto(
    val id: String,
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
    val createdAt: Long,
    val updatedAt: Long
)
```

Update `fromEntity()` and `toEntity()` methods.

- [ ] **Step 4: Add Room migration v1→v2**

In `BluffDatabase.kt`, bump version to 2 and add migration:

```kotlin
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE categories ADD COLUMN parentId TEXT")
        db.execSQL("ALTER TABLE categories ADD COLUMN quickAmounts TEXT DEFAULT '[]'")
    }
}
```

Update the database builder to use the migration:

```kotlin
Room.databaseBuilder(context, BluffDatabase::class.java, "bluff_db")
    .addMigrations(MIGRATION_1_2)
    .build()
```

- [ ] **Step 5: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add parentId and quickAmounts to Category model with Room migration v1→v2"
```

---

## Task 2: Update CategoryDao with Tree Queries

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/data/local/dao/CategoryDao.kt`

**Interfaces:**
- Consumes: Updated `CategoryEntity` from Task 1
- Produces: `getByParentId()`, `getChildrenOf()`, `getRecentCategories()`, `getMonthlySpendByCategory()` queries

- [ ] **Step 1: Add new DAO methods**

```kotlin
@Dao
interface CategoryDao {
    // ... existing methods ...

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    fun getByParentId(parentId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    fun getRootCategories(): Flow<List<CategoryEntity>>

    @Query("""
        SELECT DISTINCT c.* FROM categories c
        INNER JOIN transactions t ON t.categoryId = c.id
        ORDER BY t.date DESC
        LIMIT :limit
    """)
    suspend fun getRecentCategories(limit: Int = 10): List<CategoryEntity>

    @Query("""
        SELECT c.id, COALESCE(SUM(t.amount), 0) as total
        FROM categories c
        LEFT JOIN transactions t ON t.categoryId = c.id
            AND t.date >= :startOfMonth
            AND t.date <= :endOfMonth
            AND t.type = 'EXPENSE'
        GROUP BY c.id
    """)
    suspend fun getMonthlySpendByCategory(startOfMonth: Long, endOfMonth: Long): List<CategorySpendResult>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder")
    suspend fun getRootCategoriesList(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder")
    suspend fun getChildrenOf(parentId: String): List<CategoryEntity>
}

data class CategorySpendResult(
    val id: String,
    val total: Long
)
```

- [ ] **Step 2: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: add tree queries to CategoryDao"
```

---

## Task 3: Update CategoryRepository with Tree Methods

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/data/repository/CategoryRepository.kt`

**Interfaces:**
- Consumes: Updated `CategoryDao` from Task 2
- Produces: `getCategoryTree()`, `getRecentCategories()`, `getMonthlySpendByCategory()` methods

- [ ] **Step 1: Add new repository methods**

Update the `CategoryRepository` interface and implementation:

```kotlin
interface CategoryRepository {
    // ... existing methods ...

    suspend fun getCategoryTree(): List<Category>  // root categories with children populated
    suspend fun getRecentCategories(limit: Int = 10): List<Category>
    suspend fun getMonthlySpendByCategory(): Map<String, Long>  // categoryId → amount spent
    suspend fun getChildrenOf(parentId: String): List<Category>
}
```

Implementation: Build tree by fetching root categories, then for each root, fetch its children recursively (max 3 levels). For `getMonthlySpendByCategory`, calculate start/end of current month in millis and query.

- [ ] **Step 2: Update seedDefaultCategoriesIfNeeded()**

Replace the flat seed data with hierarchical categories including quick amounts:

```kotlin
private suspend fun seedDefaultCategoriesIfNeeded() {
    val count = db.categoryDao().getCount()
    if (count > 0) return

    val userId = userIdProvider()

    // Root categories
    val foodId = UUID.randomUUID().toString()
    val transportId = UUID.randomUUID().toString()
    val shoppingId = UUID.randomUUID().toString()
    val collegeId = UUID.randomUUID().toString()
    val entertainmentId = UUID.randomUUID().toString()
    val billsId = UUID.randomUUID().toString()
    val healthId = UUID.randomUUID().toString()
    val subscriptionsId = UUID.randomUUID().toString()

    // Food subcategories
    val mealsId = UUID.randomUUID().toString()
    val drinksId = UUID.randomUUID().toString()
    val snacksId = UUID.randomUUID().toString()

    // Food > Meals subcategories
    val breakfastId = UUID.randomUUID().toString()
    val lunchId = UUID.randomUUID().toString()
    val dinnerId = UUID.randomUUID().toString()

    // Food > Drinks subcategories
    val teaCoffeeId = UUID.randomUUID().toString()
    val juiceId = UUID.randomUUID().toString()

    // Transport subcategories
    val autoId = UUID.randomUUID().toString()
    val busId = UUID.randomUUID().toString()
    val cabId = UUID.randomUUID().toString()

    // Shopping subcategories
    val groceriesId = UUID.randomUUID().toString()
    val clothesId = UUID.randomUUID().toString()

    // College subcategories
    val booksId = UUID.randomUUID().toString()
    val stationeryId = UUID.randomUUID().toString()

    // Entertainment subcategories
    val moviesId = UUID.randomUUID().toString()
    val gamesId = UUID.randomUUID().toString()

    // Bills subcategories
    val electricityId = UUID.randomUUID().toString()
    val internetId = UUID.randomUUID().toString()
    val phoneId = UUID.randomUUID().toString()

    // Health subcategories
    val medicineId = UUID.randomUUID().toString()
    val doctorId = UUID.randomUUID().toString()

    // Subscriptions subcategories
    val netflixId = UUID.randomUUID().toString()
    val spotifyId = UUID.randomUUID().toString()

    val defaults = listOf(
        // Root: Food
        Category(id = foodId, userId = userId, name = "Food", type = CategoryType.EXPENSE, icon = "🍽️", color = "#FF6B6B", isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
        // Food > Meals
        Category(id = mealsId, userId = userId, name = "Meals", type = CategoryType.EXPENSE, icon = "🍛", color = "#FF6B6B", parentId = foodId, isSystem = true),
        // Food > Meals > Breakfast
        Category(id = breakfastId, userId = userId, name = "Breakfast", type = CategoryType.EXPENSE, icon = "🌅", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(3000, 5000)),
        // Food > Meals > Lunch
        Category(id = lunchId, userId = userId, name = "Lunch", type = CategoryType.EXPENSE, icon = "🍛", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(6000, 8000, 10000)),
        // Food > Meals > Dinner
        Category(id = dinnerId, userId = userId, name = "Dinner", type = CategoryType.EXPENSE, icon = "🌙", color = "#FF6B6B", parentId = mealsId, isSystem = true, quickAmounts = listOf(8000, 10000, 12000)),
        // Food > Drinks
        Category(id = drinksId, userId = userId, name = "Drinks", type = CategoryType.EXPENSE, icon = "☕", color = "#FF6B6B", parentId = foodId, isSystem = true),
        // Food > Drinks > Tea/Coffee
        Category(id = teaCoffeeId, userId = userId, name = "Tea/Coffee", type = CategoryType.EXPENSE, icon = "☕", color = "#FF6B6B", parentId = drinksId, isSystem = true, quickAmounts = listOf(2000, 3000, 4000)),
        // Food > Drinks > Juice
        Category(id = juiceId, userId = userId, name = "Juice", type = CategoryType.EXPENSE, icon = "🧃", color = "#FF6B6B", parentId = drinksId, isSystem = true, quickAmounts = listOf(3000, 4000)),
        // Food > Snacks
        Category(id = snacksId, userId = userId, name = "Snacks", type = CategoryType.EXPENSE, icon = "🍿", color = "#FF6B6B", parentId = foodId, isSystem = true, quickAmounts = listOf(1000, 2000, 3000)),

        // Root: Transport
        Category(id = transportId, userId = userId, name = "Transport", type = CategoryType.EXPENSE, icon = "🚌", color = "#4ECDC4", isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
        // Transport > Auto
        Category(id = autoId, userId = userId, name = "Auto", type = CategoryType.EXPENSE, icon = "🛺", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(4000, 6000, 8000)),
        // Transport > Bus
        Category(id = busId, userId = userId, name = "Bus", type = CategoryType.EXPENSE, icon = "🚌", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(2000, 3000)),
        // Transport > Cab
        Category(id = cabId, userId = userId, name = "Cab", type = CategoryType.EXPENSE, icon = "🚕", color = "#4ECDC4", parentId = transportId, isSystem = true, quickAmounts = listOf(8000, 12000, 16000)),

        // Root: Shopping
        Category(id = shoppingId, userId = userId, name = "Shopping", type = CategoryType.EXPENSE, icon = "🛍️", color = "#45B7D1", isSystem = true),
        // Shopping > Groceries
        Category(id = groceriesId, userId = userId, name = "Groceries", type = CategoryType.EXPENSE, icon = "🛒", color = "#45B7D1", parentId = shoppingId, isSystem = true, quickAmounts = listOf(20000, 50000, 100000)),
        // Shopping > Clothes
        Category(id = clothesId, userId = userId, name = "Clothes", type = CategoryType.EXPENSE, icon = "👕", color = "#45B7D1", parentId = shoppingId, isSystem = true, quickAmounts = listOf(50000, 100000, 200000)),

        // Root: College
        Category(id = collegeId, userId = userId, name = "College", type = CategoryType.EXPENSE, icon = "📚", color = "#96CEB4", isSystem = true),
        // College > Books
        Category(id = booksId, userId = userId, name = "Books", type = CategoryType.EXPENSE, icon = "📖", color = "#96CEB4", parentId = collegeId, isSystem = true, quickAmounts = listOf(20000, 50000)),
        // College > Stationery
        Category(id = stationeryId, userId = userId, name = "Stationery", type = CategoryType.EXPENSE, icon = "✏️", color = "#96CEB4", parentId = collegeId, isSystem = true, quickAmounts = listOf(10000, 20000)),

        // Root: Entertainment
        Category(id = entertainmentId, userId = userId, name = "Entertainment", type = CategoryType.EXPENSE, icon = "🎮", color = "#FFEAA7", isSystem = true),
        // Entertainment > Movies
        Category(id = moviesId, userId = userId, name = "Movies", type = CategoryType.EXPENSE, icon = "🎬", color = "#FFEAA7", parentId = entertainmentId, isSystem = true, quickAmounts = listOf(15000, 20000, 30000)),
        // Entertainment > Games
        Category(id = gamesId, userId = userId, name = "Games", type = CategoryType.EXPENSE, icon = "🎮", color = "#FFEAA7", parentId = entertainmentId, isSystem = true, quickAmounts = listOf(10000, 20000)),

        // Root: Bills
        Category(id = billsId, userId = userId, name = "Bills", type = CategoryType.EXPENSE, icon = "💡", color = "#DDA0DD", isSystem = true),
        // Bills > Electricity
        Category(id = electricityId, userId = userId, name = "Electricity", type = CategoryType.EXPENSE, icon = "⚡", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(50000, 100000, 200000)),
        // Bills > Internet
        Category(id = internetId, userId = userId, name = "Internet", type = CategoryType.EXPENSE, icon = "🌐", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(50000, 100000)),
        // Bills > Phone
        Category(id = phoneId, userId = userId, name = "Phone", type = CategoryType.EXPENSE, icon = "📱", color = "#DDA0DD", parentId = billsId, isSystem = true, quickAmounts = listOf(20000, 50000)),

        // Root: Health
        Category(id = healthId, userId = userId, name = "Health", type = CategoryType.EXPENSE, icon = "🏥", color = "#98FB98", isSystem = true),
        // Health > Medicine
        Category(id = medicineId, userId = userId, name = "Medicine", type = CategoryType.EXPENSE, icon = "💊", color = "#98FB98", parentId = healthId, isSystem = true, quickAmounts = listOf(10000, 20000, 50000)),
        // Health > Doctor
        Category(id = doctorId, userId = userId, name = "Doctor", type = CategoryType.EXPENSE, icon = "👨‍⚕️", color = "#98FB98", parentId = healthId, isSystem = true, quickAmounts = listOf(200000, 500000)),

        // Root: Subscriptions
        Category(id = subscriptionsId, userId = userId, name = "Subscriptions", type = CategoryType.EXPENSE, icon = "📱", color = "#F0E68C", isSystem = true),
        // Subscriptions > Netflix
        Category(id = netflixId, userId = userId, name = "Netflix", type = CategoryType.EXPENSE, icon = "🎬", color = "#F0E68C", parentId = subscriptionsId, isSystem = true, quickAmounts = listOf(20000, 65000)),
        // Subscriptions > Spotify
        Category(id = spotifyId, userId = userId, name = "Spotify", type = CategoryType.EXPENSE, icon = "🎵", color = "#F0E68C", parentId = subscriptionsId, isSystem = true, quickAmounts = listOf(10000, 15000)),

        // Income categories (root only)
        Category(id = UUID.randomUUID().toString(), userId = userId, name = "Salary", type = CategoryType.INCOME, icon = "💰", color = "#00C896", isSystem = true),
        Category(id = UUID.randomUUID().toString(), userId = userId, name = "Freelance", type = CategoryType.INCOME, icon = "💻", color = "#3A8EFF", isSystem = true),
        Category(id = UUID.randomUUID().toString(), userId = userId, name = "Other", type = CategoryType.BOTH, icon = "📦", color = "#888888", isSystem = true)
    )
    defaults.forEach { addCategory(it) }
}
```

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add category tree repository methods and seed hierarchical categories"
```

---

## Task 4: Update GetCategoriesUseCase with Tree and Suggestions

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/domain/usecase/category/GetCategoriesUseCase.kt`

**Interfaces:**
- Consumes: Updated `CategoryRepository` from Task 3
- Produces: `getCategoryTree()`, `getQuickSuggestions()`, `getMonthlySpendByCategory()` use cases

- [ ] **Step 1: Add new use case methods**

```kotlin
class GetCategoriesUseCase(private val repository: CategoryRepository) {
    // ... existing methods ...

    suspend fun getCategoryTree(): List<Category> {
        return repository.getCategoryTree()
    }

    suspend fun getQuickSuggestions(): QuickSuggestions {
        val recentCategories = repository.getRecentCategories(5)
        val monthlySpend = repository.getMonthlySpendByCategory()
        val hour = java.time.LocalTime.now().hour

        val timeSuggestedCategories = when (hour) {
            in 6..10 -> listOf("Breakfast", "Tea/Coffee")
            in 11..15 -> listOf("Lunch")
            in 16..20 -> listOf("Dinner", "Snacks")
            else -> listOf("Entertainment")
        }

        // Get leaf categories that match time suggestions
        val allCategories = repository.getCategoryTree()
        val flatCategories = allCategories.flatMap { root ->
            listOf(root) + root.children.flatMap { level1 ->
                listOf(level1) + level1.children
            }
        }

        val timeBased = flatCategories.filter { cat ->
            timeSuggestedCategories.any { name ->
                cat.name.contains(name, ignoreCase = true)
            }
        }.take(3)

        // Combine: time-based first, then recent, deduplicated
        val seen = mutableSetOf<String>()
        val suggestions = mutableListOf<Category>()
        for (cat in timeBased + recentCategories) {
            if (cat.id !in seen && cat.parentId != null) { // only leaf/mid categories
                seen.add(cat.id)
                suggestions.add(cat)
            }
        }

        return QuickSuggestions(
            categories = suggestions.take(6),
            monthlySpend = monthlySpend
        )
    }
}

data class QuickSuggestions(
    val categories: List<Category>,
    val monthlySpend: Map<String, Long>
)
```

- [ ] **Step 2: Wire in AppContainer**

Update `AppContainer.kt` if the use case constructor changed (it should not — same repository dependency).

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add category tree and quick suggestions to GetCategoriesUseCase"
```

---

## Task 5: Create CategoryTreePicker Component

**Files:**
- Create: `android/app/src/main/java/com/example/bluff/ui/components/CategoryTreePicker.kt`

**Interfaces:**
- Consumes: `List<Category>` (root categories with children), `selectedCategoryId`, `onCategorySelected`
- Produces: A composable that shows hierarchical category selection with expand/collapse

- [ ] **Step 1: Create CategoryTreePicker composable**

This component shows:
1. Quick suggestions section at top (if provided)
2. Root categories as expandable rows
3. Subcategories revealed on tap
4. Selected state with checkmark

```kotlin
@Composable
fun CategoryTreePicker(
    categories: List<Category>,           // root categories
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    quickSuggestions: QuickSuggestions? = null,
    monthlySpend: Map<String, Long> = emptyMap(),
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        // Quick suggestions section
        if (quickSuggestions != null && quickSuggestions.categories.isNotEmpty()) {
            BluffSectionHeader(title = "⚡ Quick")
            quickSuggestions.categories.forEach { category ->
                QuickCategoryChip(
                    category = category,
                    monthlySpend = monthlySpend[category.id] ?: 0L,
                    onClick = { onCategorySelected(category) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Root categories with expand/collapse
        BluffSectionHeader(title = "Categories")
        categories.forEach { rootCategory ->
            CategoryExpandableRow(
                category = rootCategory,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
                monthlySpend = monthlySpend
            )
        }
    }
}

@Composable
private fun CategoryExpandableRow(
    category: Category,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    monthlySpend: Map<String, Long>,
    level: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    val hasChildren = category.children.isNotEmpty()
    val indent = (level * 16).dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) {
                        expanded = !expanded
                    } else {
                        onCategorySelected(category)
                    }
                }
                .padding(start = indent, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Text(text = category.icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(12.dp))

            // Category name and spend
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                val spend = monthlySpend[category.id] ?: 0L
                if (spend > 0) {
                    Text(
                        text = "₹${TransactionAmountFormatter.formatToDisplay(spend)} this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            spend < 50000 -> SuccessColor   // < ₹500
                            spend < 200000 -> WarningColor   // ₹500-2000
                            else -> ExpenseColor              // > ₹2000
                        }
                    )
                }
            }

            // Expand/collapse icon
            if (hasChildren) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            // Selection indicator
            if (category.id == selectedCategoryId) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Primary
                )
            }
        }

        // Children
        if (expanded && hasChildren) {
            category.children.forEach { child ->
                CategoryExpandableRow(
                    category = child,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                    monthlySpend = monthlySpend,
                    level = level + 1
                )
            }
        }
    }
}

@Composable
private fun QuickCategoryChip(
    category: Category,
    monthlySpend: Long,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = category.icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                if (monthlySpend > 0) {
                    Text(
                        text = "₹${TransactionAmountFormatter.formatToDisplay(monthlySpend)}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            // Quick amounts
            category.quickAmounts.take(3).forEach { amount ->
                Surface(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { onClick() },
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariant
                ) {
                    Text(
                        text = "₹${TransactionAmountFormatter.formatToDisplay(amount)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 2: Update Category model to include children**

Add a `children` field to the `Category` domain model:

```kotlin
data class Category(
    // ... existing fields ...
    val children: List<Category> = emptyList()  // NEW: subcategories
)
```

Update `CategoryEntity.toModel()` to not populate children (done at repository level).

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add CategoryTreePicker component with expand/collapse and quick suggestions"
```

---

## Task 6: Update AddTransactionViewModel with Smart Suggestions

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/ui/addtransaction/AddTransactionViewModel.kt`

**Interfaces:**
- Consumes: Updated `GetCategoriesUseCase` from Task 4
- Produces: `categoryTree`, `quickSuggestions`, `monthlySpend` state flows

- [ ] **Step 1: Add new state flows**

```kotlin
class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    getAccountsUseCase: GetAccountsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    // ... existing state flows ...

    private val _categoryTree = MutableStateFlow<List<Category>>(emptyList())
    val categoryTree: StateFlow<List<Category>> = _categoryTree.asStateFlow()

    private val _quickSuggestions = MutableStateFlow<QuickSuggestions?>(null)
    val quickSuggestions: StateFlow<QuickSuggestions?> = _quickSuggestions.asStateFlow()

    private val _monthlySpend = MutableStateFlow<Map<String, Long>>(emptyMap())
    val monthlySpend: StateFlow<Map<String, Long>> = _monthlySpend.asStateFlow()

    init {
        viewModelScope.launch {
            // Load category tree
            _categoryTree.value = getCategoriesUseCase.getCategoryTree()

            // Load smart suggestions
            val suggestions = getCategoriesUseCase.getQuickSuggestions()
            _quickSuggestions.value = suggestions
            _monthlySpend.value = suggestions.monthlySpend

            // Also load flat categories for backward compatibility
            getCategoriesUseCase.getExpenseCategories().collect {
                _expenseCategories.value = it
            }
        }
    }

    // ... existing methods ...
}
```

- [ ] **Step 2: Update Factory**

No changes needed — same dependencies.

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: add category tree and smart suggestions to AddTransactionViewModel"
```

---

## Task 7: Update AddTransactionSheet with New Category Picker

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/ui/addtransaction/AddTransactionSheet.kt`

**Interfaces:**
- Consumes: Updated `AddTransactionViewModel` from Task 6, `CategoryTreePicker` from Task 5
- Produces: Enhanced add transaction sheet with hierarchical category picker

- [ ] **Step 1: Replace CategoryPickerGrid with CategoryTreePicker**

Replace the category picker section (lines 160-170) with:

```kotlin
if (type != TransactionType.TRANSFER) {
    Spacer(modifier = Modifier.height(8.dp))
    BluffSectionHeader(title = "Category")
    val categories = if (type == TransactionType.EXPENSE) categoryTree else incomeCategories
    CategoryTreePicker(
        categories = categories,
        selectedCategoryId = selectedCategoryId,
        onCategorySelected = { viewModel.setCategoryId(it.id) },
        quickSuggestions = quickSuggestions,
        monthlySpend = monthlySpend,
        modifier = Modifier.height(300.dp)
    )
}
```

- [ ] **Step 2: Add required state imports**

```kotlin
val categoryTree by viewModel.categoryTree.collectAsState()
val quickSuggestions by viewModel.quickSuggestions.collectAsState()
val monthlySpend by viewModel.monthlySpend.collectAsState()
```

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: integrate CategoryTreePicker into AddTransactionSheet"
```

---

## Task 8: Update CategoriesScreen for Hierarchy Management

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/ui/categories/CategoriesScreen.kt`
- Modify: `android/app/src/main/java/com/example/bluff/ui/categories/AddEditCategorySheet.kt`
- Modify: `android/app/src/main/java/com/example/bluff/ui/categories/CategoriesViewModel.kt`

**Interfaces:**
- Consumes: Updated `CategoryRepository` from Task 3
- Produces: Ability to add subcategories, view category tree in management screen

- [ ] **Step 1: Add parent category selector to AddEditCategorySheet**

Add a dropdown to select parent category when creating/editing a category. Show root categories as parent options.

- [ ] **Step 2: Update CategoriesScreen to show hierarchy**

Display categories in a tree structure with indentation for subcategories.

- [ ] **Step 3: Verify build compiles**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: update CategoriesScreen for hierarchy management"
```

---

## Task 9: Final Build Verification and Testing

**Files:**
- No new files — verify existing changes

**Interfaces:**
- Consumes: All previous tasks
- Produces: Working app with category tree and quick add

- [ ] **Step 1: Full clean build**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew clean assembleDebug`

- [ ] **Step 2: Verify all features work**

Test scenarios:
1. Open app → categories should be hierarchical (Food > Meals > Lunch)
2. Tap FAB → add transaction → category picker shows tree with expand/collapse
3. Quick suggestions appear based on time of day
4. Monthly spend shown on each category
5. Tap quick amount chip → amount auto-fills → save
6. Create new subcategory via Categories management screen
7. Edit existing category → can change parent
8. Room migration works (existing data preserved)

- [ ] **Step 3: Commit final state**

```bash
git add -A
git commit -m "feat: complete category tree + quick add transaction feature"
```

- [ ] **Step 4: Build release APK**

Run: `cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleRelease`

Copy APK: `cp app/build/outputs/apk/release/app-release.apk /home/kenz/Bluff-v2.apk`

---

## Summary

| Task | Description | Est. Time |
|------|-------------|-----------|
| 1 | Database Migration | 15 min |
| 2 | CategoryDao Tree Queries | 15 min |
| 3 | CategoryRepository Tree Methods + Seed | 20 min |
| 4 | GetCategoriesUseCase + Suggestions | 15 min |
| 5 | CategoryTreePicker Component | 25 min |
| 6 | AddTransactionViewModel Updates | 10 min |
| 7 | AddTransactionSheet Integration | 15 min |
| 8 | CategoriesScreen Hierarchy | 15 min |
| 9 | Final Build + Test | 10 min |
| **Total** | | **~2.5 hours** |
