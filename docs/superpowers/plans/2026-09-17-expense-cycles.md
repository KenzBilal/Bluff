# Smart Expense Cycle Tracking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add expense cycle tracking with smart reminders, duplicate prevention, and clean UI.

**Architecture:** New Room tables for cycles + category defaults, WorkManager worker for daily reminders, new Cycles screen in More, "Upcoming" card on Home, post-transaction cycle setup sheet.

**Tech Stack:** Kotlin 2.3.20, Jetpack Compose, Room 2.7.1, WorkManager, Navigation 3

## Global Constraints

- Kotlin 2.3.20, Compose BOM 2026.03.01, Room 2.7.1
- Money stored as Long (minor units/paise)
- Entity ↔ Domain ↔ DTO tri-layer mapping
- Manual DI via AppContainer (no Hilt)
- Build requires: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk`
- One cycle per category (unique constraint)
- UI must be clean and minimal, not cluttered

---

## Phase 1: Data Layer

### Task 1: Add ExpenseCycle Entity + DAO

**Files:**
- Create: `app/src/main/java/com/example/bluff/data/local/entity/ExpenseCycleEntity.kt`
- Create: `app/src/main/java/com/example/bluff/data/local/entity/CategoryCycleDefaultEntity.kt`
- Create: `app/src/main/java/com/example/bluff/data/local/dao/ExpenseCycleDao.kt`
- Create: `app/src/main/java/com/example/bluff/data/local/dao/CategoryCycleDefaultDao.kt`
- Modify: `app/src/main/java/com/example/bluff/data/local/BluffDatabase.kt`

**Interfaces:**
- Produces: `ExpenseCycleDao`, `CategoryCycleDefaultDao`, `ExpenseCycleEntity`, `CategoryCycleDefaultEntity`

- [ ] **Step 1: Create ExpenseCycleEntity**

```kotlin
package com.example.bluff.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_cycles",
    indices = [Index(value = ["categoryId"], unique = true)]
)
data class ExpenseCycleEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    @ColumnInfo(name = "amountMinor") val amountMinor: Long,
    @ColumnInfo(name = "cycleDays") val cycleDays: Int,
    @ColumnInfo(name = "lastTransactionDate") val lastTransactionDate: String,
    @ColumnInfo(name = "nextDueDate") val nextDueDate: String,
    @ColumnInfo(name = "isActive") val isActive: Boolean = true,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long
)
```

- [ ] **Step 2: Create CategoryCycleDefaultEntity**

```kotlin
package com.example.bluff.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_cycle_defaults")
data class CategoryCycleDefaultEntity(
    @PrimaryKey val categoryId: String,
    @ColumnInfo(name = "categoryName") val categoryName: String,
    @ColumnInfo(name = "defaultCycleDays") val defaultCycleDays: Int
)
```

- [ ] **Step 3: Create ExpenseCycleDao**

```kotlin
package com.example.bluff.data.local.dao

import androidx.room.*
import com.example.bluff.data.local.entity.ExpenseCycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCycleDao {
    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getAllActive(): Flow<List<ExpenseCycleEntity>>

    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 AND nextDueDate <= :date ORDER BY nextDueDate ASC")
    fun getDueSoon(date: String): Flow<List<ExpenseCycleEntity>>

    @Query("SELECT * FROM expense_cycles WHERE categoryId = :categoryId AND isActive = 1")
    suspend fun getByCategoryId(categoryId: String): ExpenseCycleEntity?

    @Query("SELECT * FROM expense_cycles WHERE isActive = 1 AND nextDueDate <= :date")
    suspend fun getDueForReminder(date: String): List<ExpenseCycleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: ExpenseCycleEntity)

    @Update
    suspend fun update(cycle: ExpenseCycleEntity)

    @Query("UPDATE expense_cycles SET isActive = 0, updatedAt = :now WHERE id = :id")
    suspend fun deactivate(id: String, now: Long = System.currentTimeMillis())
}
```

- [ ] **Step 4: Create CategoryCycleDefaultDao**

```kotlin
package com.example.bluff.data.local.dao

import androidx.room.*
import com.example.bluff.data.local.entity.CategoryCycleDefaultEntity

@Dao
interface CategoryCycleDefaultDao {
    @Query("SELECT * FROM category_cycle_defaults")
    suspend fun getAll(): List<CategoryCycleDefaultEntity>

    @Query("SELECT * FROM category_cycle_defaults WHERE categoryId = :categoryId")
    suspend fun getByCategoryId(categoryId: String): CategoryCycleDefaultEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(defaults: List<CategoryCycleDefaultEntity>)
}
```

- [ ] **Step 5: Add tables and migration to BluffDatabase**

Add abstract functions:
```kotlin
abstract fun expenseCycleDao(): ExpenseCycleDao
abstract fun categoryCycleDefaultDao(): CategoryCycleDefaultDao
```

Add migration:
```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE expense_cycles (
                id TEXT PRIMARY KEY NOT NULL,
                categoryId TEXT NOT NULL,
                name TEXT NOT NULL,
                amountMinor INTEGER NOT NULL DEFAULT 0,
                cycleDays INTEGER NOT NULL,
                lastTransactionDate TEXT NOT NULL,
                nextDueDate TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE UNIQUE INDEX idx_expense_cycles_category ON expense_cycles(categoryId) WHERE isActive = 1")
        db.execSQL("""
            CREATE TABLE category_cycle_defaults (
                categoryId TEXT PRIMARY KEY NOT NULL,
                categoryName TEXT NOT NULL,
                defaultCycleDays INTEGER NOT NULL
            )
        """)
    }
}
```

Register migration and bump version to 6.

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add ExpenseCycle and CategoryCycleDefault entities with DAOs"
```

---

### Task 2: Add ExpenseCycle Domain Model + Repository

**Files:**
- Create: `app/src/main/java/com/example/bluff/domain/model/ExpenseCycle.kt`
- Create: `app/src/main/java/com/example/bluff/data/repository/ExpenseCycleRepositoryImpl.kt`
- Create: `app/src/main/java/com/example/bluff/domain/repository/ExpenseCycleRepository.kt`
- Modify: `app/src/main/java/com/example/bluff/di/AppContainer.kt`

**Interfaces:**
- Consumes: `ExpenseCycleDao`, `CategoryCycleDefaultDao` from Task 1
- Produces: `ExpenseCycleRepository`, `ExpenseCycle` domain model

- [ ] **Step 1: Create ExpenseCycle domain model**

```kotlin
package com.example.bluff.domain.model

import java.time.LocalDate

data class ExpenseCycle(
    val id: String,
    val categoryId: String,
    val name: String,
    val amountMinor: Long,
    val cycleDays: Int,
    val lastTransactionDate: LocalDate,
    val nextDueDate: LocalDate,
    val isActive: Boolean = true
) {
    val daysUntilDue: Long
        get() = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextDueDate)

    val isOverdue: Boolean
        get() = daysUntilDue < 0

    val isDueSoon: Boolean
        get() = daysUntilDue in 0..2

    val statusText: String
        get() = when {
            isOverdue -> "overdue by ${-daysUntilDue} days"
            daysUntilDue == 0L -> "due today"
            daysUntilDue == 1L -> "due tomorrow"
            else -> "in $daysUntilDue days"
        }
}

data class CategoryCycleDefault(
    val categoryId: String,
    val categoryName: String,
    val defaultCycleDays: Int
)
```

- [ ] **Step 2: Create ExpenseCycleRepository interface**

```kotlin
package com.example.bluff.domain.repository

import com.example.bluff.domain.model.CategoryCycleDefault
import com.example.bluff.domain.model.ExpenseCycle
import kotlinx.coroutines.flow.Flow

interface ExpenseCycleRepository {
    fun getAllActive(): Flow<List<ExpenseCycle>>
    fun getDueSoon(date: String): Flow<List<ExpenseCycle>>
    suspend fun getByCategoryId(categoryId: String): ExpenseCycle?
    suspend fun getDueForReminder(date: String): List<ExpenseCycle>
    suspend fun add(cycle: ExpenseCycle)
    suspend fun update(cycle: ExpenseCycle)
    suspend fun deactivate(id: String)
    suspend fun getDefaults(): List<CategoryCycleDefault>
    suspend fun getDefaultForCategory(categoryId: String): CategoryCycleDefault?
}
```

- [ ] **Step 3: Create ExpenseCycleRepositoryImpl**

```kotlin
package com.example.bluff.data.repository

import com.example.bluff.data.local.dao.CategoryCycleDefaultDao
import com.example.bluff.data.local.dao.ExpenseCycleDao
import com.example.bluff.data.local.entity.ExpenseCycleEntity
import com.example.bluff.domain.model.CategoryCycleDefault
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExpenseCycleRepositoryImpl(
    private val cycleDao: ExpenseCycleDao,
    private val defaultDao: CategoryCycleDefaultDao
) : ExpenseCycleRepository {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getAllActive(): Flow<List<ExpenseCycle>> {
        return cycleDao.getAllActive().map { entities ->
            entities.map { it.toModel() }
        }
    }

    override fun getDueSoon(date: String): Flow<List<ExpenseCycle>> {
        return cycleDao.getDueSoon(date).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun getByCategoryId(categoryId: String): ExpenseCycle? {
        return cycleDao.getByCategoryId(categoryId)?.toModel()
    }

    override suspend fun getDueForReminder(date: String): List<ExpenseCycle> {
        return cycleDao.getDueForReminder(date).map { it.toModel() }
    }

    override suspend fun add(cycle: ExpenseCycle) {
        cycleDao.insert(cycle.toEntity())
    }

    override suspend fun update(cycle: ExpenseCycle) {
        cycleDao.update(cycle.toEntity())
    }

    override suspend fun deactivate(id: String) {
        cycleDao.deactivate(id)
    }

    override suspend fun getDefaults(): List<CategoryCycleDefault> {
        return defaultDao.getAll().map {
            CategoryCycleDefault(it.categoryId, it.categoryName, it.defaultCycleDays)
        }
    }

    override suspend fun getDefaultForCategory(categoryId: String): CategoryCycleDefault? {
        return defaultDao.getByCategoryId(categoryId)?.let {
            CategoryCycleDefault(it.categoryId, it.categoryName, it.defaultCycleDays)
        }
    }

    private fun ExpenseCycleEntity.toModel() = ExpenseCycle(
        id = id,
        categoryId = categoryId,
        name = name,
        amountMinor = amountMinor,
        cycleDays = cycleDays,
        lastTransactionDate = LocalDate.parse(lastTransactionDate, formatter),
        nextDueDate = LocalDate.parse(nextDueDate, formatter),
        isActive = isActive
    )

    private fun ExpenseCycle.toEntity() = ExpenseCycleEntity(
        id = id,
        categoryId = categoryId,
        name = name,
        amountMinor = amountMinor,
        cycleDays = cycleDays,
        lastTransactionDate = lastTransactionDate.format(formatter),
        nextDueDate = nextDueDate.format(formatter),
        isActive = isActive,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
```

- [ ] **Step 4: Wire in AppContainer**

Add to AppContainer:
```kotlin
val expenseCycleRepository: ExpenseCycleRepository = ExpenseCycleRepositoryImpl(
    db.expenseCycleDao(),
    db.categoryCycleDefaultDao()
)
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add ExpenseCycle domain model and repository"
```

---

### Task 3: Add Use Cases + Pre-seed Defaults

**Files:**
- Create: `app/src/main/java/com/example/bluff/domain/usecase/cycle/GetCyclesUseCase.kt`
- Create: `app/src/main/java/com/example/bluff/domain/usecase/cycle/AddCycleUseCase.kt`
- Create: `app/src/main/java/com/example/bluff/domain/usecase/cycle/MarkCyclePaidUseCase.kt`
- Create: `app/src/main/java/com/example/bluff/domain/usecase/cycle/DeactivateCycleUseCase.kt`
- Modify: `app/src/main/java/com/example/bluff/di/AppContainer.kt`
- Modify: `app/src/main/java/com/example/bluff/BluffApplication.kt`

**Interfaces:**
- Consumes: `ExpenseCycleRepository` from Task 2
- Produces: Use cases, pre-seeded defaults

- [ ] **Step 1: Create GetCyclesUseCase**

```kotlin
package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import kotlinx.coroutines.flow.Flow

class GetCyclesUseCase(private val repository: ExpenseCycleRepository) {
    operator fun invoke(): Flow<List<ExpenseCycle>> = repository.getAllActive()

    fun getDueSoon(date: String): Flow<List<ExpenseCycle>> = repository.getDueSoon(date)

    suspend fun getByCategoryId(categoryId: String): ExpenseCycle? = repository.getByCategoryId(categoryId)

    suspend fun getDueForReminder(date: String): List<ExpenseCycle> = repository.getDueForReminder(date)
}
```

- [ ] **Step 2: Create AddCycleUseCase**

```kotlin
package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import java.time.LocalDate
import java.util.UUID

class AddCycleUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(
        categoryId: String,
        name: String,
        amountMinor: Long,
        cycleDays: Int,
        transactionDate: LocalDate = LocalDate.now()
    ): ExpenseCycle {
        // Check if cycle already exists for this category
        val existing = repository.getByCategoryId(categoryId)
        if (existing != null) {
            // Update existing cycle instead of creating duplicate
            val updated = existing.copy(
                amountMinor = amountMinor,
                cycleDays = cycleDays,
                lastTransactionDate = transactionDate,
                nextDueDate = transactionDate.plusDays(cycleDays.toLong())
            )
            repository.update(updated)
            return updated
        }

        // Create new cycle
        val cycle = ExpenseCycle(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            name = name,
            amountMinor = amountMinor,
            cycleDays = cycleDays,
            lastTransactionDate = transactionDate,
            nextDueDate = transactionDate.plusDays(cycleDays.toLong())
        )
        repository.add(cycle)
        return cycle
    }
}
```

- [ ] **Step 3: Create MarkCyclePaidUseCase**

```kotlin
package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.repository.ExpenseCycleRepository
import java.time.LocalDate

class MarkCyclePaidUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(cycleId: String) {
        val cycle = repository.getAllActive().let { flow ->
            // Need to get by ID — use a simple approach
            // In practice, we'd add a getById method
            null // Placeholder — will use repository directly
        }
    }
}
```

Wait, this is getting complex. Let me simplify — the `MarkCyclePaidUseCase` should take the cycle and reset it:

```kotlin
package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.repository.ExpenseCycleRepository
import java.time.LocalDate

class MarkCyclePaidUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(cycle: ExpenseCycle) {
        val today = LocalDate.now()
        val updated = cycle.copy(
            lastTransactionDate = today,
            nextDueDate = today.plusDays(cycle.cycleDays.toLong())
        )
        repository.update(updated)
    }
}
```

- [ ] **Step 4: Create DeactivateCycleUseCase**

```kotlin
package com.example.bluff.domain.usecase.cycle

import com.example.bluff.domain.repository.ExpenseCycleRepository

class DeactivateCycleUseCase(private val repository: ExpenseCycleRepository) {
    suspend operator fun invoke(cycleId: String) {
        repository.deactivate(cycleId)
    }
}
```

- [ ] **Step 5: Wire use cases in AppContainer**

```kotlin
val getCyclesUseCase = GetCyclesUseCase(expenseCycleRepository)
val addCycleUseCase = AddCycleUseCase(expenseCycleRepository)
val markCyclePaidUseCase = MarkCyclePaidUseCase(expenseCycleRepository)
val deactivateCycleUseCase = DeactivateCycleUseCase(expenseCycleRepository)
```

- [ ] **Step 6: Pre-seed defaults in BluffApplication**

Add to `BluffApplication.onCreate()` after database creation:
```kotlin
CoroutineScope(Dispatchers.IO).launch {
    val defaults = listOf(
        CategoryCycleDefaultEntity("cat_haircut", "Haircut", 30),
        CategoryCycleDefaultEntity("cat_shaving", "Shaving", 15),
        CategoryCycleDefaultEntity("cat_car_service", "Car Service", 180),
        CategoryCycleDefaultEntity("cat_insurance", "Insurance", 365),
        CategoryCycleDefaultEntity("cat_electricity", "Electricity", 30),
        CategoryCycleDefaultEntity("cat_gas_cylinder", "Gas Cylinder", 60),
        CategoryCycleDefaultEntity("cat_internet", "Internet", 30),
        CategoryCycleDefaultEntity("cat_mobile_recharge", "Mobile Recharge", 28)
    )
    database.categoryCycleDefaultDao().insertAll(defaults)
}
```

- [ ] **Step 7: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "feat: add cycle use cases and pre-seed category defaults"
```

---

## Phase 2: UI — Cycles Screen

### Task 4: Add Cycles Screen + ViewModel

**Files:**
- Create: `app/src/main/java/com/example/bluff/ui/cycles/CyclesScreen.kt`
- Create: `app/src/main/java/com/example/bluff/ui/cycles/CyclesViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/bluff/Navigation.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/more/MoreScreen.kt`

**Interfaces:**
- Consumes: `GetCyclesUseCase`, `MarkCyclePaidUseCase`, `DeactivateCycleUseCase` from Task 3
- Produces: `CyclesScreen` composable, `CyclesKey` navigation key

- [ ] **Step 1: Add CyclesKey to NavigationKeys**

```kotlin
@Serializable data object CyclesKey : NavKey
```

- [ ] **Step 2: Create CyclesViewModel**

```kotlin
package com.example.bluff.ui.cycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.usecase.cycle.DeactivateCycleUseCase
import com.example.bluff.domain.usecase.cycle.GetCyclesUseCase
import com.example.bluff.domain.usecase.cycle.MarkCyclePaidUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CyclesViewModel(
    private val getCyclesUseCase: GetCyclesUseCase,
    private val markCyclePaidUseCase: MarkCyclePaidUseCase,
    private val deactivateCycleUseCase: DeactivateCycleUseCase
) : ViewModel() {

    val cycles = getCyclesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filter = MutableStateFlow(CycleFilter.ALL)

    val filteredCycles: StateFlow<List<ExpenseCycle>> = combine(cycles, filter) { list, f ->
        when (f) {
            CycleFilter.ALL -> list
            CycleFilter.DUE_SOON -> list.filter { it.daysUntilDue in 0..7 }
            CycleFilter.OVERDUE -> list.filter { it.isOverdue }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markPaid(cycle: ExpenseCycle) {
        viewModelScope.launch {
            markCyclePaidUseCase(cycle)
        }
    }

    fun deactivate(cycleId: String) {
        viewModelScope.launch {
            deactivateCycleUseCase(cycleId)
        }
    }

    fun setFilter(f: CycleFilter) {
        filter.value = f
    }
}

enum class CycleFilter { ALL, DUE_SOON, OVERDUE }

class CyclesViewModelFactory(
    private val getCyclesUseCase: GetCyclesUseCase,
    private val markCyclePaidUseCase: MarkCyclePaidUseCase,
    private val deactivateCycleUseCase: DeactivateCycleUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CyclesViewModel(getCyclesUseCase, markCyclePaidUseCase, deactivateCycleUseCase) as T
    }
}
```

- [ ] **Step 3: Create CyclesScreen**

```kotlin
package com.example.bluff.ui.cycles

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyclesScreen(
    viewModel: CyclesViewModel,
    onBack: () -> Unit
) {
    val cycles by viewModel.filteredCycles.collectAsState()
    val filter by viewModel.filter.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<ExpenseCycle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Cycles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Background)
        ) {
            // Filter chips
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CycleFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (cycles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense cycles", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cycles, key = { it.id }) { cycle ->
                        CycleCard(
                            cycle = cycle,
                            onMarkPaid = { viewModel.markPaid(cycle) },
                            onDeactivate = { showDeleteDialog = cycle }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { cycle ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Deactivate Cycle") },
            text = { Text("Stop tracking '${cycle.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deactivate(cycle.id)
                    showDeleteDialog = null
                }) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CycleCard(
    cycle: ExpenseCycle,
    onMarkPaid: () -> Unit,
    onDeactivate: () -> Unit
) {
    val statusColor = when {
        cycle.isOverdue -> ExpenseColor
        cycle.isDueSoon -> Color(0xFFFF9800)
        else -> IncomeColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔄", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cycle.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Every ${cycle.cycleDays} days",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    cycle.statusText,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row {
                    IconButton(onClick = onMarkPaid, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Check, "Mark paid", tint = IncomeColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeactivate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Deactivate", tint = ExpenseColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: Add navigation entry in Navigation.kt**

Add `CyclesKey` to `isBottomNavVisible` set (no — it's not a bottom nav item, it's accessed from More).

Add entry:
```kotlin
entry<CyclesKey> {
    val viewModel: CyclesViewModel = viewModel(
        factory = CyclesViewModelFactory(
            AppContainer.instance.getCyclesUseCase,
            AppContainer.instance.markCyclePaidUseCase,
            AppContainer.instance.deactivateCycleUseCase
        )
    )
    CyclesScreen(viewModel = viewModel, onBack = { backStack.removeLastOrNull() })
}
```

- [ ] **Step 5: Add Cycles to MoreScreen**

Add `onNavigateToCycles` parameter and `MoreItem("Cycles", Icons.Default.CycleOnboarding, onNavigateToCycles)`.

Wait, need a cycle icon. Use `Icons.Default.Repeat` or `Icons.Default.Cached`.

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add CyclesScreen with filter chips and cycle management"
```

---

### Task 5: Add Upcoming Section to Home Screen

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeScreen.kt`

**Interfaces:**
- Consumes: `GetCyclesUseCase` from Task 3
- Produces: `upcomingCycles` StateFlow on HomeViewModel

- [ ] **Step 1: Add upcomingCycles to HomeViewModel**

```kotlin
val upcomingCycles: StateFlow<List<ExpenseCycle>> = getCyclesUseCase()
    .map { cycles ->
        cycles
            .sortedBy { it.nextDueDate }
            .take(5)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

- [ ] **Step 2: Add Upcoming section to HomeScreen**

After the budget card, before recent transactions:

```kotlin
if (upcomingCycles.isNotEmpty()) {
    item {
        SectionHeader("Upcoming")
        Spacer(Modifier.height(8.dp))
    }
    items(upcomingCycles) { cycle ->
        UpcomingCycleRow(cycle)
    }
    item { Spacer(Modifier.height(16.dp)) }
}
```

- [ ] **Step 3: Create UpcomingCycleRow composable**

```kotlin
@Composable
private fun UpcomingCycleRow(cycle: ExpenseCycle) {
    val statusColor = when {
        cycle.isOverdue -> ExpenseColor
        cycle.isDueSoon -> Color(0xFFFF9800)
        else -> IncomeColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔄", fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cycle.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            cycle.statusText,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: show upcoming expense cycles on Home screen"
```

---

## Phase 3: Post-Transaction Cycle Setup

### Task 6: Add CycleSetupSheet

**Files:**
- Create: `app/src/main/java/com/example/bluff/ui/cycles/CycleSetupSheet.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/addtransaction/AddTransactionSheet.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/addtransaction/AddTransactionViewModel.kt`

**Interfaces:**
- Consumes: `AddCycleUseCase`, `getDefaultForCategory` from Task 3
- Produces: `CycleSetupSheet` composable

- [ ] **Step 1: Create CycleSetupSheet**

```kotlin
package com.example.bluff.ui.cycles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSetupSheet(
    categoryName: String,
    defaultCycleDays: Int?,
    onConfirm: (cycleDays: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(defaultCycleDays ?: 30) }
    val presets = listOf(7, 14, 21, 28, 30, 60, 90, 180, 365)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Track this expense?",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set a reminder for your next $categoryName",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            // Duration presets
            Text("Every", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.take(4).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.drop(4).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // Confirm button
            Button(
                onClick = { onConfirm(selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Set Reminder", color = androidx.compose.ui.graphics.Color.White)
            }
            Spacer(Modifier.height(12.dp))

            // Skip button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip", color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
```

- [ ] **Step 2: Add cycle setup trigger to AddTransactionSheet**

After the confirmation dialog's "Confirm" action, show the CycleSetupSheet:

```kotlin
// After transaction is saved successfully
var showCycleSetup by remember { mutableStateOf(false) }
var savedCategoryId by remember { mutableStateOf<String?>(null) }
var savedAmount by remember { mutableStateOf(0L) }
var savedCategoryName by remember { mutableStateOf("") }

// In the confirm action:
showCycleSetup = true
savedCategoryId = selectedCategoryId
savedAmount = amount
savedCategoryName = selectedCategoryName

if (showCycleSetup) {
    CycleSetupSheet(
        categoryName = savedCategoryName,
        defaultCycleDays = defaultCycleDays,
        onConfirm = { days ->
            viewModel.createCycle(savedCategoryId!!, savedCategoryName, savedAmount, days)
            showCycleSetup = false
            onDismiss()
        },
        onDismiss = {
            showCycleSetup = false
            onDismiss()
        }
    )
}
```

- [ ] **Step 3: Add createCycle to AddTransactionViewModel**

```kotlin
fun createCycle(categoryId: String, name: String, amountMinor: Long, cycleDays: Int) {
    viewModelScope.launch {
        addCycleUseCase(categoryId, name, amountMinor, cycleDays)
    }
}
```

- [ ] **Step 4: Load default cycle days in AddTransactionViewModel**

```kotlin
private val _defaultCycleDays = MutableStateFlow<Int?>(null)
val defaultCycleDays: StateFlow<Int?> = _defaultCycleDays

// When category is selected:
fun onCategorySelected(categoryId: String, categoryName: String) {
    selectedCategoryId = categoryId
    viewModelScope.launch {
        val default = addCycleUseCase.repository.getDefaultForCategory(categoryId)
        _defaultCycleDays.value = default?.defaultCycleDays
    }
}
```

Wait, `addCycleUseCase` doesn't expose the repository. Let me fix — inject `GetCyclesUseCase` or access via repository directly:

```kotlin
// In AddTransactionViewModel, add:
private val expenseCycleRepository = AppContainer.instance.expenseCycleRepository

// Then:
fun onCategorySelected(categoryId: String, categoryName: String) {
    selectedCategoryId = categoryId
    viewModelScope.launch {
        val default = expenseCycleRepository.getDefaultForCategory(categoryId)
        _defaultCycleDays.value = default?.defaultCycleDays
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add CycleSetupSheet for post-transaction cycle creation"
```

---

## Phase 4: Reminders

### Task 7: Add CycleReminderWorker

**Files:**
- Create: `app/src/main/java/com/example/bluff/data/sync/CycleReminderWorker.kt`
- Modify: `app/src/main/java/com/example/bluff/BluffApplication.kt`

**Interfaces:**
- Consumes: `ExpenseCycleDao` from Task 1
- Produces: `CycleReminderWorker`, notification channel

- [ ] **Step 1: Create CycleReminderWorker**

```kotlin
package com.example.bluff.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluff.R
import com.example.bluff.data.local.BluffDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class CycleReminderWorker(
    context: Context,
    params: WorkerParameters,
    private val db: BluffDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val twoDaysFromNow = today.plusDays(2)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val dueCycles = db.expenseCycleDao().getDueForReminder(twoDaysFromNow.format(formatter))

        if (dueCycles.isNotEmpty()) {
            createNotificationChannel()

            dueCycles.forEach { cycle ->
                val daysUntil = ChronoUnit.DAYS.between(today, LocalDate.parse(cycle.nextDueDate, formatter))
                val message = when {
                    daysUntil < 0 -> "${cycle.name} was due ${-daysUntil} days ago"
                    daysUntil == 0L -> "${cycle.name} is due today"
                    daysUntil == 1L -> "${cycle.name} is due tomorrow"
                    else -> "${cycle.name} is due in $daysUntil days"
                }

                sendNotification(cycle.id.hashCode(), cycle.name, message)
            }
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming expenses"
            }
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(id: Int, title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    companion object {
        const val CHANNEL_ID = "expense_reminders"
    }
}
```

- [ ] **Step 2: Register worker in BluffApplication**

Add after RecurringWorker registration:
```kotlin
val cycleReminderWorkRequest = PeriodicWorkRequestBuilder<CycleReminderWorker>(
    1, TimeUnit.DAYS
).build()

WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "cycle_reminders",
    ExistingPeriodicWorkPolicy.KEEP,
    cycleReminderWorkRequest
)
```

- [ ] **Step 3: Add notification icon**

Create a simple notification icon or use an existing one. For now, use `android.R.drawable.ic_dialog_info` as a fallback.

Actually, the app needs `ic_notification` drawable. Let me check if one exists:

If not, create a simple vector drawable or use an existing icon.

- [ ] **Step 4: Add POST_NOTIFICATIONS permission to AndroidManifest**

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: add CycleReminderWorker for daily expense reminders"
```

---

### Task 8: Final Build + Test on Device

**Files:**
- Various

- [ ] **Step 1: Run full build**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: Install on device**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`

- [ ] **Step 3: Manual test checklist**

- [ ] Add expense → cycle setup sheet appears
- [ ] Set cycle for haircut (30 days) → appears in Cycles screen
- [ ] Home shows upcoming cycle
- [ ] Mark as paid → cycle resets
- [ ] Add another haircut → updates existing cycle (no duplicate)
- [ ] More → Cycles shows all cycles
- [ ] Filter chips work (All, Due Soon, Overdue)
- [ ] Deactivate cycle works
- [ ] Notification appears (may need to wait or test manually)

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "chore: final cleanup and verification"
```

- [ ] **Step 5: Push to GitHub**

```bash
git push origin main
```
