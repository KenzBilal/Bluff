package com.example.bluff.ui.addtransaction

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.Background
import com.example.bluff.ui.components.AccountPicker
import com.example.bluff.ui.components.BluffAmountInput
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffSectionHeader
import com.example.bluff.ui.components.BluffTextField
import com.example.bluff.ui.components.CategoryPickerGrid
import com.example.bluff.ui.components.NumericKeypad

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel(factory = AddTransactionViewModel.Factory)
) {
    val amount by viewModel.amount.collectAsState()
    val type by viewModel.type.collectAsState()
    val note by viewModel.note.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    val selectedToAccountId by viewModel.selectedToAccountId.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is AddTransactionViewModel.SaveResult.Success -> {
                onDismiss()
                viewModel.consumeSaveResult()
            }
            is AddTransactionViewModel.SaveResult.Error -> {
                snackbarHostState.showSnackbar(result.message)
                viewModel.consumeSaveResult()
            }
            else -> {}
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BluffAmountInput(amount = amount)

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                BluffChip(text = "Expense", selected = type == TransactionType.EXPENSE, onClick = { viewModel.setType(TransactionType.EXPENSE) }, modifier = Modifier.weight(1f))
                BluffChip(text = "Income", selected = type == TransactionType.INCOME, onClick = { viewModel.setType(TransactionType.INCOME) }, modifier = Modifier.weight(1f))
                BluffChip(text = "Transfer", selected = type == TransactionType.TRANSFER, onClick = { viewModel.setType(TransactionType.TRANSFER) }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            val selectedAccount = accounts.find { it.id == selectedAccountId }
            BluffSectionHeader(title = "Account")
            BluffTextField(
                value = selectedAccount?.name ?: "Select account",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                accounts.take(3).forEach { account ->
                    BluffChip(
                        text = account.name,
                        selected = account.id == selectedAccountId,
                        onClick = { viewModel.setAccountId(account.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (accounts.size > 3) {
                    BluffChip(
                        text = "View all",
                        selected = false,
                        onClick = { showAccountPicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (showAccountPicker) {
                AccountPicker(
                    accounts = accounts,
                    onAccountSelected = { viewModel.setAccountId(it.id) },
                    onDismiss = { showAccountPicker = false }
                )
            }

            if (type == TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(8.dp))
                val selectedToAccount = accounts.find { it.id == selectedToAccountId }
                BluffSectionHeader(title = "To Account")
                BluffTextField(
                    value = selectedToAccount?.name ?: "Select destination",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    accounts.filter { it.id != selectedAccountId }.take(3).forEach { account ->
                        BluffChip(
                            text = account.name,
                            selected = account.id == selectedToAccountId,
                            onClick = { viewModel.setToAccountId(account.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (accounts.filter { it.id != selectedAccountId }.size > 3) {
                        BluffChip(
                            text = "View all",
                            selected = false,
                            onClick = { showToAccountPicker = true },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (showToAccountPicker) {
                    AccountPicker(
                        accounts = accounts.filter { it.id != selectedAccountId },
                        onAccountSelected = { viewModel.setToAccountId(it.id) },
                        onDismiss = { showToAccountPicker = false }
                    )
                }
            }

            if (type != TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(8.dp))
                BluffSectionHeader(title = "Category")
                val categories = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
                CategoryPickerGrid(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = { viewModel.setCategoryId(it.id) },
                    modifier = Modifier.height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            BluffTextField(
                value = note,
                onValueChange = viewModel::setNote,
                label = "Note (optional)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            NumericKeypad(
                onNumberClick = viewModel::appendAmount,
                onBackspaceClick = viewModel::removeAmount
            )

            Spacer(modifier = Modifier.height(16.dp))

            BluffButton(text = "Save Transaction", onClick = { viewModel.save() })
            Spacer(modifier = Modifier.height(16.dp))
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
