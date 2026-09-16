package com.example.bluff.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.DividerColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount

class CurrencyAmountVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val formattedText = if (originalText.isEmpty()) {
            "₹ 0"
        } else {
            try {
                val amountMinor = originalText.toLong() * 100L
                amountMinor.toDisplayAmount()
            } catch (e: Exception) {
                "₹ 0"
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = formattedText.length
            override fun transformedToOriginal(offset: Int): Int = originalText.length
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}

@Composable
fun BluffAmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TAP TO ENTER AMOUNT",
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = amountText,
            onValueChange = { newValue ->
                if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                    onAmountChange(newValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = color,
                fontSize = 52.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = CurrencyAmountVisualTransformation(),
            cursorBrush = SolidColor(Primary),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(top = 8.dp),
            color = if (amountText.isNotEmpty()) Primary else DividerColor,
            thickness = 2.dp
        )
    }
}
