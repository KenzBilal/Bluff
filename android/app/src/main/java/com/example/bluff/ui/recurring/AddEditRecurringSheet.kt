package com.example.bluff.ui.recurring

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.model.RecurringTransaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringSheet(
    recurring: RecurringTransaction?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Long, type: TransactionType, accountId: String, categoryId: String?, frequency: RecurrenceFrequency, startDate: LocalDate) -> Unit
) {
    var name by remember { mutableStateOf(recurring?.name ?: "") }
    var amount by remember { mutableStateOf(recurring?.amountMinor?.let { (it / 100).toString() } ?: "") }
    var type by remember { mutableStateOf(recurring?.type ?: TransactionType.EXPENSE) }
    var accountId by remember { mutableStateOf(recurring?.accountId ?: accounts.firstOrNull()?.id ?: "") }
    var categoryId by remember { mutableStateOf(recurring?.categoryId) }
    var frequency by remember { mutableStateOf(recurring?.frequency ?: RecurrenceFrequency.MONTHLY) }
    var startDateStr by remember { mutableStateOf(recurring?.startDate?.toString() ?: LocalDate.now().toString()) }

    LaunchedEffect(recurring) {
        if (recurring == null) {
            name = ""
            amount = ""
            type = TransactionType.EXPENSE
            accountId = accounts.firstOrNull()?.id ?: ""
            categoryId = null
            frequency = RecurrenceFrequency.MONTHLY
            startDateStr = LocalDate.now().toString()
        } else {
            name = recurring.name
            amount = (recurring.amountMinor / 100).toString()
            type = recurring.type
            accountId = recurring.accountId
            categoryId = recurring.categoryId
            frequency = recurring.frequency
            startDateStr = recurring.startDate.toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (recurring != null) "Edit Recurring" else "New Recurring",
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            BluffTextField(value = name, onValueChange = { name = it }, label = "Name", modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            BluffTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = "Amount", modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(12.dp))
            Text("Type", color = TextPrimary)
            Row {
                BluffChip(text = "Expense", selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE }, modifier = Modifier.weight(1f))
                BluffChip(text = "Income", selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Account", color = TextPrimary)
            Row {
                accounts.take(3).forEach { acc ->
                    BluffChip(text = acc.name, selected = accountId == acc.id, onClick = { accountId = acc.id }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Frequency", color = TextPrimary)
            Row {
                listOf(RecurrenceFrequency.WEEKLY, RecurrenceFrequency.MONTHLY, RecurrenceFrequency.YEARLY).forEach { f ->
                    BluffChip(text = f.name, selected = frequency == f, onClick = { frequency = f }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            BluffTextField(value = startDateStr, onValueChange = { startDateStr = it }, label = "Start Date (yyyy-MM-dd)", modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            BluffButton(
                text = if (recurring != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank() && (amount.toLongOrNull() ?: 0L) > 0 && accountId.isNotBlank()) {
                        val startDate = try { LocalDate.parse(startDateStr) } catch (e: Exception) { LocalDate.now() }
                        onSave(name, amount.toLongOrNull()?.times(100L) ?: 0L, type, accountId, categoryId, frequency, startDate)
                        onDismiss()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
