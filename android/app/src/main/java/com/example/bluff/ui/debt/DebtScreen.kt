package com.example.bluff.ui.debt

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Debt
import com.example.bluff.domain.model.DebtDirection
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import kotlin.math.abs
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtScreen(
    viewModel: DebtViewModel = viewModel(factory = DebtViewModel.Factory),
    onNavigateToDetail: (String) -> Unit
) {
    val theyOweMe by viewModel.theyOweMeNetDebts.collectAsStateWithLifecycle()
    val iOwe by viewModel.iOweNetDebts.collectAsStateWithLifecycle()
    val totalTheyOweMe by viewModel.totalTheyOweMe.collectAsStateWithLifecycle()
    val totalIOwe by viewModel.totalIOwe.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column {
            Spacer(Modifier.height(48.dp))
            Text(
                "Debts",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Summary cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DebtSummaryCard(
                            label = "They Owe Me",
                            amount = totalTheyOweMe,
                            color = IncomeColor,
                            modifier = Modifier.weight(1f)
                        )
                        DebtSummaryCard(
                            label = "I Owe",
                            amount = totalIOwe,
                            color = ExpenseColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // They Owe Me
                if (theyOweMe.isNotEmpty()) {
                    item {
                        SectionHeader(title = "They Owe Me", count = theyOweMe.size)
                    }
                    items(theyOweMe, key = { it.contactName }) { netDebt ->
                        NetDebtCard(
                            netDebt = netDebt,
                            onClick = { onNavigateToDetail(netDebt.contactName) }
                        )
                    }
                }

                // I Owe
                if (iOwe.isNotEmpty()) {
                    item {
                        SectionHeader(title = "I Owe", count = iOwe.size)
                    }
                    items(iOwe, key = { it.contactName }) { netDebt ->
                        NetDebtCard(
                            netDebt = netDebt,
                            onClick = { onNavigateToDetail(netDebt.contactName) }
                        )
                    }
                }

                // Empty state
                if (theyOweMe.isEmpty() && iOwe.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("\uD83E\uDD1D", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No active debts",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap + to record money lent or borrowed",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryCard(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount.toDisplayAmount(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.15f)
        ) {
            Text(
                text = "$count",
                color = Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetDebtCard(
    netDebt: NetDebt,
    onClick: () -> Unit
) {
    val isTheyOwe = netDebt.netBalanceMinor > 0
    val amountColor = if (isTheyOwe) IncomeColor else ExpenseColor

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = netDebt.contactName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = netDebt.contactName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${netDebt.debts.size} transactions",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = abs(netDebt.netBalanceMinor).toDisplayAmount(),
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
