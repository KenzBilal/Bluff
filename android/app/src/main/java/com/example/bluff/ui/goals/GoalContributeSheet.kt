package com.example.bluff.ui.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Goal
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.components.BluffAmountInput
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.util.toDisplayAmount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalContributeSheet(
    goal: Goal,
    onDismiss: () -> Unit,
    onContribute: (Long) -> Unit
) {
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Add Money to \"${goal.name}\"",
                color = TextPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Target: ${goal.targetAmountMinor.toDisplayAmount()}",
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))

            BluffAmountInput(
                amountText = amountText,
                onAmountChange = { amountText = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = "Contribute",
                onClick = {
                    val displayAmount = amountText.toLongOrNull() ?: 0L
                    if (displayAmount > 0) {
                        onContribute(displayAmount * 100L)
                        onDismiss()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
