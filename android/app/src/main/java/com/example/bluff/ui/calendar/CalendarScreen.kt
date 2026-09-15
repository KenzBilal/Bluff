package com.example.bluff.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onDateSelected: (LocalDate) -> Unit,
    viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory)
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val transactionsForMonth by viewModel.transactionsForMonth.collectAsState()
    val daysWithTransactions = viewModel.getDaysWithTransactions()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(16.dp)
    ) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous month",
                    tint = TextPrimary
                )
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next month",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            DayOfWeek.entries.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        CalendarGrid(
            yearMonth = currentMonth,
            selectedDate = selectedDate,
            daysWithTransactions = daysWithTransactions,
            onDateClick = { date ->
                viewModel.selectDate(date)
                onDateSelected(date)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Monthly summary
        val totalIncome = transactionsForMonth.filter { it.type == com.example.bluff.domain.model.TransactionType.INCOME }.sumOf { it.amountMinor }
        val totalExpense = transactionsForMonth.filter { it.type == com.example.bluff.domain.model.TransactionType.EXPENSE }.sumOf { it.amountMinor }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Income", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = totalIncome.toDisplayAmount(),
                        color = IncomeColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Expense", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = totalExpense.toDisplayAmount(),
                        color = ExpenseColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Net", color = TextSecondary, fontSize = 14.sp)
                    Text(
                        text = (totalIncome - totalExpense).toDisplayAmount(),
                        color = if (totalIncome - totalExpense >= 0) IncomeColor else ExpenseColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    daysWithTransactions: Set<Int>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday

    Column {
        var currentDay = 1
        val totalWeeks = ((firstDayOfWeek - 1 + lastDayOfMonth.dayOfMonth) / 7) + 1

        for (week in 0 until totalWeeks) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 1..7) {
                    val dayIndex = week * 7 + dayOfWeek - (firstDayOfWeek - 1)
                    if (dayIndex in 1..lastDayOfMonth.dayOfMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val isSelected = date == selectedDate
                        val hasTransactions = dayIndex in daysWithTransactions
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> Primary
                                        isToday -> SurfaceVariant
                                        else -> Background
                                    }
                                )
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayIndex.toString(),
                                    color = when {
                                        isSelected -> TextPrimary
                                        isToday -> Primary
                                        else -> TextPrimary
                                    },
                                    fontSize = 16.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasTransactions) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) TextPrimary else Primary)
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
