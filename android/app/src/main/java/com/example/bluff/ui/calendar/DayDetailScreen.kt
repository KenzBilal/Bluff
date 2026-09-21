package com.example.bluff.ui.calendar

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    date: LocalDate,
    onBack: () -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val transactionsForDay by viewModel.transactionsForDay.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()

    // Load transactions for this date
    viewModel.selectDate(date)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Summary section
            item {
                SummarySection(dailySummary)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expenses
            val expenses = transactionsForDay.filter { it.type == TransactionType.EXPENSE }
            if (expenses.isNotEmpty()) {
                item {
                    SectionHeader("Expenses (${expenses.size})")
                }
                items(expenses) { transaction ->
                    TransactionItem(transaction)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Income
            val income = transactionsForDay.filter { it.type == TransactionType.INCOME }
            if (income.isNotEmpty()) {
                item {
                    SectionHeader("Income (${income.size})")
                }
                items(income) { transaction ->
                    TransactionItem(transaction)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Transfers
            val transfers = transactionsForDay.filter { it.type == TransactionType.TRANSFER }
            if (transfers.isNotEmpty()) {
                item {
                    SectionHeader("Transfers (${transfers.size})")
                }
                items(transfers) { transaction ->
                    TransactionItem(transaction)
                }
            }

            // Empty state
            if (transactionsForDay.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions recorded",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummarySection(summary: CalendarViewModel.DailySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Daily Summary", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Income", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = summary.income.toDisplayAmount(),
                        color = IncomeColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = summary.expense.toDisplayAmount(),
                        color = ExpenseColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = summary.net.toDisplayAmount(),
                        color = if (summary.net >= 0) IncomeColor else ExpenseColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = TextPrimary,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Text(
                text = transaction.categoryIcon ?: "📦",
                fontSize = 24.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            // Category name and note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName ?: "Uncategorized",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (!transaction.note.isNullOrBlank()) {
                    Text(
                        text = transaction.note,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            // Amount
            Text(
                text = transaction.amountMinor.toDisplayAmount(),
                color = when (transaction.type) {
                    TransactionType.EXPENSE -> ExpenseColor
                    TransactionType.INCOME -> IncomeColor
                    TransactionType.TRANSFER -> TransferColor
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
