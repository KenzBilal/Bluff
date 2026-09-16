package com.example.bluff.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.DividerColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.SurfaceVariant
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary

@Composable
fun BluffTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    singleLine: Boolean = true,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = if (label.isNotBlank()) {
            { Text(label, color = TextSecondary) }
        } else null,
        modifier = modifier,
        singleLine = singleLine,
        readOnly = readOnly,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = CardColor,
            unfocusedContainerColor = SurfaceVariant,
            focusedBorderColor = Primary,
            unfocusedBorderColor = DividerColor,
            cursorColor = Primary
        )
    )
}
