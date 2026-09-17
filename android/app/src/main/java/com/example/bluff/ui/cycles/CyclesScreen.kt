package com.example.bluff.ui.cycles

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.ExpenseCycle
import com.example.bluff.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyclesScreen(
    onBack: () -> Unit,
    viewModel: CyclesViewModel = viewModel(factory = CyclesViewModel.Factory)
) {
    val cycles by viewModel.filteredCycles.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf<ExpenseCycle?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Cycles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Background)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CycleFilter.entries.forEach { f ->
                    FilterChip(
                        selected = filter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(f.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            if (cycles.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No expense cycles", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cycles, key = { it.id }) { cycle ->
                        CycleCard(
                            cycle = cycle,
                            onMarkPaid = { viewModel.markPaid(cycle) },
                            onDeactivate = { showDeleteDialog = cycle }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { cycle ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Deactivate Cycle") },
            text = { Text("Stop tracking '${cycle.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deactivate(cycle.id)
                    showDeleteDialog = null
                }) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CycleCard(
    cycle: ExpenseCycle,
    onMarkPaid: () -> Unit,
    onDeactivate: () -> Unit
) {
    val statusColor = when {
        cycle.isOverdue -> ExpenseColor
        cycle.isDueSoon -> Color(0xFFFF9800)
        else -> IncomeColor
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDD04", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(cycle.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text(
                    "Every ${cycle.cycleDays} days",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    cycle.statusText,
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row {
                    IconButton(onClick = onMarkPaid, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Check, "Mark paid", tint = IncomeColor, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDeactivate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Deactivate", tint = ExpenseColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
