package com.example.bluff.ui.calendar

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.TransactionType
import com.example.bluff.theme.*
import com.example.bluff.ui.components.BluffSectionHeader
import com.example.bluff.ui.components.TransactionRow
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
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val transactionsForMonth by viewModel.transactionsForMonth.collectAsStateWithLifecycle()
    val selectedDateTransactions by viewModel.transactionsForDay.collectAsStateWithLifecycle()
    val daysWithTransactions = viewModel.getDaysWithTransactions()

    val totalIncome = transactionsForMonth
        .filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
    val totalExpense = transactionsForMonth
        .filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
    val net = totalIncome - totalExpense

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(48.dp))
            Text(
                "Calendar",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Month nav + grid ──────────────────────────────────────────────
        item {
            Surface(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = CardColor
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month navigation row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMonth() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous month",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentMonth.month
                                    .getDisplayName(TextStyle.FULL, Locale.getDefault()),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = currentMonth.year.toString(),
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                        IconButton(
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariant)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next month",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Day of week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        DayOfWeek.entries.forEach { day ->
                            Text(
                                text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    CalendarGrid(
                        yearMonth = currentMonth,
                        selectedDate = selectedDate,
                        daysWithTransactions = daysWithTransactions,
                        onDateClick = { date ->
                            viewModel.selectDate(date)
                            onDateSelected(date)
                        }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Monthly summary strip ─────────────────────────────────────────
        item {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthlySummaryPill(
                    label = "Income",
                    amount = totalIncome,
                    color = IncomeColor,
                    modifier = Modifier.weight(1f)
                )
                MonthlySummaryPill(
                    label = "Spent",
                    amount = totalExpense,
                    color = ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
                MonthlySummaryPill(
                    label = "Net",
                    amount = net,
                    color = if (net >= 0) IncomeColor else ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Selected day transactions ──────────────────────────────────────
        if (selectedDate != null && selectedDateTransactions.isNotEmpty()) {
            item {
                val dateLabel = selectedDate!!.let {
                    "${it.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())}, " +
                    "${it.dayOfMonth} ${it.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}"
                }
                BluffSectionHeader(
                    title = dateLabel,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            items(selectedDateTransactions) { tx ->
                TransactionRow(
                    transaction = tx,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MonthlySummaryPill(
    label: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                amount.toDisplayAmount(),
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
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
    val lastDay = yearMonth.atEndOfMonth().dayOfMonth
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // Mon=1, Sun=7
    val today = LocalDate.now()

    Column {
        val totalWeeks = ((firstDayOfWeek - 1 + lastDay) / 7) + 1
        for (week in 0 until totalWeeks) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dow in 1..7) {
                    val dayIndex = week * 7 + dow - (firstDayOfWeek - 1)
                    if (dayIndex in 1..lastDay) {
                        val date = yearMonth.atDay(dayIndex)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasTx = dayIndex in daysWithTransactions

                        val bgColor by animateColorAsState(
                            targetValue = when {
                                isSelected -> Primary
                                isToday -> Primary.copy(alpha = 0.2f)
                                else -> Color.Transparent
                            },
                            animationSpec = spring(),
                            label = "dayBg"
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(bgColor)
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayIndex.toString(),
                                    color = when {
                                        isSelected -> Color.Black
                                        isToday -> Primary
                                        else -> TextPrimary
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (hasTx) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.Black else Primary)
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
