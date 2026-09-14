package com.example.bluff.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.IncomeColor
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.components.BluffAmountInput
import com.example.bluff.ui.components.BluffSectionHeader
import com.example.bluff.ui.components.BudgetProgressCard
import com.example.bluff.ui.components.EmptyState
import com.example.bluff.ui.components.TransactionRow
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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = greeting, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Total Balance", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            BluffAmountInput(amount = totalBalance)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Income", color = TextSecondary)
                    Text(text = income.toDisplayAmount(), color = IncomeColor, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text(text = "Spent", color = TextSecondary)
                    Text(text = spent.toDisplayAmount(), color = ExpenseColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (activeBudget != null) {
            item {
                BluffSectionHeader(title = "Budget")
                BudgetProgressCard(budget = activeBudget!!, spent = spent)
            }
        }

        item {
            BluffSectionHeader(title = "Recent Transactions")
        }

        if (recentTransactions.isEmpty()) {
            item {
                EmptyState(
                    message = "No transactions yet — tap + to add your first transaction"
                )
            }
        } else {
            items(recentTransactions) { tx ->
                TransactionRow(transaction = tx, onClick = { onNavigateToTransactionDetail(tx.id) })
            }
        }
    }
}
