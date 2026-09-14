package com.example.bluff.ui.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Budget
import com.example.bluff.domain.model.BudgetPeriod
import com.example.bluff.domain.model.Category
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetSheet(
    budget: Budget?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, period: BudgetPeriod, categoryId: String?) -> Unit
) {
    var name by remember { mutableStateOf(budget?.name ?: "") }
    var amount by remember { mutableStateOf(budget?.amountMinor?.toString() ?: "") }
    var period by remember { mutableStateOf(budget?.period ?: BudgetPeriod.MONTHLY) }
    var selectedCategoryId by remember { mutableStateOf(budget?.categoryId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (budget != null) "Edit Budget" else "New Budget",
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            BluffTextField(
                value = name,
                onValueChange = { name = it },
                label = "Budget Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            BluffTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = "Amount",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Period", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                listOf(BudgetPeriod.WEEKLY, BudgetPeriod.MONTHLY, BudgetPeriod.YEARLY).forEach { p ->
                    BluffChip(
                        text = p.name,
                        selected = period == p,
                        onClick = { period = p },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Category (optional — blank = overall budget)", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                BluffChip(
                    text = "Overall",
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = if (budget != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0) {
                        onSave(name, amount.toLongOrNull() ?: 0L, period, selectedCategoryId)
                        onDismiss()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
