package com.example.bluff.ui.cycles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSetupSheet(
    categoryName: String,
    defaultCycleDays: Int?,
    onConfirm: (cycleDays: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableStateOf(defaultCycleDays ?: 30) }
    val presets = listOf(7, 14, 21, 28, 30, 60, 90, 180, 365)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Track this expense?",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set a reminder for your next $categoryName",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            // Duration presets
            Text("Every", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.take(4).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                presets.drop(4).forEach { days ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = { selectedDays = days },
                        label = { Text("${days}d") }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))

            // Confirm button
            Button(
                onClick = { onConfirm(selectedDays) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Set Reminder", color = Color.White)
            }
            Spacer(Modifier.height(12.dp))

            // Skip button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip", color = TextSecondary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
