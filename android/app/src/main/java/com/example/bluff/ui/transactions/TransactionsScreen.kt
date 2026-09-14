package com.example.bluff.ui.transactions

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTopBar
import com.example.bluff.ui.components.EmptyState
import com.example.bluff.ui.components.TransactionRow
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: TransactionsViewModel = viewModel(factory = TransactionsViewModel.Factory)
) {
    val transactions by viewModel.transactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        BluffTopBar(title = "Transactions")

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search transactions...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BluffChip(text = "All", selected = filterType == null, onClick = { viewModel.setFilterType(null) })
                BluffChip(text = "Expense", selected = filterType == TransactionType.EXPENSE, onClick = { viewModel.setFilterType(TransactionType.EXPENSE) })
                BluffChip(text = "Income", selected = filterType == TransactionType.INCOME, onClick = { viewModel.setFilterType(TransactionType.INCOME) })
                BluffChip(text = "Transfer", selected = filterType == TransactionType.TRANSFER, onClick = { viewModel.setFilterType(TransactionType.TRANSFER) })
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                EmptyState(message = if (searchQuery.isNotBlank()) "No matching transactions" else "No transactions yet")
            } else {
                val grouped = transactions.groupBy { it.transactionDate }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    grouped.forEach { (date, txns) ->
                        item {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(txns) { tx ->
                            TransactionRow(transaction = tx)
                        }
                    }
                }
            }
        }
    }
}
