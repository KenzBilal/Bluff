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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.Background
import com.example.bluff.ui.components.BluffAmountInput
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField
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
            
            BluffTextField(
                value = note,
                onValueChange = viewModel::setNote,
                label = "Note",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            NumericKeypad(
                onNumberClick = viewModel::appendAmount,
                onBackspaceClick = viewModel::removeAmount
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            BluffButton(text = "Save Transaction", onClick = {
                viewModel.save()
                onDismiss()
            })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
