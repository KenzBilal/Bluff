package com.example.bluff.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(onBack: () -> Unit) {
    val vm: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    val categories by vm.categories.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddEdit by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

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
            if (categories.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No categories", color = TextSecondary)
                    }
                }
            } else {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { editingCategory = category; showAddEdit = true }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditCategorySheet(
            category = editingCategory,
            onDismiss = { showAddEdit = false },
            onSave = { name, type, icon, color ->
                vm.saveCategory(name, type, icon, color, editingCategory?.id)
            }
        )
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        Primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(parsedColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(category.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(category.type.name, color = TextSecondary, fontSize = 14.sp)
            }
        }
    }
}
