package com.example.bluff.ui.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.Goal
import com.example.bluff.domain.usecase.goal.GetGoalsUseCase
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import com.example.bluff.ui.util.toDisplayAmount

class GoalsViewModel(
    private val getGoalsUseCase: GetGoalsUseCase
) : ViewModel() {
    val goals = getGoalsUseCase.getActive()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                GoalsViewModel(container.getGoalsUseCase)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(onBack: () -> Unit) {
    val vm: GoalsViewModel = viewModel(factory = GoalsViewModel.Factory)
    val goals by vm.goals.collectAsStateWithLifecycle(initialValue = emptyList())

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
                    GoalCard(goal)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: Goal) {
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(goal.icon, fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(goal.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text("${goal.currentAmountMinor.toDisplayAmount()} / ${goal.targetAmountMinor.toDisplayAmount()}", color = TextSecondary, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = parsedColor,
                trackColor = CardColor,
            )
        }
    }
}
