package com.example.bluff.ui.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluff.domain.model.Category
import com.example.bluff.domain.model.CategoryType
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.ui.components.BluffButton
import com.example.bluff.ui.components.BluffChip
import com.example.bluff.ui.components.BluffTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCategorySheet(
    category: Category?,
    allCategories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (name: String, type: CategoryType, icon: String, color: String, parentId: String?, iconType: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var type by remember { mutableStateOf(category?.type ?: CategoryType.EXPENSE) }
    var icon by remember { mutableStateOf(category?.icon ?: "shopping_cart") }
    var color by remember { mutableStateOf(category?.color ?: "#888888") }
    var parentId by remember { mutableStateOf(category?.parentId) }
    var parentExpanded by remember { mutableStateOf(false) }
    var iconType by remember { mutableStateOf(category?.iconType ?: "material") }
    var showIconPicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        if (category == null) {
            name = ""
            type = CategoryType.EXPENSE
            icon = "shopping_cart"
            color = "#888888"
            parentId = null
            iconType = "material"
        } else {
            name = category.name
            type = category.type
            icon = category.icon
            color = category.color
            parentId = category.parentId
            iconType = category.iconType
        }
    }

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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
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
                    value = selectedParent?.let { parent ->
                        parent.name
                    } ?: "None (Root Category)",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = parentExpanded) },
                    leadingIcon = {
                        selectedParent?.let { parent ->
                            CategoryIcon(
                                icon = parent.icon,
                                iconType = parent.iconType,
                                color = parent.color,
                                modifier = Modifier
                            )
                        }
                    },
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
                            text = { Text(rootCat.name) },
                            leadingIcon = {
                                CategoryIcon(
                                    icon = rootCat.icon,
                                    iconType = rootCat.iconType,
                                    color = rootCat.color,
                                    modifier = Modifier
                                )
                            },
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
            CategoryIcon(
                icon = icon,
                iconType = iconType,
                color = color,
                modifier = Modifier.clickable { showIconPicker = true },
                backgroundSize = 64.dp,
                iconSize = 32.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            ColorPickerSection(
                selectedColor = color,
                onSelect = { color = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            BluffButton(
                text = if (category != null) "Update" else "Create",
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, type, icon, color, parentId, iconType)
                        onDismiss()
                    }
                }
            )

            if (category != null) {
                Spacer(modifier = Modifier.height(12.dp))
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Category", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showIconPicker) {
        IconPickerSheet(
            selectedIcon = icon,
            onSelect = { selectedIcon ->
                icon = selectedIcon
                iconType = "material"
            },
            onDismiss = { showIconPicker = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete \"${category?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        category?.let { cat ->
                            onDelete?.invoke()
                            showDeleteDialog = false
                            onDismiss()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
