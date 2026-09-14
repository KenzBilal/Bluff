package com.example.bluff.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Budget
import com.example.bluff.domain.model.BudgetPeriod
import com.example.bluff.domain.usecase.budget.DeleteBudgetUseCase
import com.example.bluff.domain.usecase.budget.GetBudgetsUseCase
import com.example.bluff.domain.usecase.budget.UpsertBudgetUseCase
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.categories.CategoriesViewModel
import com.example.bluff.ui.util.toDisplayAmount
import kotlinx.coroutines.launch
import java.time.LocalDate

class BudgetViewModel(
    private val getBudgetsUseCase: GetBudgetsUseCase,
    private val upsertBudgetUseCase: UpsertBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    val budgets = getBudgetsUseCase.getActive()

    fun saveBudget(name: String, amount: Long, period: BudgetPeriod, categoryId: String?, existingId: String? = null) {
        viewModelScope.launch {
            val now = LocalDate.now()
            val budget = Budget(
                id = existingId ?: "",
                userId = "",
                name = name,
                amountMinor = amount,
                period = period,
                categoryId = categoryId,
                startDate = now.withDayOfMonth(1),
                endDate = now.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth())
            )
            upsertBudgetUseCase(budget)
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            deleteBudgetUseCase(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                BudgetViewModel(
                    container.getBudgetsUseCase,
                    container.upsertBudgetUseCase,
                    container.deleteBudgetUseCase
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(onBack: () -> Unit) {
    val vm: BudgetViewModel = viewModel(factory = BudgetViewModel.Factory)
    val budgets by vm.budgets.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddEdit by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<Budget?>(null) }

    // Load categories for the sheet
    val categoriesViewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
    val categories by categoriesViewModel.categories.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budgets", fontWeight = FontWeight.Bold) },
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
                onClick = { editingBudget = null; showAddEdit = true },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add budget")
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
            if (budgets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No active budgets", color = TextSecondary)
                    }
                }
            } else {
                items(budgets) { budget ->
                    BudgetCard(
                        budget = budget,
                        onClick = { editingBudget = budget; showAddEdit = true }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditBudgetSheet(
            budget = editingBudget,
            categories = categories,
            onDismiss = { showAddEdit = false },
            onSave = { name, amount, period, categoryId ->
                vm.saveBudget(name, amount, period, categoryId, editingBudget?.id)
            }
        )
    }
}

@Composable
private fun BudgetCard(budget: Budget, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(budget.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(budget.amountMinor.toDisplayAmount(), color = Primary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        budget.period.name,
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            if (budget.spentMinor > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (budget.percentUsed / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = if (budget.isOverBudget) androidx.compose.ui.graphics.Color.Red else Primary,
                    trackColor = CardColor,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${budget.spentMinor.toDisplayAmount()} / ${budget.amountMinor.toDisplayAmount()} spent",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
