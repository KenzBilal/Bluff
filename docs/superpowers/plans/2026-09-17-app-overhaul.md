# BLUFF App Overhaul Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove Ledger, move Debt to bottom nav, fix Recurring/Budgets/Goals/Accounts to be fully functional, remove dead Supabase sync.

**Architecture:** Three-layer (UI/Compose → Domain/UseCases → Data/Room). Manual DI via AppContainer. WorkManager for background recurring processing. All data local (no cloud sync).

**Tech Stack:** Kotlin 2.3.20, Jetpack Compose, Room 2.7.1, WorkManager, Navigation 3, Coroutines/Flow

## Global Constraints

- Kotlin 2.3.20, Compose BOM 2026.03.01, Room 2.7.1
- Money stored as Long (minor units/paise)
- Entity ↔ Domain ↔ DTO tri-layer mapping
- Manual DI via AppContainer (no Hilt)
- Build requires: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk`
- Device: `00161357D002240`

---

## Phase 1: Remove Ledger + Restructure Navigation

### Task 1: Remove Ledger Screen and Nav Key

**Files:**
- Delete: `app/src/main/java/com/example/bluff/ui/transactions/TransactionsScreen.kt`
- Delete: `app/src/main/java/com/example/bluff/ui/transactions/TransactionsViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/bluff/Navigation.kt`

- [ ] **Step 1: Delete TransactionsScreen.kt and TransactionsViewModel.kt**

```bash
rm app/src/main/java/com/example/bluff/ui/transactions/TransactionsScreen.kt
rm app/src/main/java/com/example/bluff/ui/transactions/TransactionsViewModel.kt
```

- [ ] **Step 2: Remove TransactionsKey from NavigationKeys.kt**

Remove line 8: `@Serializable data object TransactionsKey : NavKey`

- [ ] **Step 3: Update Navigation.kt bottom nav items**

Replace the `bottomNavItems` list (lines 218-224):

```kotlin
private val bottomNavItems = listOf(
    BottomNavItem(HomeKey, "Home", Icons.Default.Home),
    BottomNavItem(CalendarKey, "Calendar", Icons.Default.CalendarMonth),
    BottomNavItem(DebtKey, "Debt", Icons.Default.AccountBalance),
    BottomNavItem(AnalyticsKey, "Analytics", Icons.Default.Analytics),
    BottomNavItem(MoreKey, "More", Icons.Default.MoreHoriz)
)
```

- [ ] **Step 4: Remove TransactionsKey from isBottomNavVisible set**

Change line 93 from:
```kotlin
val isBottomNavVisible = currentKey in setOf(HomeKey, CalendarKey, TransactionsKey, AnalyticsKey, DebtKey, MoreKey)
```
to:
```kotlin
val isBottomNavVisible = currentKey in setOf(HomeKey, CalendarKey, DebtKey, AnalyticsKey, MoreKey)
```

- [ ] **Step 5: Remove TransactionsScreen entry and import**

Delete line 162: `entry<TransactionsKey> { TransactionsScreen() }`
Delete line 70: `import com.example.bluff.ui.transactions.TransactionsScreen`

- [ ] **Step 6: Remove Debt from MoreScreen**

In `MoreScreen.kt`, remove `onNavigateToDebt` parameter and the Debt `MoreItem`.

- [ ] **Step 7: Remove Debt from MoreScreen Navigation wiring**

In `Navigation.kt` line 175, remove: `onNavigateToDebt = { backStack.add(DebtKey) }`

- [ ] **Step 8: Update DebtScreen to not use onBack from nav**

Since Debt is now a bottom nav tab, it shouldn't have a back arrow. Change `DebtScreen` to remove the `onBack` parameter and Scaffold TopAppBar.

- [ ] **Step 9: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "refactor: remove Ledger tab, move Debt to bottom nav"
```

---

## Phase 2: Remove Supabase Sync (Dead Code)

### Task 2: Remove Sync Infrastructure

**Files:**
- Delete: `app/src/main/java/com/example/bluff/data/sync/SyncWorker.kt`
- Delete: `app/src/main/java/com/example/bluff/data/sync/SyncManager.kt`
- Delete: `app/src/main/java/com/example/bluff/data/local/entity/SyncQueueEntity.kt`
- Delete: `app/src/main/java/com/example/bluff/data/local/dao/SyncQueueDao.kt`
- Modify: `app/src/main/java/com/example/bluff/di/AppContainer.kt`
- Modify: `app/src/main/java/com/example/bluff/data/local/BluffDatabase.kt`

- [ ] **Step 1: Delete sync files**

```bash
rm -f app/src/main/java/com/example/bluff/data/sync/SyncWorker.kt
rm -f app/src/main/java/com/example/bluff/data/sync/SyncManager.kt
rm -f app/src/main/java/com/example/bluff/data/local/entity/SyncQueueEntity.kt
rm -f app/src/main/java/com/example/bluff/data/local/dao/SyncQueueDao.kt
```

- [ ] **Step 2: Remove SyncQueueDao from BluffDatabase**

In `BluffDatabase.kt`, remove the `abstract fun syncQueueDao(): SyncQueueDao` line.

- [ ] **Step 3: Remove sync-related code from AppContainer**

Remove `SupabaseClient` initialization, `SyncManager` initialization, and any sync-related repository parameters that pass `supabase`.

- [ ] **Step 4: Remove supabase dependency from repositories**

For each repository (TransactionRepositoryImpl, AccountRepositoryImpl, etc.), remove the `supabase` parameter from the constructor and any Supabase-related imports.

- [ ] **Step 5: Remove supabase from build.gradle.kts**

Remove the Supabase dependency lines from `app/build.gradle.kts`.

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: remove broken Supabase sync, app is now local-only"
```

---

## Phase 3: Fix Recurring Transactions

### Task 3: Add calculateNextRunDate Utility

**Files:**
- Create: `app/src/main/java/com/example/bluff/domain/util/RecurrenceUtil.kt`
- Test: `app/src/test/java/com/example/bluff/domain/util/RecurrenceUtilTest.kt`

- [ ] **Step 1: Create RecurrenceUtil.kt**

```kotlin
package com.example.bluff.domain.util

import com.example.bluff.domain.model.RecurrenceFrequency
import java.time.LocalDate

object RecurrenceUtil {
    fun calculateNextRunDate(current: LocalDate, frequency: RecurrenceFrequency): LocalDate {
        return when (frequency) {
            RecurrenceFrequency.DAILY -> current.plusDays(1)
            RecurrenceFrequency.WEEKLY -> current.plusWeeks(1)
            RecurrenceFrequency.MONTHLY -> current.plusMonths(1)
            RecurrenceFrequency.YEARLY -> current.plusYears(1)
        }
    }

    fun firstOccurrenceAfter(startDate: LocalDate, frequency: RecurrenceFrequency, after: LocalDate): LocalDate {
        var date = startDate
        while (!date.isAfter(after)) {
            date = calculateNextRunDate(date, frequency)
        }
        return date
    }
}
```

- [ ] **Step 2: Write unit tests**

```kotlin
package com.example.bluff.domain.util

import com.example.bluff.domain.model.RecurrenceFrequency
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RecurrenceUtilTest {
    @Test
    fun `daily advances by 1 day`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2026, 9, 18), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.DAILY))
    }

    @Test
    fun `weekly advances by 7 days`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2026, 9, 24), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.WEEKLY))
    }

    @Test
    fun `monthly advances by 1 month`() {
        val date = LocalDate.of(2026, 1, 15)
        assertEquals(LocalDate.of(2026, 2, 15), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.MONTHLY))
    }

    @Test
    fun `yearly advances by 1 year`() {
        val date = LocalDate.of(2026, 9, 17)
        assertEquals(LocalDate.of(2027, 9, 17), RecurrenceUtil.calculateNextRunDate(date, RecurrenceFrequency.YEARLY))
    }

    @Test
    fun `firstOccurrenceAfter skips past dates`() {
        val start = LocalDate.of(2026, 1, 1)
        val after = LocalDate.of(2026, 9, 17)
        val result = RecurrenceUtil.firstOccurrenceAfter(start, RecurrenceFrequency.MONTHLY, after)
        assertEquals(LocalDate.of(2026, 10, 1), result)
    }
}
```

- [ ] **Step 3: Run tests**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew test --tests "com.example.bluff.domain.util.RecurrenceUtilTest"`
Expected: All tests PASS

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/com/example/bluff/domain/util/RecurrenceUtil.kt app/src/test/java/com/example/bluff/domain/util/RecurrenceUtilTest.kt
git commit -m "feat: add RecurrenceUtil with calculateNextRunDate and tests"
```

### Task 4: Add RecurringWorker

**Files:**
- Create: `app/src/main/java/com/example/bluff/data/sync/RecurringWorker.kt`
- Modify: `app/src/main/java/com/example/bluff/data/local/dao/RecurringTransactionDao.kt`
- Modify: `app/src/main/java/com/example/bluff/BluffApplication.kt`

- [ ] **Step 1: Add DAO queries to RecurringTransactionDao**

Add these methods:

```kotlin
@Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextRunDate <= :today")
suspend fun getDueRecurringTransactions(today: String): List<RecurringTransactionEntity>

@Query("UPDATE recurring_transactions SET nextRunDate = :nextDate WHERE id = :id")
suspend fun advanceNextRunDate(id: String, nextDate: String)

@Query("UPDATE recurring_transactions SET isActive = 0 WHERE id = :id")
suspend fun deactivate(id: String)
```

- [ ] **Step 2: Create RecurringWorker.kt**

```kotlin
package com.example.bluff.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.util.RecurrenceUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RecurringWorker(
    context: Context,
    params: WorkerParameters,
    private val db: BluffDatabase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val todayStr = today.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val dueTransactions = db.recurringTransactionDao().getDueRecurringTransactions(todayStr)

        for (recurring in dueTransactions) {
            // Check if end date has passed
            if (recurring.endDate != null) {
                val endDate = LocalDate.parse(recurring.endDate)
                if (today.isAfter(endDate)) {
                    db.recurringTransactionDao().deactivate(recurring.id)
                    continue
                }
            }

            // Create transaction from recurring template
            val transactionEntity = com.example.bluff.data.local.entity.TransactionEntity(
                id = java.util.UUID.randomUUID().toString(),
                accountId = recurring.accountId,
                categoryId = recurring.categoryId,
                amountMinor = recurring.amountMinor,
                type = recurring.type,
                notes = recurring.name,
                transactionDate = todayStr,
                recurringId = recurring.id,
                contactName = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            db.transactionDao().insert(transactionEntity)

            // Advance next run date
            val nextDate = RecurrenceUtil.calculateNextRunDate(today, recurring.frequency)
            db.recurringTransactionDao().advanceNextRunDate(recurring.id, nextDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        }

        return Result.success()
    }
}
```

- [ ] **Step 3: Register Worker in BluffApplication**

Add to `BluffApplication.onCreate()`:

```kotlin
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// After existing initialization:
val recurringWorkRequest = PeriodicWorkRequestBuilder<RecurringWorker>(
    1, TimeUnit.DAYS
).build()

WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    "recurring_transactions",
    ExistingPeriodicWorkPolicy.KEEP,
    recurringWorkRequest
)
```

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add RecurringWorker for auto-creating recurring transactions"
```

### Task 5: Fix RecurringScreen UI

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/recurring/RecurringScreen.kt`

- [ ] **Step 1: Show account name on recurring card**

Update `RecurringCard` to display the account name (load accounts in ViewModel, pass to card).

- [ ] **Step 2: Fix nextRunDate initialization**

In `RecurringViewModel.saveRecurring()`, if `startDate` is in the past, use `RecurrenceUtil.firstOccurrenceAfter()` to set `nextRunDate` to the first future occurrence.

- [ ] **Step 3: Add confirmation dialog before delete**

Add `showDeleteDialog` state and AlertDialog before calling `deleteRecurring()`.

- [ ] **Step 4: Build and verify on device**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Create a daily recurring → check next day if transaction appears.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "fix: improve RecurringScreen with account display and delete confirmation"
```

---

## Phase 4: Fix Budgets

### Task 6: Add Budget Spending Calculation

**Files:**
- Modify: `app/src/main/java/com/example/bluff/data/local/dao/TransactionDao.kt`
- Modify: `app/src/main/java/com/example/bluff/data/repository/BudgetRepositoryImpl.kt`
- Modify: `app/src/main/java/com/example/bluff/domain/model/Budget.kt`

- [ ] **Step 1: Add spending query to TransactionDao**

```kotlin
@Query("""
    SELECT COALESCE(SUM(amountMinor), 0) as spent
    FROM transactions
    WHERE type = 'EXPENSE'
    AND transactionDate >= :startDate
    AND transactionDate <= :endDate
    AND (:categoryId IS NULL OR categoryId = :categoryId)
""")
suspend fun getSpendingInRange(startDate: String, endDate: String, categoryId: String?): Long
```

- [ ] **Step 2: Update BudgetRepositoryImpl to populate spentMinor**

In `getAllBudgets()`, for each budget, call `transactionDao.getSpendingInRange()` with the budget's date range and categoryId, then set `spentMinor` on the Budget model.

```kotlin
override fun getAllBudgets(): Flow<List<Budget>> {
    return budgetDao.getAllBudgets().map { entities ->
        entities.map { entity ->
            val budget = entity.toModel()
            val spent = transactionDao.getSpendingInRange(
                budget.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                budget.endDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                budget.categoryId
            )
            budget.copy(spentMinor = spent)
        }
    }
}
```

- [ ] **Step 3: Fix period logic in BudgetViewModel**

Replace the hardcoded month dates with proper period-based calculation:

```kotlin
val (start, end) = when (period) {
    RecurrenceFrequency.WEEKLY -> {
        now.with(java.time.DayOfWeek.MONDAY) to now.with(java.time.DayOfWeek.SUNDAY)
    }
    RecurrenceFrequency.MONTHLY -> {
        now.withDayOfMonth(1) to now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
    }
    RecurrenceFrequency.YEARLY -> {
        now.withDayOfYear(1) to now.with(java.time.temporal.TemporalAdjusters.lastDayOfYear())
    }
    else -> now.withDayOfMonth(1) to now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
}
```

- [ ] **Step 4: Add delete confirmation to BudgetScreen**

Add `showDeleteDialog` state and AlertDialog.

- [ ] **Step 5: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Create a budget → add expenses → verify progress bar shows spending.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "feat: fix Budgets with real spending calculation and progress bar"
```

### Task 7: Show Budget on Home Screen

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeScreen.kt`

- [ ] **Step 1: Load overall budget in HomeViewModel**

Add a `overallBudget` StateFlow that loads the first budget with `categoryId == null` and its spent amount.

```kotlin
val overallBudget: StateFlow<Budget?> = budgetsUseCase.getAll()
    .map { budgets -> budgets.firstOrNull { it.categoryId == null } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
```

- [ ] **Step 2: Show budget card on HomeScreen**

Below the income/spent cards, show a budget progress card if `overallBudget` is not null.

- [ ] **Step 3: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Create overall budget → Home shows progress.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: show budget progress on Home screen"
```

---

## Phase 5: Fix Goals

### Task 8: Add Goal Progress Tracking

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/goals/GoalsScreen.kt`
- Modify: `app/src/main/java/com/example/bluff/domain/usecase/goal/UpsertGoalUseCase.kt`

- [ ] **Step 1: Add "Add Money" bottom sheet**

Create `GoalContributeSheet.kt` with a simple amount input.

```kotlin
package com.example.bluff.ui.goals

@Composable
fun GoalContributeSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onContribute: (Long) -> Unit
) {
    // Amount input + confirm button
    // Uses BluffAmountInput for consistency
}
```

- [ ] **Step 2: Add contribute action to GoalsViewModel**

```kotlin
fun contributeToGoal(goalId: String, amountMinor: Long) {
    viewModelScope.launch {
        val goal = goals.value.first { it.id == goalId }
        val updated = goal.copy(
            currentAmountMinor = goal.currentAmountMinor + amountMinor,
            isCompleted = (goal.currentAmountMinor + amountMinor) >= goal.targetAmountMinor
        )
        upsertGoalUseCase(updated)
    }
}
```

- [ ] **Step 3: Update GoalCard with contribute button**

Add a "+" button on each goal card that opens the contribute sheet.

- [ ] **Step 4: Format target date properly**

Replace `goal.targetDate.toString()` with formatted date using `dd MMM yyyy`.

- [ ] **Step 5: Add completion badge**

When `isCompleted`, show a checkmark overlay and "Completed" text.

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Create goal → add money → verify progress updates.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add goal progress tracking with contribute action"
```

### Task 9: Show Goals on Home Screen

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeScreen.kt`

- [ ] **Step 1: Load top goals in HomeViewModel**

```kotlin
val topGoals: StateFlow<List<Goal>> = getGoalsUseCase()
    .map { goals -> goals.filter { !it.isCompleted }.take(3) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

- [ ] **Step 2: Show goals section on HomeScreen**

Below recent transactions, show "Goals" section with top 2-3 goals and progress bars.

- [ ] **Step 3: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Create goals → Home shows progress.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "feat: show goal progress on Home screen"
```

---

## Phase 6: Fix Accounts

### Task 10: Fix Home Total Balance

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/home/HomeViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/data/repository/AccountRepositoryImpl.kt`

- [ ] **Step 1: Fix getAllAccounts to include real-time balance**

Update `AccountRepositoryImpl.getAllAccounts()` to call `getAccountBalance()` for each account and include it in the model.

```kotlin
override fun getAllAccounts(): Flow<List<Account>> {
    return accountDao.getAllAccounts().map { entities ->
        entities.map { entity ->
            val balance = accountDao.getAccountBalance(entity.id)
            entity.toModel(currentBalanceMinor = balance)
        }
    }
}
```

- [ ] **Step 2: Remove duplicate getActiveAccounts()**

Delete `getActiveAccounts()` from AccountDao and AccountRepository (it's identical to `getAllAccounts()`).

- [ ] **Step 3: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Add transactions → Home balance updates correctly.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "fix: Home screen shows correct account balance"
```

### Task 11: Add Per-Account Transaction View

**Files:**
- Create: `app/src/main/java/com/example/bluff/ui/accounts/AccountDetailScreen.kt`
- Create: `app/src/main/java/com/example/bluff/ui/accounts/AccountDetailViewModel.kt`
- Modify: `app/src/main/java/com/example/bluff/NavigationKeys.kt`
- Modify: `app/src/main/java/com/example/bluff/Navigation.kt`
- Modify: `app/src/main/java/com/example/bluff/ui/accounts/AccountsScreen.kt`

- [ ] **Step 1: Add AccountDetailKey to NavigationKeys**

```kotlin
@Serializable data class AccountDetailKey(val accountId: String) : NavKey
```

- [ ] **Step 2: Create AccountDetailViewModel**

```kotlin
class AccountDetailViewModel(
    accountId: String,
    getAccountByIdUseCase: GetAccountByIdUseCase,
    getTransactionsByAccountUseCase: GetTransactionsByAccountUseCase
) : ViewModel() {
    val account = getAccountByIdUseCase(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactions = getTransactionsByAccountUseCase(accountId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
```

- [ ] **Step 3: Create AccountDetailScreen**

Shows account header (name, type, balance) + list of transactions for that account.

- [ ] **Step 4: Update AccountsScreen to navigate on tap**

Change tap handler from opening edit sheet to navigating to `AccountDetailKey`.

- [ ] **Step 5: Add navigation entry in Navigation.kt**

```kotlin
entry<AccountDetailKey> { key ->
    AccountDetailScreen(
        accountId = key.accountId,
        onBack = { backStack.removeLastOrNull() }
    )
}
```

- [ ] **Step 6: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
Manual test: Tap account → see its transactions.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: add per-account transaction detail view"
```

### Task 12: Add Account Delete Confirmation

**Files:**
- Modify: `app/src/main/java/com/example/bluff/ui/accounts/AccountsScreen.kt`

- [ ] **Step 1: Add delete confirmation dialog**

Add `showDeleteDialog` state and AlertDialog before calling `deleteAccount()`.

- [ ] **Step 2: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "fix: add delete confirmation for accounts"
```

---

## Phase 7: Fix Categories

### Task 13: Fix Category Date Bug

**Files:**
- Modify: `app/src/main/java/com/example/bluff/data/repository/CategoryRepositoryImpl.kt`

- [ ] **Step 1: Fix getMonthlySpendByCategory() date comparison**

Replace the broken `cal.timeInMillis.toString()` with proper ISO date string:

```kotlin
val startOfMonth = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
```

- [ ] **Step 2: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "fix: correct date comparison in category monthly spend query"
```

---

## Phase 8: Database Migration

### Task 14: Add goalId to Transactions (Migration v3→v4)

**Files:**
- Modify: `app/src/main/java/com/example/bluff/data/local/BluffDatabase.kt`
- Modify: `app/src/main/java/com/example/bluff/data/local/entity/TransactionEntity.kt`

- [ ] **Step 1: Add goalId column to TransactionEntity**

```kotlin
@ColumnInfo(name = "goalId")
val goalId: String? = null
```

- [ ] **Step 2: Add migration in BluffDatabase**

```kotlin
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transactions ADD COLUMN goalId TEXT")
    }
}
```

- [ ] **Step 3: Register migration**

Add `addMigrations(MIGRATION_3_4)` to the database builder.

- [ ] **Step 4: Build and verify**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`
App should launch without crashes on existing data.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "feat: add goalId to transactions, DB migration v3→v4"
```

---

## Phase 9: Final Cleanup

### Task 15: Clean Up and Final Build

**Files:**
- Various

- [ ] **Step 1: Remove unused imports across modified files**

- [ ] **Step 2: Run full build**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Install on device and test all features**

Run: `JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew installDebug`

Manual test checklist:
- [ ] Home shows correct balance
- [ ] Home shows budget progress
- [ ] Home shows goal progress
- [ ] Calendar shows transactions
- [ ] Debt works (add, mark paid, delete)
- [ ] Analytics shows correct data
- [ ] More → Accounts (tap account → see transactions)
- [ ] More → Budgets (create, see progress)
- [ ] More → Goals (create, add money, see progress)
- [ ] More → Categories (tree works)
- [ ] More → Recurring (create, auto-creates transactions)
- [ ] More → Settings
- [ ] Add transaction (Expense/Debt/Transfer modes)
- [ ] FAB opens add transaction sheet

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "chore: final cleanup and verification"
```

- [ ] **Step 5: Push to GitHub**

```bash
git push origin main
```
