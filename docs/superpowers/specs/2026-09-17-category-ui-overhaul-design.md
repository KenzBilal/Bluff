# Category UI Overhaul — Design Spec

## Overview

Overhaul the category management system to use professional Material Symbols vector icons, a categorized icon picker with search, an expanded color palette, and a polished UI throughout the app.

## Current State

- Icons: 24 hardcoded emojis stored as strings, displayed as `Text()`
- Icon picker: 8-column grid of emojis, no search, no categories
- Color picker: 20 hardcoded colors, no custom hex input
- Categories screen: basic card list, no delete functionality
- No undo, no confirmation dialogs, limited polish

## Target State

### 1. Icon System — Material Symbols

- Icons stored as Material Symbol names (e.g., `"restaurant"`, `"directions_car"`)
- Displayed via `Icon(Icons.Rounded.Restaurant, ...)` vector composables
- Backward compatible: existing emoji categories continue to work via emoji fallback
- New field: `iconType: String` — `"material"` for new icons, `"emoji"` for legacy
- No DB migration needed — `icon` field stores symbol name or emoji string

### 2. Icon Picker — Categorized Grid with Search

**Layout:**
- Search bar at top (filters icons by name in real-time)
- Horizontal scrollable category tabs: All / Money / Transport / Food / Home / Health / Leisure / Shopping / Other
- Grid of icons (5 columns), scrollable, filtered by selected category
- Selected icon: Primary-colored circle background
- Tap icon to select, tap again to deselect

**Icon Categories (Material Symbol names):**

| Category | Icons |
|----------|-------|
| Money | `payments`, `account_balance`, `savings`, `credit_card`, `money`, `attach_money`, `monetization_on`, `receipt`, `wallet`, `paid`, `trending_up`, `show_chart`, `currency_exchange`, `request_quote` |
| Transport | `directions_car`, `directions_bus`, `flight`, `train`, `local_taxi`, `bike_scooter`, `directions_boat`, `local_shipping`, `gas_station`, `ev_station`, `parking`, `traffic`, `route`, `map` |
| Food | `restaurant`, `local_cafe`, `local_bar`, `bakery_dining`, `lunch_dining`, `dinner_dining`, `icecream`, `local_pizza`, `ramen_dining`, `liquor`, `coffee`, `egg_alt`, `kebab_dining`, `brunch_dining` |
| Home | `home`, `apartment`, `villa`, `cottage`, `house`, `roofing`, `plumbing`, `electrical_services`, `hardware`, `cleaning_services`, `laundry`, `dry_cleaning`, `pest_control`, `yard` |
| Health | `local_hospital`, `medical_services`, 'medication', `vaccines`, `health_and_safety', `fitness_center`, `spa', `psychology', `visibility', `bloodtype', `monitor_heart', `healing', `emoji_people', `self_improvement` |
| Leisure | `sports_esports', `movie', `music_note', `theaters', `sports_soccer', `pool', `hiking', `camping', `skateboarding', `surfing', `sports_tennis', `sports_basketball', `emoji_events', `celebration` |
| Shopping | `shopping_cart', `shopping_bag', `store', `local_mall', `checkroom', `shoe', `watch', `jewelry', `photo_camera', `devices', `phone_iphone', `laptop', `headphones', `toys` |
| Other | `school', `flight_takeoff', `beach_access', `pets', `child_care', `elderly', `groups', `volunteer_activism', `church', `mosque', `temple_hindu', `synagogue`, `diversity_3`, `translate` |

**Total: ~150+ icons**

### 3. Categories Screen — Polished Card List

- Scaffold with top app bar ("Categories" + back arrow)
- LazyColumn of category cards
- Each card:
  - 48dp circle with Material Symbol icon (or emoji fallback) on `color.copy(alpha=0.15f)` background
  - Category name (16sp, SemiBold)
  - Type badge: small chip showing "Expense" / "Income" / "Both"
  - Tap → open AddEditCategorySheet
  - Long-press → show delete confirmation dialog
- FAB (+) → open AddEditCategorySheet for new category
- Empty state: illustration + "No categories yet" message + "Create one" button

### 4. Add/Edit Category Sheet — Cleaner Layout

**Fields:**
1. **Name** — BluffTextField
2. **Type** — Row of BluffChip (Expense / Income / Both)
3. **Parent** — ExposedDropdownMenuBox (root categories only, exclude self + descendants)
4. **Icon** — Tappable icon preview → opens full IconPickerSheet
5. **Color** — Row of 30+ color circles organized by hue families, plus custom hex input

**IconPickerSheet (sub-sheet):**
- Search bar
- Category tabs (horizontal scroll)
- Icon grid (5 columns)
- Selected icon highlighted with Primary circle

**Color Palette (30+ colors):**
- Reds: `#E53935`, `#D32F2F`, `#C62828`, `#FF5252`
- Oranges: `#FF9800`, `#F57C00`, `#EF6C00`, `#FFB74D`
- Yellows: `#FDD835`, `#FBC02D`, `#F9A825`, `#FFEE58`
- Greens: `#4CAF50`, `#388E3C`, `#2E7D32`, `#66BB6A`
- Teals: `#009688`, `#00796B`, `#004D40`, `#26A69A`
- Blues: `#2196F3`, `#1976D2`, `#1565C0`, `#42A5F5`
- Indigos: `#3F51B5`, `#303F9F`, `#1A237E`, `#5C6BC0`
- Purples: `#9C27B0`, `#7B1FA2`, `#6A1B9A`, `#AB47BC`
- Pinks: `#E91E63`, `#C2185B`, `#AD1457`, `#EC407A`
- Grays: `#9E9E9E`, `#757575`, `#616161`, `#424242`, `#212121`
- Custom: hex input field with preview circle

**Buttons:**
- Save (Create / Update)
- Delete (only in edit mode, with confirmation dialog)

### 5. Category Display Everywhere

- **TransactionRow:** Material Symbol icon in category color circle
- **CategoryTreePicker:** Material Symbol icons in tree rows
- **CategoryPickerGrid:** Material Symbol icons in grid
- **Home budget/goal cards:** Material Symbol icons for category references

### 6. Delete Category

- Long-press on category card → confirmation dialog
- "Delete '{name}'? This cannot be undone."
- System categories (`isSystem = true`): show warning "System categories can be hidden but not deleted"
- Categories with transactions: show warning "This category has X transactions. Deleting will unlink them."
- Use existing `CategoryDao.deleteCategory(id)` method

## Migration Strategy

- **No DB migration required** — `icon` field already stores a String
- Existing emoji categories: `iconType = "emoji"` (or null for legacy)
- New categories: `iconType = "material"`, `icon` stores Material Symbol name
- Display logic: check `iconType` → if `"material"`, render as `Icon()`; else render as `Text()` (emoji)
- Seed data update: update `DefaultDataSeeder` to use Material Symbols for new installs

## Files to Create

- `ui/categories/IconPickerSheet.kt` — Full categorized icon picker
- `ui/categories/ColorPickerSection.kt` — Expanded color palette with hex input
- `ui/categories/CategoryIcon.kt` — Reusable composable for rendering category icons (vector or emoji fallback)

## Files to Modify

- `ui/categories/CategoriesScreen.kt` — Polish cards, add long-press delete, empty state
- `ui/categories/AddEditCategorySheet.kt` — Integrate new icon picker, color picker, delete button
- `ui/categories/CategoriesViewModel.kt` — Add delete functionality
- `domain/model/Category.kt` — Add `iconType` field
- `data/local/entity/CategoryEntity.kt` — Add `iconType` column
- `data/local/dao/CategoryDao.kt` — No changes needed
- `data/repository/CategoryRepository.kt` — Handle `iconType` in mapping
- `data/seed/DefaultDataSeeder.kt` — Update seed icons to Material Symbols
- `ui/components/TransactionRow.kt` — Use CategoryIcon composable
- `ui/components/CategoryTreePicker.kt` — Use CategoryIcon composable
- `ui/components/CategoryPickerGrid.kt` — Use CategoryIcon composable

## Testing

- Create category with Material Symbol icon → appears correctly in list
- Edit existing emoji category → icon picker shows, can switch to Material Symbol
- Delete category → confirmation dialog, category removed
- Search icons → filters grid in real-time
- Category tabs → filter icons by category
- Custom hex color → applies to category
- Long-press delete on system category → shows warning
- All existing categories still display correctly (emoji fallback)
