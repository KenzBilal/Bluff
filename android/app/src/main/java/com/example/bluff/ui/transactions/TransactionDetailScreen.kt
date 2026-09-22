package com.example.bluff.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.data.repository.SplitBillRepository
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.SplitBill
import com.example.bluff.domain.model.SplitParticipant
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.domain.usecase.split.MarkSplitParticipantPaidUseCase
import com.example.bluff.domain.usecase.transaction.DeleteTransactionUseCase
import com.example.bluff.domain.usecase.transaction.GetTransactionsUseCase
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

// ─── ViewModel ────────────────────────────────────────────────────────────────

class TransactionDetailViewModel(
    private val transactionId: String,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val splitBillRepository: SplitBillRepository,
    private val markSplitParticipantPaidUseCase: MarkSplitParticipantPaidUseCase
) : ViewModel() {

    val transaction: StateFlow<Transaction?> = getTransactionsUseCase.getAll()
        .map { list -> list.find { it.id == transactionId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val splitBill: StateFlow<SplitBill?> = splitBillRepository
        .getSplitBillByTransactionId(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _deleted = MutableStateFlow(false)
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    fun deleteTransaction() {
        viewModelScope.launch {
            val result = deleteTransactionUseCase(transactionId)
            if (result.isSuccess) _deleted.value = true
            else _errorMsg.value = result.exceptionOrNull()?.message
        }
    }

    fun markParticipantPaid(participantId: String) {
        viewModelScope.launch {
            val result = markSplitParticipantPaidUseCase(participantId)
            if (result.isFailure) _errorMsg.value = result.exceptionOrNull()?.message
        }
    }

    fun clearError() { _errorMsg.value = null }

    companion object {
        fun factory(transactionId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val c = AppContainer.instance
                TransactionDetailViewModel(
                    transactionId,
                    c.getTransactionsUseCase,
                    c.deleteTransactionUseCase,
                    c.splitBillRepository,
                    c.markSplitParticipantPaidUseCase
                )
            }
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onBack: () -> Unit,
    viewModel: TransactionDetailViewModel = viewModel(
        factory = TransactionDetailViewModel.factory(transactionId)
    )
) {
    val transaction by viewModel.transaction.collectAsStateWithLifecycle()
    val splitBill by viewModel.splitBill.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()
    val errorMsg by viewModel.errorMsg.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleted) { if (deleted) onBack() }
    LaunchedEffect(errorMsg) {
        errorMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Transaction",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpenseColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { padding ->
        val tx = transaction
        if (tx == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // ── Amount Hero ────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(CardColor)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val amtColor = when (tx.type) {
                                TransactionType.EXPENSE -> ExpenseColor
                                TransactionType.INCOME -> IncomeColor
                                TransactionType.TRANSFER -> TransferColor
                            }
                            val prefix = when (tx.type) {
                                TransactionType.EXPENSE -> "-"
                                TransactionType.INCOME -> "+"
                                TransactionType.TRANSFER -> ""
                            }
                            Text(
                                text = "$prefix${tx.amountMinor.toDisplayAmount()}",
                                color = amtColor,
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = amtColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = tx.type.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    color = amtColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Details Card ───────────────────────────────────────────
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault())
                            DetailRow("Date", tx.transactionDate.format(fmt))
                            HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                            val defaultCat = if (tx.type == TransactionType.TRANSFER) "Transfer" else "Uncategorized"
                            DetailRow("Category", tx.categoryName ?: tx.note ?: defaultCat)
                            if (!tx.note.isNullOrBlank()) {
                                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow("Note", tx.note)
                            }
                            if (!tx.accountName.isNullOrBlank()) {
                                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow("Account", tx.accountName)
                            }
                            if (splitBill != null) {
                                HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                                DetailRow("Split Bill", "✓ Yes (${splitBill!!.participants.size} friends)")
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Split Bill Details ─────────────────────────────────────
                if (splitBill != null) {
                    item {
                        Text(
                            "Split Details",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    item {
                        // Summary: who has paid / pending
                        val paid = splitBill!!.participants.count { it.isPaid }
                        val total = splitBill!!.participants.size
                        val recovered = splitBill!!.participants.filter { it.isPaid }.sumOf { it.amountMinor }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("$paid / $total paid back", color = Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Recovered: ${recovered.toDisplayAmount()}", color = TextSecondary, fontSize = 12.sp)
                                }
                                val pending = splitBill!!.participants.filter { !it.isPaid }.sumOf { it.amountMinor }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Pending", color = WarningColor, fontSize = 12.sp)
                                    Text(pending.toDisplayAmount(), color = WarningColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    items(splitBill!!.participants) { participant ->
                        SplitParticipantRow(
                            participant = participant,
                            onMarkPaid = { viewModel.markParticipantPaid(participant.id) }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardColor,
            title = { Text("Delete Transaction", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("This cannot be undone. Are you sure?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTransaction()
                }) { Text("Delete", color = ExpenseColor, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun SplitParticipantRow(
    participant: SplitParticipant,
    onMarkPaid: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (participant.isPaid) IncomeColor.copy(alpha = 0.15f)
                        else Primary.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = participant.contactName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = if (participant.isPaid) IncomeColor else Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.contactName,
                    color = if (participant.isPaid) TextSecondary else TextPrimary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (participant.isPaid) TextDecoration.LineThrough else null
                )
                if (participant.isPaid) {
                    Text("Paid ✓", color = IncomeColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Text("Pending", color = WarningColor, fontSize = 11.sp)
                }
            }

            Text(
                text = participant.amountMinor.toDisplayAmount(),
                color = if (participant.isPaid) TextSecondary else TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            if (!participant.isPaid) {
                IconButton(
                    onClick = onMarkPaid,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(IncomeColor.copy(alpha = 0.15f))
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Mark Paid",
                        tint = IncomeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
