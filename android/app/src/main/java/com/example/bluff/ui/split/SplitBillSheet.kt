package com.example.bluff.ui.split

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.theme.*
import com.example.bluff.ui.components.*
import com.example.bluff.ui.util.toDisplayAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillSheet(
    onDismiss: () -> Unit,
    viewModel: SplitBillViewModel = viewModel(factory = SplitBillViewModel.Factory)
) {
    val amountText by viewModel.totalAmountText.collectAsStateWithLifecycle()
    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val splitMode by viewModel.splitMode.collectAsStateWithLifecycle()
    val participants by viewModel.participants.collectAsStateWithLifecycle()
    val saveResult by viewModel.saveResult.collectAsStateWithLifecycle()

    var showContactPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveResult) {
        val result = saveResult ?: return@LaunchedEffect
        if (result.isSuccess) {
            onDismiss()
            viewModel.consumeSaveResult()
        } else {
            snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Error")
            viewModel.consumeSaveResult()
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
                .padding(horizontal = 20.dp)
        ) {
            if (showContactPicker) {
                ContactPickerContent(
                    onContactSelected = { contact ->
                        viewModel.addContact(contact.name, contact.phone)
                        showContactPicker = false
                    },
                    onDismiss = { showContactPicker = false }
                )
            } else {
                Text(
                    text = "Split Bill",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Total Amount
                item {
                    BluffAmountInput(
                        amountText = amountText,
                        onAmountChange = viewModel::setTotalAmount
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Account & Category
                item {
                    BluffSectionHeader(title = "Account")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        accounts.take(3).forEach { account ->
                            BluffChip(
                                text = account.name,
                                selected = account.id == selectedAccountId,
                                onClick = { viewModel.setAccountId(account.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    BluffSectionHeader(title = "Category")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        categories.take(3).forEach { cat ->
                            BluffChip(
                                text = cat.name,
                                selected = cat.id == selectedCategoryId,
                                onClick = { viewModel.setCategoryId(cat.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Split Options
                item {
                    BluffSectionHeader(title = "Split Method")
                    BluffSegmentedControl(
                        options = listOf("Equal", "Custom"),
                        selectedIndex = if (splitMode == SplitMode.EQUAL) 0 else 1,
                        onSelect = { idx ->
                            viewModel.setSplitMode(if (idx == 0) SplitMode.EQUAL else SplitMode.CUSTOM)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BluffSectionHeader(title = "Participants", modifier = Modifier)
                        TextButton(onClick = { showContactPicker = true }) {
                            Text("+ Add Contact", color = Primary)
                        }
                    }
                }

                items(participants, key = { it.id }) { participant ->
                    Surface(
                        color = CardColor,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = participant.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (splitMode == SplitMode.EQUAL) {
                                Text(
                                    text = participant.amountMinor.toDisplayAmount(),
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                // Simple input for custom amounts
                                OutlinedTextField(
                                    value = if (participant.amountMinor > 0) (participant.amountMinor / 100).toString() else "",
                                    onValueChange = {
                                        viewModel.updateParticipantCustomAmount(
                                            participant.id, 
                                            (it.toLongOrNull() ?: 0L) * 100
                                        )
                                    },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = TextPrimary, 
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = DividerColor,
                                        focusedBorderColor = Primary
                                    )
                                )
                            }
                            if (!participant.isMe) {
                                IconButton(onClick = { viewModel.removeParticipant(participant.id) }) {
                                    Icon(Icons.Default.Close, "Remove", tint = ExpenseColor)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(48.dp))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    BluffButton(
                        text = "Save Split Bill",
                        onClick = { viewModel.saveSplit() }
                    )
                }
            }
            } // close else block
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}
