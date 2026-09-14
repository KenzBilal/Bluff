# BLUFF --- Personal Finance & Money Manager

## 1. Project Overview

**BLUFF** is a personal finance and money-management Android app
designed to make tracking, understanding, and controlling personal
finances simple.

### Core goals

-   Track income and expenses quickly.
-   Maintain accurate account balances.
-   Set and monitor budgets.
-   Track savings goals.
-   Analyze spending patterns.
-   Manage recurring transactions.
-   Provide useful financial insights.
-   Work offline and keep financial data local by default.
-   Offer extensive customization without making the interface
    complicated.

### Product principle

> Powerful underneath. Simple on the surface.

BLUFF should feel like a personal financial control panel, not a
traditional accounting application.

------------------------------------------------------------------------

# 2. Product Identity

**Name:** BLUFF

**Category:** Personal Finance / Money Manager

**Platform:** Android

**Primary technology:** Kotlin + Jetpack Compose

**Primary theme:** Dark / OLED-friendly

**Design direction:**

-   Minimal
-   Clean
-   Structured
-   Fast
-   Precise
-   User-friendly
-   Highly customizable
-   Low visual clutter

### Positioning

> **Know your money.**

------------------------------------------------------------------------

# 3. Design System

## 3.1 Theme

BLUFF is completely dark by default.

Suggested visual hierarchy:

-   Near-black app background
-   Slightly lighter elevated surfaces
-   White primary text
-   Muted secondary text
-   Subtle dividers
-   One configurable accent color
-   Minimal shadows
-   Controlled use of blur/transparency
-   Consistent corner radius
-   Consistent spacing

Avoid excessive gradients, excessive glassmorphism, unnecessary
decoration, and oversized cards.

## 3.2 Typography

Use a clean Android system font.

Hierarchy:

1.  Large balance / financial numbers
2.  Screen titles
3.  Section titles
4.  Primary labels
5.  Secondary information
6.  Metadata

Numbers must be highly readable.

## 3.3 Components

Create reusable Compose components:

-   `BluffButton`
-   `BluffIconButton`
-   `BluffCard`
-   `BluffTextField`
-   `BluffAmountInput`
-   `BluffChip`
-   `BluffBottomSheet`
-   `BluffDialog`
-   `BluffTopBar`
-   `BluffSectionHeader`
-   `TransactionRow`
-   `AccountCard`
-   `BudgetCard`
-   `GoalCard`
-   `InsightCard`
-   `EmptyState`
-   `LoadingState`

------------------------------------------------------------------------

# 4. Navigation

Use four primary sections:

1.  **Home**
2.  **Transactions**
3.  **Analytics**
4.  **More**

A prominent floating/add action should be available for creating
transactions.

Primary add actions:

-   Expense
-   Income
-   Transfer

Avoid excessive bottom-navigation items.

------------------------------------------------------------------------

# 5. Home Screen

## Purpose

The Home screen answers:

> How much money do I have, and how am I doing financially?

## Content

### Greeting

Example:

`Good evening, Kenz`

### Total balance

``` text
₹8,420.00
Total Balance
```

### Income and spending

``` text
Income       ₹12,000
Spent         ₹3,580
```

### Budget

``` text
September Budget

₹3,580 / ₹6,000 spent

₹2,420 remaining
```

Display a clear progress indicator.

### Quick actions

-   -   Expense
-   -   Income
-   Transfer

### Recent transactions

Group by date.

Example:

``` text
Today

Food
Lunch
−₹120

Transport
Bus
−₹40

Freelance
Website payment
+₹1,500
```

### Customization

Users can choose which Home sections are visible and reorder them.

Possible widgets:

-   Balance
-   Budget
-   Recent transactions
-   Goals
-   Analytics
-   Accounts
-   Quick actions
-   Net worth

------------------------------------------------------------------------

# 6. Add Transaction

This must be one of the fastest workflows in the app.

## Transaction types

-   Expense
-   Income
-   Transfer

## Required fields

-   Amount
-   Type
-   Account
-   Category where applicable
-   Date

## Optional fields

-   Note
-   Time
-   Payment method
-   Tags
-   Attachment / receipt
-   Recurring transaction

## UX

Opening the add button should immediately present the amount input.

Example:

``` text
₹ 0
```

Use a numeric keypad.

Then select:

``` text
Expense | Income | Transfer
```

Then category/account and save.

Primary button:

**Save Transaction**

Avoid multi-screen forms unless necessary.

------------------------------------------------------------------------

# 7. Categories

Default categories:

-   Food
-   Transport
-   Shopping
-   College
-   Entertainment
-   Bills
-   Health
-   Subscriptions
-   Other

Users must be able to:

-   Create categories
-   Rename categories
-   Delete categories where safe
-   Change category icons
-   Change category colors
-   Reorder categories
-   Set category budgets

System categories used by existing transactions should not be
destructively deleted. Offer migration to another category.

------------------------------------------------------------------------

# 8. Transactions Screen

Display a clean financial ledger.

## Features

-   Date grouping
-   Search
-   Filtering
-   Sorting
-   Transaction editing
-   Transaction deletion
-   Category filtering
-   Account filtering
-   Date filtering
-   Amount filtering
-   Income/expense filtering
-   Tag filtering

## Search

``` text
Search transactions...
```

## Transaction interaction

Tap:

``` text
View → Edit / Delete
```

Optional swipe actions:

``` text
Edit | Delete
```

------------------------------------------------------------------------

# 9. Analytics

Analytics should provide useful conclusions, not decorative charts.

## Overview

Show:

-   Total spent
-   Average daily spending
-   Largest expense
-   Total income
-   Total savings
-   Savings rate

## Charts

-   Spending by category
-   Daily spending
-   Weekly spending
-   Monthly comparison
-   Income vs expenses
-   Savings trend

## Insights

Examples:

> Food is your biggest expense this month.

> You spent 24% more this week than last week.

> You have 12 days remaining and ₹2,420 available.

Insights must be based on actual stored data.

------------------------------------------------------------------------

# 10. Budgets

Users can create overall and category budgets.

## Overall budget

Example:

``` text
September
₹6,000
```

## Category budgets

``` text
Food             ₹2,000
Transport          ₹800
Entertainment      ₹500
Shopping           ₹700
```

Track:

-   Amount spent
-   Amount remaining
-   Percentage used
-   Over-budget amount
-   Days remaining

## Warnings

Example:

``` text
Food
82% used
```

When exceeded:

``` text
Food budget exceeded by ₹240
```

Notifications must be configurable.

------------------------------------------------------------------------

# 11. Savings Goals

Users can create goals.

Example:

``` text
MacBook Fund

Target: ₹80,000
Saved: ₹21,500
Progress: 26.8%

Target date: June 2027
```

BLUFF can calculate required periodic savings.

Example:

> Save approximately ₹6,500/month to reach this goal.

Goal types can include:

-   Emergency fund
-   Phone
-   Laptop
-   Travel
-   Education
-   Custom

------------------------------------------------------------------------

# 12. Accounts

Support multiple money sources.

Examples:

``` text
Cash
₹1,200

Bank
₹7,200

UPI Wallet
₹850

Savings
₹20,000
```

Account types may include:

-   Cash
-   Bank account
-   Wallet
-   Savings
-   Custom

## Transfers

Transfers move money between accounts.

Important:

**Transfers are not income or expenses.**

Example:

``` text
Bank → Cash
₹1,000
```

This must not increase total income or spending.

------------------------------------------------------------------------

# 13. Recurring Transactions

Support scheduled recurring transactions.

Examples:

``` text
Spotify
₹119 / month

Mobile recharge
₹299 / month

Hostel
₹X / month
```

Options:

-   Frequency
-   Start date
-   End date
-   Account
-   Category
-   Amount
-   Automatic creation
-   Notification

Recurring transactions must be editable and pausable.

------------------------------------------------------------------------

# 14. Tags

Allow optional tags such as:

-   College
-   Trip
-   Friends
-   Project
-   Food
-   Personal

Tags enable additional filtering and analytics.

------------------------------------------------------------------------

# 15. Customization

Customization is a major BLUFF feature.

## Appearance

Options:

-   Pure Black
-   OLED Black
-   Dark Gray
-   Custom background
-   Accent color
-   Text intensity
-   Compact layout
-   Comfortable layout
-   Corner radius
-   Animation intensity

## Home customization

Users can:

-   Show/hide sections
-   Reorder sections
-   Select default account
-   Select default transaction type
-   Configure quick actions

## Category customization

Users can:

-   Change icons
-   Change colors
-   Rename categories
-   Reorder categories

------------------------------------------------------------------------

# 16. Settings

Settings must be organized into sections.

## Personal

-   Name
-   Currency
-   Country
-   Financial month start

## Appearance

-   Theme
-   Accent color
-   Layout
-   Animations

## Money

-   Default account
-   Default category
-   Budget rules
-   Rounding

## Notifications

-   Budget warnings
-   Daily spending reminder
-   Recurring payment reminders
-   Goal reminders

## Security

-   App lock
-   PIN
-   Biometric authentication
-   Auto-lock duration
-   Hide balance in app switcher

## Data

-   Export CSV
-   Export JSON
-   Import backup
-   Create backup
-   Restore backup
-   Clear all data

## Advanced

-   Decimal precision
-   Date format
-   Week start day
-   Confirmation requirements
-   Developer/debug information

------------------------------------------------------------------------

# 17. Privacy

BLUFF should be **local-first**.

The first version requires:

-   No account
-   No mandatory cloud connection
-   No internet dependency for core features
-   Local database
-   Local calculations

Default principle:

> Your financial data stays on your device.

Future optional feature:

-   Encrypted cloud backup
-   Multi-device synchronization

Cloud functionality must never compromise local functionality.

------------------------------------------------------------------------

# 18. Money Handling

Financial calculations must never depend on floating-point arithmetic.

Do not use `Double` for stored money values.

Use integer minor units.

For INR:

``` text
₹120.50 → 12050 paise
```

For currencies without minor units, store according to the currency
configuration.

Store:

-   Amount in minor units
-   Currency code
-   Transaction type

All calculations must use exact integer arithmetic or appropriate
decimal arithmetic.

------------------------------------------------------------------------

# 19. Data Model

Minimum entities:

``` text
Transaction
Account
Category
Budget
Goal
RecurringTransaction
Tag
AppSettings
```

## Transaction

Suggested fields:

``` text
id
amount
currency
type
accountId
categoryId
note
date
createdAt
updatedAt
```

## Account

Suggested fields:

``` text
id
name
type
currency
initialBalance
isArchived
createdAt
updatedAt
```

## Category

Suggested fields:

``` text
id
name
icon
color
type
isSystem
isArchived
createdAt
updatedAt
```

## Budget

Suggested fields:

``` text
id
name
amount
period
categoryId
startDate
endDate
createdAt
updatedAt
```

## Goal

Suggested fields:

``` text
id
name
targetAmount
currentAmount
targetDate
icon
createdAt
updatedAt
```

## RecurringTransaction

Suggested fields:

``` text
id
amount
type
accountId
categoryId
frequency
nextRunAt
startDate
endDate
isActive
createdAt
updatedAt
```

------------------------------------------------------------------------

# 20. Architecture

Recommended architecture:

``` text
Kotlin
   ↓
Jetpack Compose
   ↓
Navigation
   ↓
ViewModel
   ↓
Use Cases
   ↓
Repository
   ↓
Room Database
```

Use a clean separation between:

-   UI
-   Domain logic
-   Data access

## Suggested project structure

``` text
app/
├── data/
│   ├── database/
│   ├── dao/
│   ├── entities/
│   ├── repository/
│   └── preferences/
│
├── domain/
│   ├── model/
│   └── usecase/
│
├── ui/
│   ├── home/
│   ├── transactions/
│   ├── analytics/
│   ├── budget/
│   ├── goals/
│   ├── accounts/
│   ├── settings/
│   ├── components/
│   └── theme/
│
└── MainActivity.kt
```

------------------------------------------------------------------------

# 21. Recommended Android Technologies

-   Kotlin
-   Jetpack Compose
-   Material 3 where appropriate
-   Room
-   Kotlin Coroutines
-   Flow
-   ViewModel
-   Navigation Compose
-   DataStore for app preferences
-   Android Biometric APIs
-   WorkManager for scheduled local tasks
-   Android notifications
-   Activity Result APIs for file import/export

Keep dependencies minimal.

Do not add libraries when native Android functionality is sufficient.

------------------------------------------------------------------------

# 22. Backup and Export

## CSV Export

Export transaction data with fields such as:

``` text
Date
Type
Amount
Currency
Account
Category
Note
Tags
```

## JSON Backup

JSON should contain the complete local data model required for
restoration.

Backup should include:

-   Accounts
-   Transactions
-   Categories
-   Budgets
-   Goals
-   Recurring transactions
-   Tags
-   Relevant settings

Import must validate the file before modifying the database.

Use transactional database operations during restore.

------------------------------------------------------------------------

# 23. Security Requirements

Financial data is sensitive.

Implement:

-   Biometric app lock
-   PIN fallback
-   Auto-lock
-   Screenshot/app-switcher privacy option
-   Safe database operations
-   Input validation
-   No financial data in debug logs
-   No API keys stored in source code

If cloud sync is introduced later, encryption and authentication must be
designed before implementation.

------------------------------------------------------------------------

# 24. Notifications

Notifications must be useful and optional.

Possible notifications:

### Budget

> Food budget is 80% used.

### Recurring payment

> Spotify payment is due tomorrow.

### Goal

> You are ₹500 away from this month's savings target.

### Daily tracking

> Record today's spending.

Users can individually enable or disable notification types.

------------------------------------------------------------------------

# 25. UX Principles

1.  Every important action should take approximately 2--3 taps.
2.  The primary action on each screen must be obvious.
3.  Avoid unnecessary popups.
4.  Avoid clutter.
5.  Use plain language.
6.  Never hide important financial information.
7.  Confirm destructive actions.
8.  Show useful empty states.
9.  Keep animations subtle and fast.
10. Make the app usable with one hand where practical.
11. Maintain consistent spacing and component behavior.
12. Never sacrifice correctness for visual effects.

------------------------------------------------------------------------

# 26. Accessibility

Support:

-   Dynamic font sizing
-   Sufficient text contrast
-   Screen-reader labels
-   Large touch targets
-   Reduced motion
-   Clear error states
-   Semantic icons
-   Non-color-only financial indicators

Do not communicate important financial status through color alone.

------------------------------------------------------------------------

# 27. Error Handling

Every operation must have clear states:

-   Loading
-   Success
-   Empty
-   Error

Examples:

``` text
Couldn't save transaction.
Try again.
```

``` text
No transactions found.
```

``` text
Backup file is invalid.
```

Never silently fail financial operations.

------------------------------------------------------------------------

# 28. Database Rules

The database is the source of truth.

Requirements:

-   Foreign-key integrity
-   Appropriate indexes
-   Transaction-safe writes
-   No duplicate recurring transactions
-   Safe deletion rules
-   Archived categories/accounts where historical data depends on them
-   Database migrations for schema changes

Do not permanently delete historical data when doing so would corrupt
reports.

------------------------------------------------------------------------

# 29. Calculations

Core calculations include:

### Balance

``` text
Balance =
Initial Balance
+ Income
- Expenses
+ Incoming Transfers
- Outgoing Transfers
```

### Budget remaining

``` text
Budget Remaining =
Budget Amount - Eligible Expenses
```

### Savings

``` text
Savings =
Total Income - Total Expenses
```

### Savings rate

``` text
Savings Rate =
Savings / Total Income × 100
```

Handle zero-income cases safely.

------------------------------------------------------------------------

# 30. Dashboard Insights

Insights should be generated from real user data.

Possible logic:

-   Highest spending category
-   Highest spending day
-   Weekly spending change
-   Monthly spending change
-   Budget risk
-   Savings progress
-   Unusual spending
-   Upcoming recurring payments
-   Goal progress

Do not generate misleading insights when insufficient data exists.

------------------------------------------------------------------------

# 31. MVP

Build the first version with only:

1.  Home
2.  Add income
3.  Add expense
4.  Add transfer
5.  Transaction history
6.  Categories
7.  Accounts
8.  Monthly budget
9.  Basic analytics
10. Settings
11. Local persistence

The MVP must be completely functional before adding advanced features.

------------------------------------------------------------------------

# 32. Phase 2

Add:

-   Savings goals
-   Recurring transactions
-   Tags
-   Advanced filters
-   Better analytics
-   Spending insights
-   Notifications
-   CSV export
-   JSON backup/restore
-   Biometric lock
-   Home customization

------------------------------------------------------------------------

# 33. Phase 3

Possible advanced features:

-   Encrypted cloud backup
-   Multi-device synchronization
-   Advanced financial reports
-   Net worth tracking
-   Subscription detection
-   Smart categorization
-   Advanced recurring rules
-   Custom dashboards
-   Financial trends
-   More sophisticated insights

These are optional. Do not allow them to complicate the core experience.

------------------------------------------------------------------------

# 34. Development Order

Build in this order:

### Step 1 --- Project setup

-   Android project
-   Kotlin
-   Jetpack Compose
-   Theme
-   Navigation
-   Basic architecture

### Step 2 --- Database

-   Room
-   Entities
-   DAOs
-   Repository
-   Database migrations

### Step 3 --- Accounts

-   Create account
-   Edit account
-   Archive account
-   Balance calculation

### Step 4 --- Categories

-   Default categories
-   Custom categories
-   Category management

### Step 5 --- Transactions

-   Expense
-   Income
-   Transfer
-   Edit
-   Delete
-   Search
-   Filter

### Step 6 --- Home

-   Balance
-   Income
-   Expenses
-   Recent transactions
-   Budget

### Step 7 --- Budget

-   Create budget
-   Track spending
-   Remaining amount
-   Warnings

### Step 8 --- Analytics

-   Category breakdown
-   Daily spending
-   Monthly summary
-   Income vs expenses

### Step 9 --- Settings

-   Appearance
-   Currency
-   Notifications
-   Security
-   Data management

### Step 10 --- Advanced

-   Goals
-   Recurring transactions
-   Tags
-   Backup
-   Insights
-   Customization

------------------------------------------------------------------------

# 35. Testing

Test financial correctness heavily.

## Unit tests

Test:

-   Balance calculations
-   Budget calculations
-   Savings calculations
-   Transfers
-   Recurring transactions
-   Currency handling
-   Date ranges
-   Analytics
-   Goal calculations

## UI tests

Test:

-   Add expense
-   Add income
-   Transfer
-   Edit transaction
-   Delete transaction
-   Budget creation
-   Account creation
-   Settings changes

## Edge cases

Test:

-   ₹0 transactions if allowed
-   Very large amounts
-   Negative values
-   Same-day transactions
-   Month boundaries
-   Leap years
-   Deleted/archived categories
-   Deleted/archived accounts
-   Empty database
-   Invalid backups
-   Duplicate recurring transactions

------------------------------------------------------------------------

# 36. Performance

BLUFF should feel instant.

Requirements:

-   Local database reads
-   Efficient Flow observation
-   Lazy lists for transactions
-   Minimal recomposition
-   Avoid expensive calculations on every UI frame
-   Cache or precompute expensive analytics when appropriate
-   No unnecessary network requests

------------------------------------------------------------------------

# 37. Final Product Standard

BLUFF is successful when:

-   Adding an expense feels effortless.
-   The balance is always accurate.
-   Users immediately understand their financial position.
-   Analytics answer real questions.
-   Budgets are easy to create and monitor.
-   Customization never creates clutter.
-   The app works without an internet connection.
-   Financial data is treated as sensitive.
-   The interface feels premium without being complicated.

## Core philosophy

> **Simple enough to use every day. Powerful enough to manage your
> entire personal financial life.**

**BLUFF --- Know your money.**
