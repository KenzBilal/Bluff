# BLUFF: Category Tree + Quick Add Transaction

## Overview

Enhance the add transaction flow with 3-level subcategories, quick-add capabilities, and smart suggestions. The goal is to reduce the number of taps needed to log common expenses while maintaining a clean, professional UI.

## Goals

1. **3-level subcategory support** — Food > Meals > Lunch, Dinner
2. **Quick-add flow** — tap category → amount → save (3-2-1 taps)
3. **Quick amounts per category** — pre-set amounts (Chai ₹40, Auto ₹60)
4. **FAB quick add from home** — instant access to add transaction
5. **Smart suggestions** — time-based, recent categories, monthly spend
6. **Professional, clean UI** — no search, user-friendly

## Architecture: Category Tree

### Model Changes

Add `parentId` and `quickAmounts` to the Category model:

```kotlin
data class Category(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: CategoryType,
    val parentId: String? = null,        // NEW: null = root category
    val quickAmounts: List<Long> = emptyList(), // NEW: pre-set amounts in minor units
    val isSystem: Boolean = false,
    val isArchived: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### Database Migration

```sql
ALTER TABLE categories ADD COLUMN parentId TEXT;
ALTER TABLE categories ADD COLUMN quickAmounts TEXT; -- JSON array of Long
```

### Default Seed Data

```
Food 🍽️ (parentId=null, quickAmounts=[4000, 6000, 8000])
  ├── Meals 🍛 (parentId=foodId)
  │   ├── Breakfast (quickAmounts=[3000, 5000])
  │   ├── Lunch (quickAmounts=[6000, 8000, 10000])
  │   └── Dinner (quickAmounts=[8000, 10000, 12000])
  ├── Drinks ☕ (parentId=foodId)
  │   ├── Tea/Coffee (quickAmounts=[2000, 3000, 4000])
  │   └── Juice (quickAmounts=[3000, 4000])
  └── Snacks 🍿 (parentId=foodId, quickAmounts=[1000, 2000, 3000])

Transport 🚌 (parentId=null, quickAmounts=[4000, 6000, 8000])
  ├── Auto 🛺 (parentId=transportId, quickAmounts=[4000, 6000, 8000])
  ├── Bus 🚌 (parentId=transportId, quickAmounts=[2000, 3000])
  └── Cab 🚕 (parentId=transportId, quickAmounts=[8000, 12000, 16000])

Shopping 🛍️ (parentId=null)
  ├── Groceries (quickAmounts=[20000, 50000, 100000])
  └── Clothes (quickAmounts=[50000, 100000, 200000])

College 📚 (parentId=null)
  ├── Books (quickAmounts=[20000, 50000])
  └── Stationery (quickAmounts=[10000, 20000])

Entertainment 🎮 (parentId=null)
  ├── Movies 🎬 (quickAmounts=[15000, 20000, 30000])
  └── Games 🎮 (quickAmounts=[10000, 20000])

Bills 💡 (parentId=null)
  ├── Electricity (quickAmounts=[50000, 100000, 200000])
  ├── Internet (quickAmounts=[50000, 100000])
  └── Phone (quickAmounts=[20000, 50000])

Health 🏥 (parentId=null)
  ├── Medicine (quickAmounts=[10000, 20000, 50000])
  └── Doctor (quickAmounts=[200000, 500000])

Subscriptions 📱 (parentId=null)
  ├── Netflix (quickAmounts=[20000, 65000])
  └── Spotify (quickAmounts=[10000, 15000])

Salary 💰 (parentId=null, type=INCOME)
Freelance 💻 (parentId=null, type=INCOME)
Other 📦 (parentId=null, type=BOTH)
```

## Quick Add Flow

### FAB on Home Screen

- Floating action button (FAB) on HomeScreen
- Tap opens compact bottom sheet
- Same sheet used for full add transaction, but starts in "quick mode"

### Compact Quick-Add Sheet

```
┌─────────────────────────┐
│  + Add Expense          │
│                         │
│  [Food ▼] [Transport ▼] │  ← parent categories as chips
│                         │
│  ☕ Tea ₹40  🍛 Lunch ₹80 │  ← quick amount chips
│  🍿 Snack ₹20  🚕 Cab ₹60 │  ← with pre-set amounts
│  ⋮ More                  │  ← expands to show all subcategories
│                         │
│  ₹ [    40.00    ]      │  ← editable amount
│  [Save]                  │
└─────────────────────────┘
```

### Interaction Flow

1. **Tap FAB** → compact sheet opens
2. **Tap quick-amount chip** (e.g., "☕ Tea ₹40") → amount auto-fills, category set
3. **Tap Save** → done, sheet closes (3 taps total)

**Without quick amount:**
1. Tap FAB → compact sheet
2. Tap parent category → expands
3. Tap subcategory → expands
4. Tap leaf category → amount keypad opens
5. Type amount → Save (5 taps, but rare for common spends)

## Smart Suggestions

### A. Time-Based Suggestions

Query time ranges and show relevant categories first:

| Time | Suggested First |
|------|-----------------|
| 6am-11am | Breakfast, Tea/Coffee |
| 11am-4pm | Lunch |
| 4pm-9pm | Dinner, Snacks |
| 9pm-6am | Entertainment |

### B. Recent Categories Priority

- Query last 10 transactions
- Show recently used categories at top of "Quick" section
- Combine with time-based (e.g., "Lunch" appears first in afternoon AND was used yesterday)

### C. Monthly Spend Per Category

- When showing category chips, display: "Lunch ₹1,200" (amount spent this month)
- Color-code the spend amount:
  - Green: < ₹500
  - Yellow: ₹500-2000
  - Red: > ₹2000

### Display Order in Picker

```
⚡ Quick (time-based + recent)
  ☕ Tea ₹40    ← monthly: ₹320
  🍛 Lunch ₹80  ← monthly: ₹1,200

📁 Food
  🍛 Meals ▶
  ☕ Drinks ▶
  🍿 Snacks ▶

🚌 Transport ▶
🛍️ Shopping ▶
📚 College ▶
🎮 Entertainment ▶
💡 Bills ▶
🏥 Health ▶
📱 Subscriptions ▶
```

## Full Add Transaction Sheet (Enhanced)

The existing sheet gets upgraded with the new category picker:

```
┌─────────────────────────┐
│  ₹ 150.00               │  ← amount display
│                         │
│  [Expense] [Income] [Transfer] │
│                         │
│  Account: Cash ▼        │
│                         │
│  Category:              │
│  ⚡ Quick: ☕ Tea ₹40   │
│  ⚡ Quick: 🍛 Lunch ₹80│
│                         │
│  🍽️ Food ▶              │  ← tap to expand
│  🚌 Transport ▶         │
│  🛍️ Shopping ▶          │
│  📚 College ▶           │
│  🎮 Entertainment ▶     │
│  💡 Bills ▶             │
│  🏥 Health ▶            │
│  📱 Subscriptions ▶     │
│                         │
│  Note: [lunch with friends] │
│                         │
│  [7] [8] [9]            │
│  [4] [5] [6]            │
│  [1] [2] [3]            │
│  [.] [0] [⌫]           │
│                         │
│  [Save Transaction]     │
└─────────────────────────┘
```

**Key improvements:**
- Quick suggestions at top (time-based + recent)
- Monthly spend shown on each category
- Nested expand/collapse for subcategories
- Same familiar layout, just smarter

## Files to Modify

### Data Layer
1. `Category.kt` — Add `parentId: String?` and `quickAmounts: List<Long>`
2. `CategoryEntity.kt` — Add fields + migration
3. `CategoryDto.kt` — Add fields
4. `CategoryDao.kt` — Add queries for children, recent, monthly spend
5. `CategoryRepository.kt` — Add `getChildren()`, `getRecentCategories()`, `getMonthlySpendByCategory()`
6. `DefaultDataSeeder.kt` — Seed with subcategories and quick amounts

### Domain Layer
7. `GetCategoriesUseCase.kt` — Add `getCategoryTree()`, `getQuickSuggestions()`
8. `TransactionAmountFormatter.kt` — May need updates for display

### UI Layer
9. `AddTransactionSheet.kt` — Enhanced category picker with tree + quick amounts
10. `AddTransactionViewModel.kt` — Smart suggestions logic
11. `HomeScreen.kt` — Add FAB for quick add
12. `CategoryPickerGrid.kt` — Replace with new hierarchical picker

### Theme/Components
13. Possibly new `CategoryTreePicker.kt` component

## Constraints

- Keep manual DI (AppContainer)
- Money stored as Long (minor units/paise)
- Dark/OLED theme (Background, TextPrimary, CardColor)
- No search feature
- Professional, clean UI
- Follow existing entity ↔ domain ↔ DTO tri-layer mapping
