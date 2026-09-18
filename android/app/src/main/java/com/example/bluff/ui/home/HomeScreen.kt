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
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.domain.model.Goal
import com.example.bluff.domain.model.RecurringTransaction
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
    val topGoals by viewModel.topGoals.collectAsState()
    val upcomingCycles by viewModel.upcomingCycles.collectAsState()
    val dueRecurring by viewModel.dueRecurringTransactions.collectAsState()

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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CardColor)
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = greeting,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = totalBalance.toDisplayAmount(),
                            color = TextPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "Total Balance",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(color = DividerColor, thickness = 1.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Column {
                                Text("Income", color = TextSecondary, fontSize = 11.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    income.toDisplayAmount(),
                                    color = IncomeColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column {
                                Text("Spent", color = TextSecondary, fontSize = 11.sp)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    spent.toDisplayAmount(),
                                    color = ExpenseColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
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

        // ── Upcoming Cycles ──────────────────────────────────────────────
        if (upcomingCycles.isNotEmpty()) {
            item {
                BluffSectionHeader(
                    title = "Upcoming",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(upcomingCycles) { cycle ->
                UpcomingCycleRow(cycle)
            }
            item { Spacer(Modifier.height(16.dp)) }
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

        // ── Goals ────────────────────────────────────────────────────────
        if (topGoals.isNotEmpty()) {
            item {
                BluffSectionHeader(
                    title = "Goals",
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(topGoals) { goal ->
                GoalRow(goal = goal, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            }
        }
    }

    if (dueRecurring.isNotEmpty()) {
        val currentDue = dueRecurring.first()
        AlertDialog(
            onDismissRequest = { viewModel.dismissRecurring(currentDue.id) },
            containerColor = CardColor,
            title = {
                Text(
                    text = "Recurring Payment Due",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Did you pay ${currentDue.name} (${currentDue.amountMinor.toDisplayAmount()})?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.payRecurring(currentDue) }) {
                    Text("Paid", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRecurring(currentDue.id) }) {
                    Text("Later", color = TextSecondary)
                }
            }
        )
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

@Composable
private fun GoalRow(goal: Goal, modifier: Modifier = Modifier) {
    val progress = (goal.progressPercent / 100f).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "goalProgress"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = goal.progressPercent.toInt().toString() + "%",
                    color = IncomeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${goal.currentAmountMinor.toDisplayAmount()} of ${goal.targetAmountMinor.toDisplayAmount()}",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(DividerColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(IncomeColor.copy(alpha = 0.7f), IncomeColor)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun UpcomingCycleRow(cycle: ExpenseCycle) {
    val statusColor = when {
        cycle.isOverdue -> ExpenseColor
        cycle.isDueSoon -> Color(0xFFFF9800)
        else -> IncomeColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text("🔄", fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(cycle.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            cycle.statusText,
            color = statusColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
