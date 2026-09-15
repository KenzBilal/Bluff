# Calendar Feature Design

## Overview

Add a Calendar tab to the bottom navigation that shows a monthly calendar view with transaction indicators. Tapping any date opens a separate detail page showing all transactions for that day in chronological order.

## Goals

1. **Month view calendar** with dots on days that have transactions
2. **Day detail page** — separate screen showing all transactions for the selected day
3. **Daily summary** — income, expense, net for the day
4. **Chronological transaction list** — category icon, name, amount, note
5. **Month navigation** — left/right arrows to browse months

## Layout

### Calendar Tab (Month View)

```
┌─────────────────────────────────┐
│  ← September 2026 →            │  ← month navigation
│  Mon Tue Wed Thu Fri Sat Sun   │
│      1   2   3   4   5   6     │
│  7   8   9  10  11  12  13     │  ← dots on days with transactions
│  14 [15] 16  17  18  19  20     │  ← selected day highlighted
│  21  22  23  24  25  26  27     │
│  28  29  30                    │
│                                 │
│  Today: Sep 15                  │
│  Expense: ₹450 | Income: ₹0    │
└─────────────────────────────────┘
```

### Day Detail Page (Separate Screen)

```
┌─────────────────────────────────┐
│  ← Tuesday, Sep 15             │
│                                 │
│  ── Summary ─────────────────── │
│  Income:  ₹0                    │
│  Expense: ₹450                  │
│  Net:     -₹450                 │
│                                 │
│  ── Expenses (4) ────────────── │
│  ☕ Tea/Coffee      ₹40  note   │
│  🍛 Lunch          ₹80          │
│  🚌 Bus            ₹20          │
│  🛒 Groceries     ₹310  weekly  │
│                                 │
│  ── Income (0) ──────────────── │
│  No income recorded             │
│                                 │
│  ── Transfers (0) ───────────── │
│  No transfers recorded          │
└─────────────────────────────────┘
```

## Architecture

### Files to Create/Modify

1. **Create:** `ui/calendar/CalendarScreen.kt` — Month view with calendar grid
2. **Create:** `ui/calendar/CalendarViewModel.kt` — Load transactions by month, load day details
3. **Create:** `ui/calendar/DayDetailScreen.kt` — Separate page showing day transactions
4. **Modify:** `Navigation.kt` — Add Calendar tab to bottom nav, add day detail route
5. **Modify:** `NavigationKeys.kt` — Add DayDetail key

### Data Flow

1. CalendarScreen loads all transactions for the selected month
2. For each day, calculate daily totals (income/expense/net)
3. Show dots on days with transactions
4. On day tap → navigate to DayDetailScreen with date parameter
5. DayDetailScreen loads all transactions for that specific date

### Queries Needed

Add to `TransactionDao`:
```kotlin
@Query("SELECT * FROM transactions WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate ASC, createdAt ASC")
fun getTransactionsBetween(startDate: String, endDate: String): Flow<List<TransactionEntity>>

@Query("SELECT * FROM transactions WHERE transactionDate = :date ORDER BY createdAt ASC")
fun getTransactionsByDate(date: String): Flow<List<TransactionEntity>>
```

### Navigation

Bottom nav tabs: Home, Calendar, Ledger, Analytics, More

Calendar tab opens `CalendarScreen`. Tapping a day navigates to `DayDetailScreen` with the date as a parameter. Back button returns to calendar.

## Constraints

- Use existing theme colors (Background, TextPrimary, CardColor, etc.)
- Use existing entity ↔ domain ↔ DTO mapping
- Follow existing Compose patterns (ModalBottomSheet, Scaffold, etc.)
- Money stored as Long (minor units/paise)
- Display using `toDisplayAmount()` extension function
