package com.example.bluff.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.DividerColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary

@Composable
fun BluffChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Primary else CardColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextSecondary,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipText"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.97f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "chipScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(50.dp),
        color = bgColor,
        border = if (selected) null else BorderStroke(1.dp, DividerColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/** Segmented control — 3 equal options with a sliding indicator style */
@Composable
fun BluffSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, label ->
                BluffChip(
                    text = label,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
