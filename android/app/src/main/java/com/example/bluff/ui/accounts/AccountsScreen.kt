package com.example.bluff.ui.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Account
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(onBack: () -> Unit, onNavigateToAccountDetail: (String) -> Unit) {
    val vm: AccountsViewModel = viewModel(factory = AccountsViewModel.Factory)
    val accounts by vm.accounts.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddEdit by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts", fontWeight = FontWeight.Bold) },
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
                onClick = { editingAccount = null; showAddEdit = true },
                containerColor = Primary,
                contentColor = androidx.compose.ui.graphics.Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add account")
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
            if (accounts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No active accounts", color = TextSecondary)
                    }
                }
            } else {
                items(accounts) { account ->
                    AccountCard(
                        account = account,
                        onClick = { onNavigateToAccountDetail(account.id) },
                        onLongClick = {
                            accountToDelete = account
                            showDeleteDialog = true
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showDeleteDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                accountToDelete = null
            },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete \"${accountToDelete!!.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.archiveAccount(accountToDelete!!.id)
                    showDeleteDialog = false
                    accountToDelete = null
                }) {
                    Text("Delete", color = ExpenseColor)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    accountToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddEdit) {
        AddEditAccountSheet(
            account = editingAccount,
            onDismiss = { showAddEdit = false },
            onSave = { name, type, balance, icon, color ->
                vm.saveAccount(name, type, balance, icon, color, editingAccount?.id)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun AccountCard(account: Account, onClick: () -> Unit, onLongClick: (() -> Unit)? = null) {
    val parsedColor = try {
        Color(account.color.toColorInt())
    } catch (e: Exception) {
        Primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(parsedColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(account.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(account.type.name, color = TextSecondary, fontSize = 14.sp)
            }
            Text(account.currentBalanceMinor.toDisplayAmount(), color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
