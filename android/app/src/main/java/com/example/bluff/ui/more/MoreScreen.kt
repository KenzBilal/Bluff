package com.example.bluff.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.*

private data class MoreNavItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun MoreScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToCycles: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val groups = listOf(
        "Finance" to listOf(
            MoreNavItem("Accounts", "Wallets & cards", Icons.Default.AccountBalanceWallet, onNavigateToAccounts),
            MoreNavItem("Budgets", "Spending limits", Icons.Default.PieChart, onNavigateToBudgets),
            MoreNavItem("Goals", "Savings targets", Icons.Default.Flag, onNavigateToGoals)
        ),
        "Manage" to listOf(
            MoreNavItem("Categories", "Organise transactions", Icons.Default.Category, onNavigateToCategories),
            MoreNavItem("Recurring", "Automated entries", Icons.Default.Repeat, onNavigateToRecurring),
            MoreNavItem("Cycles", "Expense reminders", Icons.Default.Cached, onNavigateToCycles)
        ),
        "App" to listOf(
            MoreNavItem("Settings", "Preferences & data", Icons.Default.Settings, onNavigateToSettings)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            Spacer(Modifier.height(48.dp))
            Text(
                "More",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(28.dp))
        }

        groups.forEach { (groupTitle, items) ->
            item {
                Text(
                    text = groupTitle.uppercase(),
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CardColor,
                    border = BorderStroke(1.dp, DividerColor)
                ) {
                    Column {
                        items.forEachIndexed { i, item ->
                            MoreNavRow(item)
                            if (i < items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 68.dp),
                                    color = DividerColor,
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun MoreNavRow(item: MoreNavItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(SurfaceVariant, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                item.icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                item.subtitle,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}
