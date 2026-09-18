package com.example.bluff.ui.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Account
import com.example.bluff.domain.model.RecurrenceFrequency
import com.example.bluff.domain.model.RecurringTransaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.util.RecurrenceUtil
import com.example.bluff.domain.usecase.account.GetAccountsUseCase
import com.example.bluff.domain.usecase.recurring.DeleteRecurringTransactionUseCase
import com.example.bluff.domain.usecase.recurring.GetRecurringTransactionsUseCase
import com.example.bluff.domain.usecase.recurring.UpsertRecurringTransactionUseCase
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.IncomeColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecurringViewModel(
    private val getRecurringUseCase: GetRecurringTransactionsUseCase,
    private val upsertRecurringUseCase: UpsertRecurringTransactionUseCase,
    private val deleteRecurringUseCase: DeleteRecurringTransactionUseCase
) : ViewModel() {

    val recurringTransactions = getRecurringUseCase.getActive()

    fun saveRecurring(name: String, amount: Long, type: TransactionType, accountId: String, categoryId: String?, frequency: RecurrenceFrequency, startDate: LocalDate, existingId: String? = null) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val nextRunDate = if (startDate.isBefore(today)) {
                RecurrenceUtil.firstOccurrenceAfter(startDate, frequency, today)
            } else {
                startDate
            }
            val recurring = RecurringTransaction(
                id = existingId ?: "",
                userId = "",
                name = name,
                amountMinor = amount,
                type = type,
                accountId = accountId,
                categoryId = categoryId,
                frequency = frequency,
                startDate = startDate,
                nextRunDate = nextRunDate
            )
            upsertRecurringUseCase(recurring)
        }
    }

    fun deleteRecurring(id: String) {
        viewModelScope.launch {
            deleteRecurringUseCase(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                RecurringViewModel(
                    container.getRecurringTransactionsUseCase,
                    container.upsertRecurringTransactionUseCase,
                    container.deleteRecurringTransactionUseCase
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(onBack: () -> Unit) {
    val vm: RecurringViewModel = viewModel(factory = RecurringViewModel.Factory)
    val recurring by vm.recurringTransactions.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddEdit by remember { mutableStateOf(false) }
    var editingRecurring by remember { mutableStateOf<RecurringTransaction?>(null) }

    val accounts by AppContainer.instance.getAccountsUseCase.getActive()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletingRecurringId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring", fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingRecurring = null; showAddEdit = true },
                containerColor = Primary,
                contentColor = androidx.compose.ui.graphics.Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add recurring")
            }
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            if (recurring.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No recurring transactions", color = TextSecondary)
                    }
                }
            } else {
                items(recurring) { recur ->
                    RecurringCard(
                        recurring = recur,
                        accounts = accounts,
                        onClick = { editingRecurring = recur; showAddEdit = true },
                        onDelete = {
                            deletingRecurringId = recur.id
                            showDeleteDialog = true
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditRecurringSheet(
            recurring = editingRecurring,
            accounts = accounts,
            onDismiss = { showAddEdit = false },
            onSave = { name, amount, type, accountId, categoryId, frequency, startDate ->
                vm.saveRecurring(name, amount, type, accountId, categoryId, frequency, startDate, editingRecurring?.id)
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recurring") },
            text = { Text("Are you sure you want to delete this recurring transaction?") },
            confirmButton = {
                TextButton(onClick = {
                    deletingRecurringId?.let { vm.deleteRecurring(it) }
                    showDeleteDialog = false
                    deletingRecurringId = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; deletingRecurringId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RecurringCard(recurring: RecurringTransaction, accounts: List<Account>, onClick: () -> Unit, onDelete: () -> Unit) {
    val accountName = accounts.find { it.id == recurring.accountId }?.name ?: "Unknown"
    val amountColor = when (recurring.type) {
        TransactionType.INCOME -> IncomeColor
        TransactionType.EXPENSE -> ExpenseColor
        TransactionType.TRANSFER -> TextPrimary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(recurring.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(accountName, color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recurring.amountMinor.toDisplayAmount(), color = amountColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete recurring",
                        tint = TextSecondary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        recurring.frequency.name,
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Next: ${recurring.nextRunDate}", color = TextSecondary, fontSize = 14.sp)
        }
    }
}
