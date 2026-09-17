package com.example.bluff.ui.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.Category
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary

private data class FlatCategory(
    val category: Category,
    val depth: Int
)

private fun flattenTree(categories: List<Category>, depth: Int = 0): List<FlatCategory> {
    val result = mutableListOf<FlatCategory>()
    for (category in categories) {
        result.add(FlatCategory(category, depth))
        if (category.children.isNotEmpty()) {
            result.addAll(flattenTree(category.children, depth + 1))
        }
    }
    return result
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    val categories by vm.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    val categoryTree by vm.categoryTree.collectAsStateWithLifecycle()
    var showAddEdit by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    val flatCategories = remember(categoryTree) { flattenTree(categoryTree) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { editingCategory = null; showAddEdit = true },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            if (flatCategories.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.FolderOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No categories yet",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Add a category to organize\nyour transactions",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { editingCategory = null; showAddEdit = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add Category")
                            }
                        }
                    }
                }
            } else {
                items(flatCategories) { flat ->
                    CategoryCard(
                        category = flat.category,
                        modifier = Modifier.padding(start = (flat.depth * 24).dp),
                        onClick = { editingCategory = flat.category; showAddEdit = true },
                        onLongClick = { deleteTarget = flat.category }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditCategorySheet(
            category = editingCategory,
            allCategories = categories,
            onDismiss = { showAddEdit = false },
            onSave = { name, type, icon, color, parentId, iconType ->
                vm.saveCategory(name, type, icon, color, editingCategory?.id, parentId, iconType)
            }
        )
    }

    deleteTarget?.let { category ->
        DeleteCategoryDialog(
            category = category,
            onConfirm = {
                vm.deleteCategory(category.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }
}

@Composable
private fun DeleteCategoryDialog(
    category: Category,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (category.isSystem) "Delete system category?" else "Delete category?"
    val message = if (category.isSystem) {
        "This is a system category and may be used by existing transactions. Deleting it could affect your transaction history."
    } else {
        "Are you sure you want to delete \"${category.name}\"? This action cannot be undone."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                icon = category.icon,
                iconType = category.iconType,
                color = category.color
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(category.type.name, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
