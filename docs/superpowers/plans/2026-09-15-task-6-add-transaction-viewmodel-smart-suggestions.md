# Task 6: Update AddTransactionViewModel with Smart Suggestions Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add category tree, quick suggestions, and monthly spend state flows to AddTransactionViewModel

**Architecture:** Extend existing ViewModel with three new state flows that load data from GetCategoriesUseCase on initialization

**Tech Stack:** Kotlin, Jetpack Compose, ViewModel, StateFlow, Coroutines

## Global Constraints

- Follow existing code patterns in the project
- Maintain backward compatibility with existing expenseCategories and incomeCategories
- Use the same dependency injection pattern via AppContainer
- Ensure build compiles successfully

---

### Task 1: Add new state flows to AddTransactionViewModel

**Files:**
- Modify: `android/app/src/main/java/com/example/bluff/ui/addtransaction/AddTransactionViewModel.kt`

**Interfaces:**
- Consumes: Updated `GetCategoriesUseCase` from Task 4 with `getCategoryTree()` and `getQuickSuggestions()` methods
- Produces: `categoryTree`, `quickSuggestions`, `monthlySpend` state flows

- [ ] **Step 1: Add QuickSuggestions import**

Add import for QuickSuggestions class from GetCategoriesUseCase.kt

```kotlin
import com.example.bluff.domain.usecase.category.QuickSuggestions
```

- [ ] **Step 2: Add new state flows**

Add three new private MutableStateFlow and public StateFlow properties after existing state flows

```kotlin
private val _categoryTree = MutableStateFlow<List<Category>>(emptyList())
val categoryTree: StateFlow<List<Category>> = _categoryTree.asStateFlow()

private val _quickSuggestions = MutableStateFlow<QuickSuggestions?>(null)
val quickSuggestions: StateFlow<QuickSuggestions?> = _quickSuggestions.asStateFlow()

private val _monthlySpend = MutableStateFlow<Map<String, Long>>(emptyMap())
val monthlySpend: StateFlow<Map<String, Long>> = _monthlySpend.asStateFlow()
```

- [ ] **Step 3: Update init block to load new data**

Add code to init block to load category tree, quick suggestions, and monthly spend

```kotlin
init {
    viewModelScope.launch {
        // Load category tree
        _categoryTree.value = getCategoriesUseCase.getCategoryTree()

        // Load smart suggestions
        val suggestions = getCategoriesUseCase.getQuickSuggestions()
        _quickSuggestions.value = suggestions
        _monthlySpend.value = suggestions.monthlySpend
    }
}
```

Note: expenseCategories and incomeCategories are already loaded via stateIn in the existing code, so no additional collection is needed.

- [ ] **Step 4: Verify build compiles**

Run: `export JAVA_HOME=/usr/lib/jvm/java-17-openjdk && cd /home/kenz/Projects/apps/Bluff/android && ./gradlew assembleDebug`

Expected: Build succeeds without errors

- [ ] **Step 5: Commit changes**

```bash
git add -A
git commit -m "feat: add category tree and smart suggestions to AddTransactionViewModel"
```

## Self-Review

**1. Spec coverage:** The task brief specifies adding three state flows and loading them in init block. This plan covers all requirements.

**2. Placeholder scan:** No placeholders found - all steps contain actual code.

**3. Type consistency:** Types match existing code patterns and GetCategoriesUseCase return types.