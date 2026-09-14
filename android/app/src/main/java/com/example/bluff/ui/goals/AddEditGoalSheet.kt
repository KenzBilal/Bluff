package com.example.bluff.ui.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Goal
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffTextField
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalSheet(
    goal: Goal?,
    onDismiss: () -> Unit,
    onSave: suspend (name: String, targetAmount: Long, targetDate: LocalDate?) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmountMinor?.toString() ?: "") }
    var targetDateStr by remember { mutableStateOf(goal?.targetDate?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        val coroutineScope = rememberCoroutineScope()
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (goal != null) "Edit Goal" else "New Goal",
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            BluffTextField(
                value = name,
                onValueChange = { name = it },
                label = "Goal Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            BluffTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                label = "Target Amount",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            BluffTextField(
                value = targetDateStr,
                onValueChange = { targetDateStr = it },
                label = "Target Date (yyyy-MM-dd, optional)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = if (goal != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank() && (targetAmount.toLongOrNull() ?: 0L) > 0) {
                        val date = try {
                            if (targetDateStr.isNotBlank()) LocalDate.parse(targetDateStr) else null
                        } catch (e: Exception) { null }
                        coroutineScope.launch {
                            onSave(name, targetAmount.toLongOrNull() ?: 0L, date)
                            onDismiss()
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
