# Smart Expense Cycle Tracking — Design Spec

## Overview

When you add an expense, optionally attach a **cycle** — a reminder tracking when this expense is due again. App learns spending rhythms and nudges you before things are due.

## Core Rules

1. **One cycle per category** — no duplicates. If you add a haircut expense while a haircut cycle exists, it resets the existing cycle.
2. **Early payment resets** — if you pay before due date, cycle resets from today (new nextDueDate = today + cycleDays).
3. **Clean UI** — minimal, not cluttered. Maximum 5 items visible on Home. Full list in More → Cycles.

## Data Model

### `expense_cycles` table

| Field | Type | Description |
|-------|------|-------------|
| `id` | String (PK) | UUID |
| `categoryId` | String | Links to category (unique — one cycle per category) |
| `name` | String | Display name (e.g., "Haircut") |
| `amountMinor` | Long | Last known amount (for reference) |
| `cycleDays` | Int | Duration in days (e.g., 30, 28, 90) |
| `lastTransactionDate` | String | Date of last payment (ISO yyyy-MM-dd) |
| `nextDueDate` | String | Calculated next due date |
| `isActive` | Boolean | Whether tracking is active |
| `createdAt` | Long | Timestamp |
| `updatedAt` | Long | Timestamp |

**Unique constraint on `categoryId`** — only one active cycle per category.

### `category_cycle_defaults` table (pre-seeded)

| Category | Default Cycle |
|----------|--------------|
| Haircut | 30 days |
| Shaving | 15 days |
| Car Service | 180 days |
| Insurance | 365 days |
| Electricity | 30 days |
| Gas Cylinder | 60 days |
| Internet | 30 days |
| Mobile Recharge | 28 days (user picks: 14/28/56/90) |

Categories not in this list = no default, user must pick duration.

## Flow

### Adding a Cycle

1. User adds expense → saves transaction
2. Bottom sheet appears: "Track this expense?"
3. If category has default → show "Haircut — every 30 days" with edit option
4. If no default → show duration picker (presets: 7, 14, 21, 28, 30, 60, 90, 180, 365 + custom)
5. User confirms → cycle created, `nextDueDate = transactionDate + cycleDays`
6. User skips → no cycle created

### Resetting a Cycle

When user marks a cycle as "paid":
- `lastTransactionDate = today`
- `nextDueDate = today + cycleDays`
- This handles early payments correctly

### Duplicate Prevention

If a cycle already exists for the category:
- Show "Update existing cycle?" instead of "Create new?"
- User can update the duration or reset the date
- Never create a second cycle for the same category

### Daily Worker Check

`CycleReminderWorker` runs daily:
- Queries cycles where `nextDueDate <= today + 2 days`
- Sends notification for each due item
- Notification: "Your {name} is due in {N} days" (or "tomorrow", "today", "overdue by {N} days")

## Home Screen

New **"Upcoming"** section after Budget card, before Recent Transactions:
- Maximum 5 items, sorted by due date
- Each item: category icon + name + countdown ("in 2 days", "tomorrow", "overdue")
- Tap → opens Cycles screen
- Empty state: hidden (no card shown when no upcoming cycles)

## Cycles Screen (More → Cycles)

Full list of all active cycles:
- Sorted by next due date (most urgent first)
- Each card: icon, name, cycle duration badge ("every 30 days"), countdown
- Swipe right → mark as paid (resets cycle)
- Tap → edit bottom sheet (change duration)
- Long press → deactivate option
- Filter chips: All / Due Soon (≤7 days) / Overdue

## UI Style

- Follow existing Bluff design: dark theme, CardColor backgrounds, Primary accent
- Countdown colors: green (>7 days), orange (≤7 days), red (overdue)
- Minimal — don't fill the screen, lots of whitespace
- Use existing BluffCard, BluffChip components

## Database Migration

v5 → v6:
```sql
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
);
CREATE UNIQUE INDEX idx_expense_cycles_category ON expense_cycles(categoryId) WHERE isActive = 1;

CREATE TABLE category_cycle_defaults (
    categoryId TEXT PRIMARY KEY NOT NULL,
    categoryName TEXT NOT NULL,
    defaultCycleDays INTEGER NOT NULL
);
```

Pre-seed `category_cycle_defaults` with known categories.

## Files to Create

- `data/local/entity/ExpenseCycleEntity.kt`
- `data/local/entity/CategoryCycleDefaultEntity.kt`
- `data/local/dao/ExpenseCycleDao.kt`
- `data/local/dao/CategoryCycleDefaultDao.kt`
- `data/repository/ExpenseCycleRepositoryImpl.kt`
- `domain/model/ExpenseCycle.kt`
- `domain/usecase/cycle/` — AddCycleUseCase, GetCyclesUseCase, MarkCyclePaidUseCase, DeactivateCycleUseCase
- `ui/cycles/CyclesScreen.kt`
- `ui/cycles/CyclesViewModel.kt`
- `ui/cycles/CycleSetupSheet.kt` (post-transaction bottom sheet)
- `data/sync/CycleReminderWorker.kt`

## Files to Modify

- `BluffDatabase.kt` — add tables, migration v5→v6
- `AppContainer.kt` — wire repositories and use cases
- `Navigation.kt` — add CyclesKey entry
- `NavigationKeys.kt` — add CyclesKey
- `MoreScreen.kt` — add Cycles item
- `HomeScreen.kt` — add Upcoming section
- `HomeViewModel.kt` — load upcoming cycles
- `BluffApplication.kt` — register CycleReminderWorker
- `AddTransactionSheet.kt` — show cycle setup after save
- `AddTransactionViewModel.kt` — handle cycle creation

## Testing

- Unit tests for cycle date calculations
- Unit tests for duplicate prevention logic
- Manual test: add expense → set cycle → verify Home shows upcoming
- Manual test: mark as paid → verify cycle resets
- Manual test: add same category expense → verify existing cycle updates
- Manual test: verify notification fires on due date
