package com.example.bluff.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Budget
import com.example.bluff.theme.*
import com.example.bluff.ui.components.*
import com.example.bluff.ui.util.toDisplayAmount

@Composable
fun HomeScreen(
    onNavigateToTransactionDetail: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val greeting by viewModel.greeting.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val income by viewModel.monthlyIncome.collectAsState()
    val spent by viewModel.monthlySpent.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val activeBudget by viewModel.activeBudget.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Hero Balance Card ──────────────────────────────────────────────
        item {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 })
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF2A1F6E), Color(0xFF1A1040))
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = greeting,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Total Balance",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = totalBalance.toDisplayAmount(),
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Stat pills row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatPill(
                                label = "Income",
                                value = income.toDisplayAmount(),
                                color = IncomeColor,
                                modifier = Modifier.weight(1f)
                            )
                            StatPill(
                                label = "Spent",
                                value = spent.toDisplayAmount(),
                                color = ExpenseColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ── Budget Card ────────────────────────────────────────────────────
        if (activeBudget != null) {
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        BluffSectionHeader(title = "Budget")
                        EnhancedBudgetCard(budget = activeBudget!!, spent = spent)
                    }
                }
            }
        }

        // ── Recent Transactions ────────────────────────────────────────────
        item {
            BluffSectionHeader(
                title = "Recent Transactions",
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        if (recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💸", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add your first one",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            itemsIndexed(recentTransactions) { index, tx ->
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        )
                    )
                ) {
                    TransactionRow(
                        transaction = tx,
                        onClick = { onNavigateToTransactionDetail(tx.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = label,
                color = color.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = color,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedBudgetCard(budget: Budget, spent: Long) {
    val limit = budget.amountMinor
    val progress = if (limit > 0) (spent.toFloat() / limit.toFloat()).coerceIn(0f, 1f) else 0f
    val pct = (progress * 100).toInt()
    val progressColor = when {
        progress >= 0.9f -> ExpenseColor
        progress >= 0.7f -> WarningColor
        else -> IncomeColor
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "budgetProgress"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = CardColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = budget.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "${spent.toDisplayAmount()} of ${limit.toDisplayAmount()}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(progressColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$pct%",
                        color = progressColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(DividerColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(progressColor.copy(alpha = 0.7f), progressColor)
                            )
                        )
                )
            }
        }
    }
}
