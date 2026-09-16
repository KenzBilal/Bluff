package com.example.bluff.ui.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.*

@Composable
fun MoreScreen(
    onNavigateToAccounts: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToRecurring: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDebt: () -> Unit
) {
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
            Spacer(Modifier.height(24.dp))
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                MoreItem("Accounts", Icons.Default.AccountBalanceWallet, onNavigateToAccounts)
                Spacer(Modifier.height(12.dp))
                MoreItem("Debt", Icons.Default.AccountBalance, onNavigateToDebt)
                Spacer(Modifier.height(12.dp))
                MoreItem("Budgets", Icons.Default.PieChart, onNavigateToBudgets)
                Spacer(Modifier.height(12.dp))
                MoreItem("Goals", Icons.Default.Flag, onNavigateToGoals)
                Spacer(Modifier.height(12.dp))
                MoreItem("Categories", Icons.Default.Category, onNavigateToCategories)
                Spacer(Modifier.height(12.dp))
                MoreItem("Recurring", Icons.Default.Repeat, onNavigateToRecurring)
                Spacer(Modifier.height(12.dp))
                MoreItem("Settings", Icons.Default.Settings, onNavigateToSettings)
            }
        }
    }
}

@Composable
private fun MoreItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Primary)
            }
            Spacer(Modifier.width(16.dp))
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
