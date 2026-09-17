package com.example.bluff.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.usecase.category.QuickSuggestions
import com.example.bluff.theme.CardColor
import com.example.bluff.ui.categories.CategoryIcon
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.SuccessColor
import com.example.bluff.theme.SurfaceVariant
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.theme.WarningColor
import com.example.bluff.ui.util.toDisplayAmount

@Composable
fun CategoryTreePicker(
    categories: List<Category>,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier,
    quickSuggestions: QuickSuggestions? = null,
    monthlySpend: Map<String, Long> = emptyMap(),
    onQuickAmountSelected: ((Category, Long) -> Unit)? = null
) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        if (quickSuggestions != null && quickSuggestions.categories.isNotEmpty()) {
            BluffSectionHeader(title = "\u26A1 Quick")
            quickSuggestions.categories.forEach { category ->
                QuickCategoryChip(
                    category = category,
                    monthlySpend = monthlySpend[category.id] ?: 0L,
                    onClick = { onCategorySelected(category) },
                    onAmountClick = { amount -> onQuickAmountSelected?.invoke(category, amount) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        BluffSectionHeader(title = "Categories")
        categories.forEach { rootCategory ->
            CategoryExpandableRow(
                category = rootCategory,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
                monthlySpend = monthlySpend
            )
        }
    }
}

@Composable
private fun CategoryExpandableRow(
    category: Category,
    selectedCategoryId: String?,
    onCategorySelected: (Category) -> Unit,
    monthlySpend: Map<String, Long>,
    level: Int = 0
) {
    var expanded by remember { mutableStateOf(false) }
    val hasChildren = category.children.isNotEmpty()
    val indent = (level * 16).dp

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (hasChildren) {
                        expanded = !expanded
                    } else {
                        onCategorySelected(category)
                    }
                }
                .padding(start = indent, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = category.icon,
                iconType = category.iconType,
                color = category.color,
                backgroundSize = 40.dp,
                iconSize = 20.dp,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                val spend = monthlySpend[category.id] ?: 0L
                if (spend > 0) {
                    Text(
                        text = "${spend.toDisplayAmount()} this month",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            spend < 50000 -> SuccessColor
                            spend < 200000 -> WarningColor
                            else -> ExpenseColor
                        }
                    )
                }
            }

            if (hasChildren) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextSecondary
                )
            }

            if (category.id == selectedCategoryId) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Primary
                )
            }
        }

        if (expanded && hasChildren) {
            category.children.forEach { child ->
                CategoryExpandableRow(
                    category = child,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                    monthlySpend = monthlySpend,
                    level = level + 1
                )
            }
        }
    }
}

@Composable
private fun QuickCategoryChip(
    category: Category,
    monthlySpend: Long,
    onClick: () -> Unit,
    onAmountClick: (Long) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = category.icon,
                iconType = category.iconType,
                color = category.color,
                backgroundSize = 40.dp,
                iconSize = 20.dp,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                if (monthlySpend > 0) {
                    Text(
                        text = monthlySpend.toDisplayAmount(),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            category.quickAmounts.take(3).forEach { amount ->
                Surface(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .clickable { onAmountClick(amount) },
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceVariant
                ) {
                    Text(
                        text = amount.toDisplayAmount(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = TextPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
