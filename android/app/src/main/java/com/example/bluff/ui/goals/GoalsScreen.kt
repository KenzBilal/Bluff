package com.example.bluff.ui.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.bluff.domain.model.Goal
import com.example.bluff.domain.usecase.goal.DeleteGoalUseCase
import com.example.bluff.domain.usecase.goal.GetGoalsUseCase
import com.example.bluff.domain.usecase.goal.UpsertGoalUseCase
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount
import kotlinx.coroutines.launch
import java.time.LocalDate

class GoalsViewModel(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val upsertGoalUseCase: UpsertGoalUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    val goals = getGoalsUseCase.getActive()

    fun saveGoal(name: String, targetAmount: Long, targetDate: LocalDate?, existingId: String? = null) {
        viewModelScope.launch {
            val goal = Goal(
                id = existingId ?: "",
                userId = "",
                name = name,
                targetAmountMinor = targetAmount,
                targetDate = targetDate,
                icon = "🎯",
                color = "#6C63FF"
            )
            upsertGoalUseCase(goal)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            deleteGoalUseCase(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                GoalsViewModel(
                    container.getGoalsUseCase,
                    container.upsertGoalUseCase,
                    container.deleteGoalUseCase
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(onBack: () -> Unit) {
    val vm: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory)
    val goals by vm.goals.collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddEdit by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals", fontWeight = FontWeight.Bold) },
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
                onClick = { editingGoal = null; showAddEdit = true },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add goal")
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
            if (goals.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No active goals", color = TextSecondary)
                    }
                }
            } else {
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { editingGoal = goal; showAddEdit = true },
                        onDelete = { vm.deleteGoal(goal.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    if (showAddEdit) {
        AddEditGoalSheet(
            goal = editingGoal,
            onDismiss = { showAddEdit = false },
            onSave = { name, target, date ->
                vm.saveGoal(name, target, date, editingGoal?.id)
            }
        )
    }
}

@Composable
private fun GoalCard(goal: Goal, onClick: () -> Unit, onDelete: () -> Unit) {
    val progress = if (goal.targetAmountMinor > 0) {
        goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()
    } else 0f

    val parsedColor = try {
        Color(android.graphics.Color.parseColor(goal.color))
    } catch (e: Exception) {
        Primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardColor,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(goal.icon, fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("${goal.currentAmountMinor.toDisplayAmount()} / ${goal.targetAmountMinor.toDisplayAmount()}", color = TextSecondary, fontSize = 14.sp)
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete goal",
                        tint = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = parsedColor,
                trackColor = CardColor,
            )
            if (goal.targetDate != null) {
                Spacer(Modifier.height(8.dp))
                Text("Target: ${goal.targetDate}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}
