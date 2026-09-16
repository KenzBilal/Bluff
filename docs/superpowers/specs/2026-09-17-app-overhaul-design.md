# BLUFF App Overhaul — Design Spec

## Overview

Remove the Ledger tab, move Debt to bottom nav, and make Recurring/Budgets/Goals/Accounts actually functional. Fix critical bugs and remove dead code.

## 1. Navigation Restructuring

### Remove
- `TransactionsScreen` + `TransactionsViewModel` (Ledger)
- `TransactionsKey` from nav keys
- `TransactionsScreen` import from Navigation.kt

### New Bottom Nav (5 tabs)
| Tab | Key | Icon |
|-----|-----|------|
| Home | `HomeKey` | Home |
| Calendar | `CalendarKey` | CalendarMonth |
| Debt | `DebtKey` | AccountBalance |
| Analytics | `AnalyticsKey` | Analytics |
| More | `MoreKey` | MoreHoriz |

### More Screen Items (after changes)
- Accounts
- Budgets
- Goals
- Categories
- Recurring
- Settings

(Debt removed from More since it's now a bottom nav tab)

## 2. Fix Recurring Transactions

### Problem
Recurring is a CRUD shell. No auto-creation, no `nextRunDate` advancement, no WorkManager.

### Solution

#### A. Add `calculateNextRunDate()` utility
```kotlin
fun calculateNextRunDate(current: LocalDate, frequency: RecurrenceFrequency): LocalDate {
    return when (frequency) {
        DAILY -> current.plusDays(1)
        WEEKLY -> current.plusWeeks(1)
        MONTHLY -> current.plusMonths(1)
        YEARLY -> current.plusYears(1)
    }
}
```

#### B. Add `RecurringWorker` (PeriodicWorkRequest)
- Runs once daily (repeatInterval = 1 day)
- Queries `recurring_transactions` where `isActive = 1 AND nextRunDate <= :today AND (endDate IS NULL OR endDate >= :today)`
- For each matching recurring transaction:
  1. Create a `TransactionEntity` from the template (copy amount, type, accountId, categoryId, notes)
  2. Set `recurringId` on the new transaction to link back
  3. Update `nextRunDate` = `calculateNextRunDate(nextRunDate, frequency)`
- If `nextRunDate` passes `endDate`, set `isActive = 0`

#### C. DAO additions
```kotlin
@Query("SELECT * FROM recurring_transactions WHERE isActive = 1 AND nextRunDate <= :today")
suspend fun getDueRecurringTransactions(today: String): List<RecurringTransactionEntity>

@Query("UPDATE recurring_transactions SET nextRunDate = :nextDate WHERE id = :id")
suspend fun advanceNextRunDate(id: String, nextDate: String)

@Query("UPDATE recurring_transactions SET isActive = 0 WHERE id = :id")
suspend fun deactivate(id: String)
```

#### D. Register Worker in Application
- In `BluffApplication.onCreate()`, enqueue `PeriodicWorkRequest` for `RecurringWorker`
- Use `enqueueUniquePeriodicWork` to avoid duplicates

#### E. Fix existing issues
- Set `nextRunDate` to first occurrence (not `startDate` if `startDate` is in the past)
- Show which account a recurring transaction is tied to on the card

## 3. Fix Budgets

### Problem
`spentMinor` always 0. No spending calculation. Progress bar never renders.

### Solution

#### A. Add DAO spending query
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

#### B. Populate `spentMinor` in `BudgetRepositoryImpl`
- When loading budgets, query spending for each budget's date range + category
- Set `spentMinor` on the `Budget` model before returning

#### C. Fix period logic
```kotlin
when (budget.period) {
    WEEKLY -> { start = now.with(DayOfWeek.MONDAY); end = now.with(DayOfWeek.SUNDAY) }
    MONTHLY -> { start = now.withDayOfMonth(1); end = now.with(TemporalAdjusters.lastDayOfMonth()) }
    YEARLY -> { start = now.withDayOfYear(1); end = now.with(TemporalAdjusters.lastDayOfYear()) }
}
```

#### D. Show budget on Home
- `HomeViewModel` loads the first overall budget (categoryId == null)
- Display progress bar with `spent / total`

#### E. Add delete button to budget cards
- Show a delete icon on each budget card
- Confirmation dialog before delete

## 4. Fix Goals

### Problem
`currentAmountMinor` always 0. No way to contribute. Always 0%.

### Solution

#### A. Add "Add Money" action
- Button on goal card (or tap goal → detail → add money)
- Opens a simple amount input bottom sheet
- Updates `currentAmountMinor` via `UpsertGoalUseCase`

#### B. Add `goalId` to TransactionEntity (optional)
- New optional column `goalId TEXT` on `transactions` table
- When contributing to a goal via the goal screen, create an expense transaction linked to the goal
- Room migration v3→v4

#### C. Show goal progress on Home
- "Goals" section below recent transactions
- Show top 2-3 goals with progress bars

#### D. Completion handling
- When `currentAmountMinor >= targetAmountMinor`, show a celebration (checkmark, "Completed" badge)
- Option to archive completed goals

#### E. Format target date properly
- Use `dd MMM yyyy` format instead of `LocalDate.toString()`

## 5. Fix Accounts

### Problem
Home shows wrong balance (sum of initial, not current). No per-account transaction view.

### Solution

#### A. Fix Home total balance
- `HomeViewModel.totalBalance` should use `getAccountBalance()` per account, not `currentBalanceMinor` from the model
- Or: make `getAllAccounts()` return models with real-time balance

#### B. Per-account transaction list
- Tap account card → navigate to `AccountDetailKey(accountId)`
- Shows account info (name, type, balance) + list of transactions for that account
- Filter by date range

#### C. Remove duplicate query
- Delete `getActiveAccounts()` (identical to `getAllAccounts()`)

#### D. Add delete confirmation
- Show dialog before archiving an account

## 6. Fix Categories

### Problem
Date comparison bug in `getMonthlySpendByCategory()`.

### Solution

#### A. Fix the date bug
- `CategoryRepository.getMonthlySpendByCategory()` converts dates using `cal.timeInMillis.toString()` which produces epoch millis as string
- `transactionDate` is stored as `"yyyy-MM-dd"` strings
- Fix: use `LocalDate.now().withDayOfMonth(1).toString()` for `startOfMonth`

#### B. Move Categories to Settings (or keep in More)
- Keep in More for now, but ensure category tree picker works correctly everywhere

## 7. Fix Supabase Sync

### Problem
`scheduleSync()` never called. `SyncWorker` can't be instantiated.

### Solution
- **Option A (recommended):** Remove Supabase sync entirely for now. The app works offline. Revisit later with proper setup.
- **Option B:** Fix it properly with a custom `WorkerFactory` in `BluffApplication`.

Decision: **Option A** — remove sync code to reduce complexity. The app is a personal finance tool; local-only is fine for v1.

### Files to modify
- Remove `SyncWorker.kt`, `SyncManager.kt`, `SyncQueueEntity`, `SyncQueueDao`
- Remove `SupabaseClient` initialization from `AppContainer`
- Remove sync queue table from database
- Remove `supabase` dependency from repositories (keep as local-only)

## 8. Database Migrations

### v3 → v4
```sql
ALTER TABLE transactions ADD COLUMN goalId TEXT;
```

Remove sync_queue table (if keeping sync removal):
```sql
DROP TABLE IF EXISTS sync_queue;
```

## 9. Files to Delete
- `TransactionsScreen.kt`
- `TransactionsViewModel.kt`
- `NumericKeypad.kt` (already deleted)
- `SyncWorker.kt`
- `SyncManager.kt`
- `SyncQueueEntity.kt`
- `SyncQueueDao.kt`

## 10. Files to Create
- `RecurringWorker.kt` (WorkManager worker)
- `AccountDetailScreen.kt` (per-account transactions)
- `GoalContributeSheet.kt` (add money to goal)

## 11. Testing
- Unit tests for `calculateNextRunDate()`
- Unit tests for budget spending calculation
- Unit tests for goal progress calculation
- Manual test: create recurring → wait/check if transactions auto-created
- Manual test: create budget → add expenses → verify progress bar
- Manual test: create goal → add money → verify progress updates
