package com.example.bluff.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Account
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.util.toDisplayAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPicker(
    accounts: List<Account>,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BluffSectionHeader(title = "Select Account")
            Spacer(modifier = Modifier.height(8.dp))
            accounts.forEach { account ->
                BluffCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = {
                        onAccountSelected(account)
                        onDismiss()
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = account.name, color = TextPrimary)
                        Text(text = account.balance.toDisplayAmount(), color = TextPrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
