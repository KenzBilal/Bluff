package com.example.bluff.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.bluff.domain.model.Transaction
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.DividerColor
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.IncomeColor
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.theme.TextTertiary
import com.example.bluff.theme.TransferColor
import com.example.bluff.ui.util.toDisplayAmount
import java.time.format.DateTimeFormatter

@Composable
fun TransactionRow(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
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

    val iconBgColor = remember(transaction.categoryColor) {
        runCatching {
            Color(transaction.categoryColor?.toColorInt() ?: 0xFF6C63FF.toInt())
        }.getOrElse { Color(0xFF6C63FF) }.copy(alpha = 0.18f)
    }
    val iconFgColor = remember(transaction.categoryColor) {
        runCatching {
            Color(transaction.categoryColor?.toColorInt() ?: 0xFF6C63FF.toInt())
        }.getOrElse { Color(0xFF6C63FF) }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = CardColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon bubble with category color
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.categoryIcon ?: "💸",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.categoryName ?: transaction.note ?: "Transaction",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!transaction.note.isNullOrBlank() && transaction.categoryName != null) {
                        Text(
                            text = transaction.note,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                        Text(
                            text = " · ",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = transaction.transactionDate.format(
                            DateTimeFormatter.ofPattern("dd MMM")
                        ),
                        color = TextTertiary,
                        fontSize = 12.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix${transaction.amountMinor.toDisplayAmount()}",
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (transaction.accountName.isNotBlank()) {
                    Text(
                        text = transaction.accountName,
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
