package com.example.bluff.ui.addtransaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.DebtDirection
import com.example.bluff.theme.*
import com.example.bluff.ui.components.*
import com.example.bluff.ui.cycles.CycleSetupSheet
import com.example.bluff.ui.util.toDisplayAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = viewModel(factory = AddTransactionViewModel.Factory)
) {
    val amount by viewModel.amount.collectAsState()
    val amountText by viewModel.amountText.collectAsState()
    val mode by viewModel.mode.collectAsState()
    val note by viewModel.note.collectAsState()
    val selectedAccountId by viewModel.selectedAccountId.collectAsState()
    val selectedToAccountId by viewModel.selectedToAccountId.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val categoryTree by viewModel.categoryTree.collectAsState()
    val quickSuggestions by viewModel.quickSuggestions.collectAsState()
    val monthlySpend by viewModel.monthlySpend.collectAsState()
    val debtDirection by viewModel.debtDirection.collectAsState()
    val debtContactName by viewModel.debtContactName.collectAsState()
    val transferContactName by viewModel.transferContactName.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAccountPicker by remember { mutableStateOf(false) }
    var showToAccountPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDebtContactPicker by remember { mutableStateOf(false) }
    var showTransferContactPicker by remember { mutableStateOf(false) }
    var showCycleSetup by remember { mutableStateOf(false) }
    var savedCategoryId by remember { mutableStateOf<String?>(null) }
    var savedAmount by remember { mutableStateOf(0L) }
    var savedCategoryName by remember { mutableStateOf("") }
    val defaultCycleDays by viewModel.defaultCycleDays.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.reset()
    }

    LaunchedEffect(saveResult) {
        when (val result = saveResult) {
            is AddTransactionViewModel.SaveResult.Success -> {
                if (mode == EntryMode.EXPENSE && savedCategoryId != null) {
                    showCycleSetup = true
                } else {
                    onDismiss()
                }
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
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerColor, RoundedCornerShape(50.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────────
            Text(
                text = when (mode) {
                    EntryMode.EXPENSE -> "Add Expense"
                    EntryMode.DEBT -> "Record Debt"
                    EntryMode.TRANSFER -> "Transfer Money"
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            // ── Amount ─────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(12.dp))
            BluffAmountInput(
                amountText = amountText,
                onAmountChange = viewModel::setAmountText
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Mode segmented control ─────────────────────────────────────
            BluffSegmentedControl(
                options = listOf("Expense", "Debt", "Transfer"),
                selectedIndex = when (mode) {
                    EntryMode.EXPENSE -> 0
                    EntryMode.DEBT -> 1
                    EntryMode.TRANSFER -> 2
                },
                onSelect = { idx ->
                    viewModel.setMode(
                        when (idx) {
                            0 -> EntryMode.EXPENSE
                            1 -> EntryMode.DEBT
                            else -> EntryMode.TRANSFER
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Mode content (animated) ────────────────────────────────────
            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 3 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 3 })
                },
                label = "modeContent"
            ) { currentMode ->
                Column {
                    when (currentMode) {
                        // ── EXPENSE ──────────────────────────────────────────
                        EntryMode.EXPENSE -> {
                            // Account selector
                            BluffSectionHeader(title = "Account")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
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
                                        text = "More…",
                                        selected = false,
                                        onClick = { showAccountPicker = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Category picker
                            BluffSectionHeader(title = "Category")
                            CategoryTreePicker(
                                categories = categoryTree,
                                selectedCategoryId = selectedCategoryId,
                                onCategorySelected = { viewModel.setCategoryId(it.id) },
                                quickSuggestions = quickSuggestions,
                                monthlySpend = monthlySpend,
                                onQuickAmountSelected = { category, amt ->
                                    viewModel.setCategoryId(category.id)
                                    viewModel.setAmountText((amt / 100).toString())
                                },
                                modifier = Modifier.height(280.dp)
                            )
                        }

                        // ── DEBT ──────────────────────────────────────────────
                        EntryMode.DEBT -> {
                            BluffSectionHeader(title = "Direction")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BluffChip(
                                    text = "They Owe Me",
                                    selected = debtDirection == DebtDirection.THEY_OWE,
                                    onClick = { viewModel.setDebtDirection(DebtDirection.THEY_OWE) },
                                    modifier = Modifier.weight(1f)
                                )
                                BluffChip(
                                    text = "I Owe Them",
                                    selected = debtDirection == DebtDirection.I_OWE,
                                    onClick = { viewModel.setDebtDirection(DebtDirection.I_OWE) },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            BluffSectionHeader(title = "Person")
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = if (debtContactName.isBlank()) "No contact selected" else debtContactName,
                                            color = if (debtContactName.isBlank()) TextSecondary else TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }
                                    TextButton(onClick = { showDebtContactPicker = true }) {
                                        Text(
                                            text = if (debtContactName.isBlank()) "Pick" else "Change",
                                            color = Primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        // ── TRANSFER ──────────────────────────────────────────
                        EntryMode.TRANSFER -> {
                            BluffSectionHeader(title = "From Account")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.take(3).forEach { account ->
                                    BluffChip(
                                        text = account.name,
                                        selected = account.id == selectedAccountId,
                                        onClick = { viewModel.setAccountId(account.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (accounts.size > 3) {
                                    BluffChip("More…", false, { showAccountPicker = true }, Modifier.weight(1f))
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            BluffSectionHeader(title = "To Account")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                accounts.filter { it.id != selectedAccountId }.take(3).forEach { account ->
                                    BluffChip(
                                        text = account.name,
                                        selected = account.id == selectedToAccountId,
                                        onClick = { viewModel.setToAccountId(account.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (accounts.filter { it.id != selectedAccountId }.size > 3) {
                                    BluffChip("More…", false, { showToAccountPicker = true }, Modifier.weight(1f))
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Optional contact
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = CardColor,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = "Sending to",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = if (transferContactName.isBlank()) "Optional — pick a contact" else transferContactName,
                                            color = if (transferContactName.isBlank()) TextTertiary else TextPrimary,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                    TextButton(onClick = { showTransferContactPicker = true }) {
                                        Text(
                                            text = if (transferContactName.isBlank()) "+ Add" else "Change",
                                            color = Primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Note ───────────────────────────────────────────────────────
            Spacer(modifier = Modifier.height(20.dp))
            BluffSectionHeader(title = "Note")
            BluffTextField(
                value = note,
                onValueChange = viewModel::setNote,
                label = "Optional note",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Save Button ────────────────────────────────────────────────
            BluffButton(
                text = when (mode) {
                    EntryMode.EXPENSE -> "Save Expense"
                    EntryMode.DEBT -> "Save Debt"
                    EntryMode.TRANSFER -> "Confirm Transfer"
                },
                onClick = { showConfirmDialog = true }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }

    // ── Contact pickers ────────────────────────────────────────────────────
    if (showDebtContactPicker) {
        ContactPickerSheet(
            onContactSelected = { contact ->
                viewModel.setDebtContact(contact.name, contact.phone)
                showDebtContactPicker = false
            },
            onDismiss = { showDebtContactPicker = false }
        )
    }
    if (showTransferContactPicker) {
        ContactPickerSheet(
            onContactSelected = { contact ->
                viewModel.setTransferContact(contact.name, contact.phone)
                showTransferContactPicker = false
            },
            onDismiss = { showTransferContactPicker = false }
        )
    }
    if (showAccountPicker) {
        AccountPicker(
            accounts = accounts,
            onAccountSelected = { viewModel.setAccountId(it.id); showAccountPicker = false },
            onDismiss = { showAccountPicker = false }
        )
    }
    if (showToAccountPicker) {
        AccountPicker(
            accounts = accounts.filter { it.id != selectedAccountId },
            onAccountSelected = { viewModel.setToAccountId(it.id); showToAccountPicker = false },
            onDismiss = { showToAccountPicker = false }
        )
    }

    // ── Confirm dialog ─────────────────────────────────────────────────────
    if (showConfirmDialog) {
        val accountName = accounts.find { it.id == selectedAccountId }?.name ?: ""
        val categoryName = categoryTree.flatMap { root ->
            listOf(root) + root.children.flatMap { l1 -> listOf(l1) + l1.children }
        }.find { it.id == selectedCategoryId }?.name ?: ""

        val summary = when (mode) {
            EntryMode.EXPENSE -> "₹${amount.toDisplayAmount().replace("₹", "")} on $categoryName\nFrom: $accountName"
            EntryMode.DEBT -> {
                val dir = if (debtDirection == DebtDirection.THEY_OWE) "owes you" else "you owe"
                "$debtContactName $dir ₹${amount.toDisplayAmount().replace("₹", "")}"
            }
            EntryMode.TRANSFER -> {
                val toName = accounts.find { it.id == selectedToAccountId }?.name ?: ""
                "₹${amount.toDisplayAmount().replace("₹", "")} from $accountName → $toName" +
                    if (transferContactName.isNotBlank()) "\nTo: $transferContactName" else ""
            }
        }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = CardColor,
            title = {
                Text(
                    when (mode) {
                        EntryMode.EXPENSE -> "Confirm Expense"
                        EntryMode.DEBT -> "Confirm Debt"
                        EntryMode.TRANSFER -> "Confirm Transfer"
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = summary + if (note.isNotBlank()) "\nNote: $note" else "",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    savedCategoryId = selectedCategoryId
                    savedAmount = amount
                    savedCategoryName = categoryTree.flatMap { root ->
                        listOf(root) + root.children.flatMap { l1 -> listOf(l1) + l1.children }
                    }.find { it.id == selectedCategoryId }?.name ?: ""
                    showConfirmDialog = false
                    viewModel.save()
                }) {
                    Text("Save", color = Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ── Cycle setup sheet ──────────────────────────────────────────────────
    if (showCycleSetup) {
        CycleSetupSheet(
            categoryName = savedCategoryName,
            defaultCycleDays = defaultCycleDays,
            onConfirm = { days ->
                viewModel.createCycle(savedCategoryId!!, savedCategoryName, savedAmount, days)
                showCycleSetup = false
                onDismiss()
            },
            onDismiss = {
                showCycleSetup = false
                onDismiss()
            }
        )
    }
}
