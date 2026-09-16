package com.example.bluff.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField

private val defaultIcons = listOf(
    "🍽️", "🚌", "🛍️", "📚", "🎮", "💡",
    "🏥", "📱", "💰", "💻", "📦", "🏠",
    "✈️", "🎬", "🎵", "🏋️", "☕", "🎁",
    "🐱", "🚗", "💊", "📝", "🎨", "🔧"
)

private val defaultColors = listOf(
    "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
    "#FFEAA7", "#DDA0DD", "#98FB98", "#F0E68C",
    "#00C896", "#3A8EFF", "#888888", "#FF8C42",
    "#6C63FF", "#FF4757", "#2ED573", "#FFA502",
    "#70A1FF", "#7BED9F", "#ECCC68", "#FF6348"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditCategorySheet(
    category: Category?,
    allCategories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: CategoryType, icon: String, color: String, parentId: String?) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: CategoryType.EXPENSE) }
    var icon by remember { mutableStateOf(category?.icon ?: "📦") }
    var color by remember { mutableStateOf(category?.color ?: "#888888") }
    var parentId by remember { mutableStateOf(category?.parentId) }
    var parentExpanded by remember { mutableStateOf(false) }

    val excludedIds = remember(category) {
        if (category == null) emptySet()
        else {
            val descendants = mutableSetOf<String>()
            val queue = mutableListOf(category.id)
            while (queue.isNotEmpty()) {
                val current = queue.removeAt(0)
                allCategories.filter { it.parentId == current }.forEach {
                    descendants.add(it.id)
                    queue.add(it.id)
                }
            }
            descendants + category.id
        }
    }
    val rootCategories = allCategories.filter { it.parentId == null && it.id !in excludedIds }
    val selectedParent = allCategories.find { it.id == parentId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (category != null) "Edit Category" else "New Category",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))

            BluffTextField(
                value = name,
                onValueChange = { name = it },
                label = "Category Name",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Category Type", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                CategoryType.entries.forEach { t ->
                    BluffChip(
                        text = t.name,
                        selected = type == t,
                        onClick = { type = t },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Parent Category", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = parentExpanded,
                onExpandedChange = { parentExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedParent?.let { "${it.icon} ${it.name}" } ?: "None (Root Category)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CardColor,
                        unfocusedContainerColor = com.example.bluff.theme.SurfaceVariant,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = com.example.bluff.theme.DividerColor,
                        cursorColor = Primary
                    )
                )
                ExposedDropdownMenu(
                    expanded = parentExpanded,
                    onDismissRequest = { parentExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (Root Category)") },
                        onClick = {
                            parentId = null
                            parentExpanded = false
                        }
                    )
                    rootCategories.forEach { rootCat ->
                        DropdownMenuItem(
                            text = { Text("${rootCat.icon} ${rootCat.name}") },
                            onClick = {
                                parentId = rootCat.id
                                parentExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Icon", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(defaultIcons) { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = if (icon == emoji) Primary else CardColor,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { icon = emoji }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Color", color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                defaultColors.forEach { hex ->
                    val parsedColor = try {
                        Color(hex.toColorInt())
                    } catch (e: Exception) {
                        Primary
                    }
                    Surface(
                        shape = CircleShape,
                        color = parsedColor,
                        modifier = Modifier
                            .size(36.dp)
                            .then(
                                if (color == hex) Modifier.border(3.dp, Color.White, CircleShape)
                                else Modifier
                            )
                            .clickable { color = hex }
                    ) {}
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = if (category != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, type, icon, color, parentId)
                        onDismiss()
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
