package com.example.bluff.ui.categories

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.example.bluff.theme.*

val categoryColors = listOf(
    // Reds
    "#E53935", "#D32F2F", "#C62828", "#FF5252",
    // Oranges
    "#FF9800", "#F57C00", "#EF6C00", "#FFB74D",
    // Yellows
    "#FDD835", "#FBC02D", "#F9A825", "#FFEE58",
    // Greens
    "#4CAF50", "#388E3C", "#2E7D32", "#66BB6A",
    // Teals
    "#009688", "#00796B", "#004D40", "#26A69A",
    // Blues
    "#2196F3", "#1976D2", "#1565C0", "#42A5F5",
    // Indigos
    "#3F51B5", "#303F9F", "#1A237E", "#5C6BC0",
    // Purples
    "#9C27B0", "#7B1FA2", "#6A1B9A", "#AB47BC",
    // Pinks
    "#E91E63", "#C2185B", "#AD1457", "#EC407A",
    // Grays
    "#9E9E9E", "#757575", "#616161", "#424242", "#212121"
)

@Composable
fun ColorPickerSection(
    selectedColor: String,
    onSelect: (String) -> Unit
) {
    var showHexInput by remember { mutableStateOf(false) }
    var hexValue by remember { mutableStateOf(selectedColor.removePrefix("#")) }

    Column {
        Text("Color", color = TextPrimary)
        Spacer(Modifier.height(8.dp))

        // Color grid
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            categoryColors.chunked(5).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { hex ->
                        val parsedColor = try {
                            Color(hex.toColorInt())
                        } catch (e: Exception) {
                            Primary
                        }
                        Surface(
                            shape = CircleShape,
                            color = parsedColor,
                            modifier = Modifier
                                .size(32.dp)
                                .then(
                                    if (selectedColor == hex) Modifier.border(3.dp, Color.White, CircleShape)
                                    else Modifier
                                )
                                .clickable { onSelect(hex) }
                        ) {}
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Custom hex input
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preview circle
            val previewColor = try {
                Color("#$hexValue".toColorInt())
            } catch (e: Exception) {
                Primary
            }
            Surface(
                shape = CircleShape,
                color = previewColor,
                modifier = Modifier.size(32.dp)
            ) {}

            OutlinedTextField(
                value = hexValue,
                onValueChange = { newHex ->
                    hexValue = newHex
                    if (newHex.length == 6) {
                        onSelect("#$newHex")
                    }
                },
                placeholder = { Text("FF5722", color = TextSecondary) },
                prefix = { Text("#", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CardColor,
                    unfocusedContainerColor = CardColor,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = DividerColor,
                    cursorColor = Primary
                )
            )
        }
    }
}
