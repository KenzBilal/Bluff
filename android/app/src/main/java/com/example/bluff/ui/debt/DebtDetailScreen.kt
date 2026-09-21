package com.example.bluff.ui.debt

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
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
fun DebtDetailScreen(
    contactName: String,
    onBack: () -> Unit,
    viewModel: DebtViewModel = viewModel(factory = DebtViewModel.Factory)
) {
    val allDebts by viewModel.activeDebts.collectAsStateWithLifecycle()
    val contactDebts = remember(allDebts, contactName) {
        allDebts.filter { it.contactName == contactName }.sortedByDescending { it.createdAt }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(contactName, fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(contactDebts, key = { it.id }) { debt ->
                DebtDetailCard(
                    debt = debt,
                    onMarkPaid = { viewModel.markPaid(debt.id) },
                    onDelete = { viewModel.delete(debt.id) }
                )
            }
        }
    }
}

@Composable
private fun DebtDetailCard(
    debt: Debt,
    onMarkPaid: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(debt.createdAt) {
        SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(debt.createdAt))
    }
    val isTheyOwe = debt.direction == DebtDirection.THEY_OWE
    val amountColor = if (isTheyOwe) IncomeColor else ExpenseColor
    val typeText = if (isTheyOwe) "They borrowed" else "You borrowed"

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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeText,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (!debt.note.isNullOrBlank()) {
                    Text(text = debt.note, color = TextSecondary, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = dateStr, color = TextTertiary, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = debt.amountMinor.toDisplayAmount(),
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IncomeColor.copy(alpha = 0.1f))
                            .clickable(onClick = onMarkPaid),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Mark paid",
                            tint = IncomeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ExpenseColor.copy(alpha = 0.1f))
                            .clickable(onClick = { showDeleteConfirm = true }),
                        contentAlignment = Alignment.Center
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
            title = { Text("Delete record?", color = TextPrimary) },
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
