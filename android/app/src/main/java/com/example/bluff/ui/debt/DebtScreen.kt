package com.example.bluff.ui.debt

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Debt
import com.example.bluff.domain.model.DebtDirection
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtScreen(
    onBack: () -> Unit = {},
    viewModel: DebtViewModel = viewModel(factory = DebtViewModel.Factory)
) {
    val theyOweMe by viewModel.theyOweMe.collectAsState()
    val iOwe by viewModel.iOwe.collectAsState()
    val totalTheyOweMe by viewModel.totalTheyOweMe.collectAsState()
    val totalIOwe by viewModel.totalIOwe.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debts", fontWeight = FontWeight.Bold) },
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
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // ── Summary cards ────────────────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DebtSummaryCard(
                    label = "They Owe Me",
                    amount = totalTheyOweMe,
                    color = IncomeColor,
                    modifier = Modifier.weight(1f)
                )
                DebtSummaryCard(
                    label = "I Owe",
                    amount = totalIOwe,
                    color = ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ── They Owe Me ──────────────────────────────────────────────────────
        if (theyOweMe.isNotEmpty()) {
            item {
                SectionHeader(title = "They Owe Me", count = theyOweMe.size)
            }
            items(theyOweMe, key = { it.id }) { debt ->
                DebtCard(
                    debt = debt,
                    onMarkPaid = { viewModel.markPaid(debt.id) },
                    onDelete = { viewModel.delete(debt.id) }
                )
            }
        }

        // ── I Owe ────────────────────────────────────────────────────────────
        if (iOwe.isNotEmpty()) {
            item {
                SectionHeader(title = "I Owe", count = iOwe.size)
            }
            items(iOwe, key = { it.id }) { debt ->
                DebtCard(
                    debt = debt,
                    onMarkPaid = { viewModel.markPaid(debt.id) },
                    onDelete = { viewModel.delete(debt.id) }
                )
            }
        }

        // ── Empty state ──────────────────────────────────────────────────────
        if (theyOweMe.isEmpty() && iOwe.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active debts",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to record money lent or borrowed",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
        }
    }
        }
    }
}

@Composable
private fun DebtSummaryCard(
    label: String,
    amount: Long,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = amount.toDisplayAmount(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = CircleShape,
            color = Primary.copy(alpha = 0.15f)
        ) {
            Text(
                text = "$count",
                color = Primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebtCard(
    debt: Debt,
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(debt.createdAt) {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(debt.createdAt))
    }
    val isTheyOwe = debt.direction == DebtDirection.THEY_OWE
    val amountColor = if (isTheyOwe) IncomeColor else ExpenseColor

    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = Primary.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = debt.contactName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.contactName,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (!debt.note.isNullOrBlank()) {
                    Text(text = debt.note, color = TextSecondary, fontSize = 12.sp)
                }
                Text(text = dateStr, color = TextTertiary, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = debt.amountMinor.toDisplayAmount(),
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Mark paid
                    IconButton(
                        onClick = onMarkPaid,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IncomeColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark paid",
                            tint = IncomeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    // Delete
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ExpenseColor.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardColor,
            title = { Text("Delete debt?", color = TextPrimary) },
            text = { Text("This will permanently remove this debt record.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Delete", color = ExpenseColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
