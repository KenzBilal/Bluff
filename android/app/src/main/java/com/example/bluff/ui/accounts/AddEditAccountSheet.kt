package com.example.bluff.ui.accounts

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
import androidx.compose.material3.MaterialTheme
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
import com.example.bluff.domain.model.AccountType
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountSheet(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: AccountType, initialBalance: Long, icon: String, color: String) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var type by remember { mutableStateOf(account?.type ?: AccountType.CASH) }
    var initialBalance by remember { mutableStateOf(account?.initialBalanceMinor?.let { (it / 100).toString() } ?: "0") }

    LaunchedEffect(account) {
        if (account == null) {
            name = ""
            type = AccountType.CASH
            initialBalance = "0"
        } else {
            name = account.name
            type = account.type
            initialBalance = (account.initialBalanceMinor / 100).toString()
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
                text = if (account != null) "Edit Account" else "New Account",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            BluffTextField(
                value = name,
                onValueChange = { name = it },
                label = "Account Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Account Type", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Column {
                Row {
                    AccountType.entries.take(3).forEach { t ->
                        BluffChip(
                            text = t.name,
                            selected = type == t,
                            onClick = { type = t },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    AccountType.entries.drop(3).forEach { t ->
                        BluffChip(
                            text = t.name,
                            selected = type == t,
                            onClick = { type = t },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BluffTextField(
                value = initialBalance,
                onValueChange = { initialBalance = it.filter { c -> c.isDigit() } },
                label = "Initial Balance",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = if (account != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name,
                            type,
                            initialBalance.toLongOrNull()?.times(100L) ?: 0L,
                            account?.icon ?: "wallet",
                            account?.color ?: "#6C63FF"
                        )
                        onDismiss()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
