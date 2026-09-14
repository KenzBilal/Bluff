package com.example.bluff.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Goal
import com.example.bluff.theme.Primary
import com.example.bluff.theme.SurfaceVariant
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount

@Composable
fun GoalCard(
    goal: Goal,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BluffCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = goal.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = goal.currentAmount.toDisplayAmount(), color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = goal.targetAmount.toDisplayAmount(), color = TextSecondary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (goal.targetAmount > 0) (goal.currentAmount.toFloat() / goal.targetAmount).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Primary,
                trackColor = SurfaceVariant
            )
        }
    }
}
