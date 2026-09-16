package com.example.bluff.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.util.toDisplayAmount

@Composable
fun BluffAmountDisplay(
    amount: Long,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = amount.toDisplayAmount(),
            color = color,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
