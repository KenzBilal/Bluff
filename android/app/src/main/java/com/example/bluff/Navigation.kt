package com.example.bluff

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.example.bluff.theme.Background
import com.example.bluff.theme.DividerColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.Surface
import com.example.bluff.theme.TextSecondary
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.bluff.di.AppContainer
import com.example.bluff.ui.accounts.AccountsScreen
import com.example.bluff.ui.addtransaction.AddTransactionSheet
import com.example.bluff.ui.analytics.AnalyticsScreen
import com.example.bluff.ui.budget.BudgetScreen
import com.example.bluff.ui.calendar.CalendarScreen
import com.example.bluff.ui.calendar.DayDetailScreen
import com.example.bluff.ui.categories.CategoriesScreen
import com.example.bluff.ui.goals.GoalsScreen
import com.example.bluff.ui.debt.DebtScreen
import com.example.bluff.ui.home.HomeScreen
import com.example.bluff.ui.more.MoreScreen
import com.example.bluff.ui.onboarding.OnboardingScreen
import com.example.bluff.ui.recurring.RecurringScreen
import com.example.bluff.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Composable
fun MainNavigation() {
    val prefs = AppContainer.instance.userPreferencesManager
    val isOnboardingComplete by prefs.isOnboardingComplete
        .collectAsStateWithLifecycle(initialValue = null)

    when (isOnboardingComplete) {
        null -> Box(modifier = Modifier.fillMaxSize()) // loading - splash shows during this
        false -> OnboardingScreen(onComplete = { /* navigation handled inside */ })
        true -> BluffMainApp()
    }
}

@Composable
private fun BluffMainApp() {
    val backStack = rememberNavBackStack(HomeKey)
    var showAddTransaction by remember { mutableStateOf(false) }

    val currentKey = backStack.lastOrNull()
    val isBottomNavVisible = currentKey in setOf(HomeKey, CalendarKey, DebtKey, AnalyticsKey, MoreKey)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = isBottomNavVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring()),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = spring())
            ) {
                BluffBottomNavBar(
                    currentKey = currentKey,
                    onNavigate = { key ->
                        if (backStack.lastOrNull() != key) {
                            // Pop to root if same section, otherwise push
                            val existing = backStack.indexOfFirst { it == key }
                            if (existing >= 0) {
                                while (backStack.size > existing + 1) backStack.removeLastOrNull()
                            } else {
                                // Clear stack back to one of the 4 root tabs then push
                                while (backStack.size > 1) backStack.removeLastOrNull()
                                backStack.add(key)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (isBottomNavVisible) {
                FloatingActionButton(
                    onClick = { showAddTransaction = true },
                    containerColor = Primary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add transaction",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                entryProvider = entryProvider {
                    entry<HomeKey> { HomeScreen(onNavigateToTransactionDetail = {}) }
                    entry<CalendarKey> {
                        CalendarScreen(
                            onDateSelected = { date ->
                                backStack.add(DayDetailKey(date.toString()))
                            }
                        )
                    }
                    entry<DayDetailKey> { key ->
                        DayDetailScreen(
                            date = LocalDate.parse(key.date),
                            onBack = { backStack.removeLastOrNull() }
                        )
                    }
                    entry<AnalyticsKey> { AnalyticsScreen() }
                    entry<DebtKey> { DebtScreen() }
                    entry<MoreKey> {
                        MoreScreen(
                            onNavigateToAccounts = { backStack.add(AccountsKey) },
                            onNavigateToBudgets = { backStack.add(BudgetsKey) },
                            onNavigateToGoals = { backStack.add(GoalsKey) },
                            onNavigateToCategories = { backStack.add(CategoriesKey) },
                            onNavigateToRecurring = { backStack.add(RecurringKey) },
                            onNavigateToSettings = { backStack.add(SettingsKey) }
                        )
                    }
                    entry<AccountsKey> {
                        AccountsScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<BudgetsKey> {
                        BudgetScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<GoalsKey> {
                        GoalsScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<CategoriesKey> {
                        CategoriesScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<RecurringKey> {
                        RecurringScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<SettingsKey> {
                        SettingsScreen(onBack = { backStack.removeLastOrNull() })
                    }
                    entry<OnboardingKey> {
                        OnboardingScreen(onComplete = { backStack.removeLastOrNull() })
                    }
                }
            )

            // Add Transaction Sheet overlaid on top
            if (showAddTransaction) {
                AddTransactionSheet(
                    onDismiss = { showAddTransaction = false }
                )
            }
        }
    }
}

private data class BottomNavItem(
    val key: NavKey,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(HomeKey, "Home", Icons.Default.Home),
    BottomNavItem(CalendarKey, "Calendar", Icons.Default.CalendarMonth),
    BottomNavItem(DebtKey, "Debt", Icons.Default.AccountBalance),
    BottomNavItem(AnalyticsKey, "Analytics", Icons.Default.Analytics),
    BottomNavItem(MoreKey, "More", Icons.Default.MoreHoriz)
)

@Composable
private fun BluffBottomNavBar(
    currentKey: NavKey?,
    onNavigate: (NavKey) -> Unit
) {
    NavigationBar(
        containerColor = Surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(80.dp)
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentKey == item.key
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item.key) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
