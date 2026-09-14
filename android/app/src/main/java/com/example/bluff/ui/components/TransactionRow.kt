package com.example.bluff.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.IncomeColor
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.theme.TransferColor
import com.example.bluff.ui.util.toDisplayAmount
import java.time.format.DateTimeFormatter

@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BluffCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = CardColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = transaction.categoryIcon ?: "📦")
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.categoryName ?: "Other", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                if (!transaction.note.isNullOrBlank()) {
                    Text(text = transaction.note, color = TextSecondary)
                }
                Text(text = transaction.accountName, color = TextSecondary)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                val amountColor = when (transaction.type) {
                    TransactionType.INCOME -> IncomeColor
                    TransactionType.EXPENSE -> ExpenseColor
                    TransactionType.TRANSFER -> TransferColor
                }
                val prefix = when (transaction.type) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.TRANSFER -> "↔"
                }
                Text(
                    text = "$prefix${transaction.amountMinor.toDisplayAmount()}",
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = transaction.transactionDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    color = TextSecondary
                )
            }
        }
    }
}
