package com.example.bluff.ui.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.AnalyticsSummary
import com.example.bluff.domain.model.CategorySpending
import com.example.bluff.domain.model.InsightType
import com.example.bluff.domain.usecase.analytics.GetAnalyticsUseCase
import com.example.bluff.theme.*
import com.example.bluff.ui.util.toDisplayAmount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class AnalyticsPeriod(val label: String) {
    THIS_WEEK("Week"),
    THIS_MONTH("Month"),
    THREE_MONTHS("3 Mo"),
    SIX_MONTHS("6 Mo"),
    THIS_YEAR("Year")
}

class AnalyticsViewModel(
    private val getAnalyticsUseCase: GetAnalyticsUseCase
) : ViewModel() {

    var selectedPeriod by mutableStateOf(AnalyticsPeriod.THIS_MONTH)
        private set

    fun selectPeriod(period: AnalyticsPeriod) { selectedPeriod = period }

    fun getDateRange(period: AnalyticsPeriod): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        return when (period) {
            AnalyticsPeriod.THIS_WEEK -> Pair(now.minusDays(now.dayOfWeek.value.toLong() - 1), now)
            AnalyticsPeriod.THIS_MONTH -> Pair(now.withDayOfMonth(1), now.with(TemporalAdjusters.lastDayOfMonth()))
            AnalyticsPeriod.THREE_MONTHS -> Pair(now.minusMonths(3).withDayOfMonth(1), now)
            AnalyticsPeriod.SIX_MONTHS -> Pair(now.minusMonths(6).withDayOfMonth(1), now)
            AnalyticsPeriod.THIS_YEAR -> Pair(now.withDayOfYear(1), now.with(TemporalAdjusters.lastDayOfYear()))
        }
    }

    @Composable
    fun analyticsState(period: AnalyticsPeriod): AnalyticsSummary {
        val (start, end) = getDateRange(period)
        return getAnalyticsUseCase.getForPeriod(start, end)
            .collectAsStateWithLifecycle(initialValue = AnalyticsSummary()).value
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                AnalyticsViewModel(AppContainer.instance.getAnalyticsUseCase)
            }
        }
    }
}

@Composable
fun AnalyticsScreen() {
    val vm: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.Factory)
    val summary = vm.analyticsState(vm.selectedPeriod)
    val hasData = summary.totalSpentMinor > 0 || summary.totalIncomeMinor > 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // ── Title ────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(48.dp))
            Text(
                "Analytics",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

        // ── Period pills ──────────────────────────────────────────────────
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(AnalyticsPeriod.entries) { period ->
                    val selected = vm.selectedPeriod == period
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = if (selected) Color.White else CardColor,
                        modifier = Modifier.clickable { vm.selectPeriod(period) }
                    ) {
                        Text(
                            period.label,
                            color = if (selected) Color.Black else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── 4 summary cards ───────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsSummaryCard("Spent", summary.totalSpentMinor, ExpenseColor, Modifier.weight(1f))
                    AnalyticsSummaryCard("Income", summary.totalIncomeMinor, IncomeColor, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AnalyticsSummaryCard(
                        "Saved",
                        summary.totalSavingsMinor,
                        if (summary.totalSavingsMinor >= 0) IncomeColor else ExpenseColor,
                        Modifier.weight(1f)
                    )
                    AnalyticsSummaryCard("Daily Avg", summary.averageDailySpendingMinor, TextPrimary, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        // ── Empty state ───────────────────────────────────────────────────
        if (!hasData) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 52.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("No data for this period", color = TextSecondary, fontSize = 16.sp)
                        Text("Add some transactions to see analytics", color = TextTertiary, fontSize = 13.sp)
                    }
                }
            }
        }

        // ── Donut chart ───────────────────────────────────────────────────
        if (summary.categoryBreakdown.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = CardColor
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Spending by Category",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(20.dp))

                        // Centred donut
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChart(
                                categories = summary.categoryBreakdown,
                                modifier = Modifier.size(180.dp)
                            )
                        }

                        Spacer(Modifier.height(20.dp))

                        // Legend below
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            summary.categoryBreakdown.take(6).forEach { cat ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(parseColor(cat.categoryColor))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        cat.categoryName,
                                        color = TextSecondary,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${cat.percentage.toInt()}%",
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        cat.amountMinor.toDisplayAmount(),
                                        color = ExpenseColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // ── Insights ──────────────────────────────────────────────────────
        if (summary.insights.isNotEmpty()) {
            item {
                Text(
                    "Insights",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
            items(summary.insights) { insight ->
                val (bgColor, accentColor) = when (insight.type) {
                    InsightType.POSITIVE -> IncomeColor.copy(alpha = 0.08f) to IncomeColor
                    InsightType.WARNING -> WarningColor.copy(alpha = 0.08f) to WarningColor
                    InsightType.INFO -> SurfaceVariant to TextSecondary
                }
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(insight.icon, fontSize = 24.sp)
                        Text(
                            insight.message,
                            color = accentColor,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsSummaryCard(
    label: String,
    amount: Long,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardColor
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                amount.toDisplayAmount(),
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DonutChart(categories: List<CategorySpending>, modifier: Modifier = Modifier) {
    val colors = categories.map { parseColor(it.categoryColor) }
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val strokeWidth = 36f
        val inset = strokeWidth / 2f
        val oval = Size(size.width - strokeWidth, size.height - strokeWidth)
        val topLeft = Offset(inset, inset)

        categories.forEachIndexed { i, cat ->
            val sweep = (cat.percentage / 100f) * 360f
            drawArc(
                color = colors.getOrElse(i) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweep - 1.5f,
                useCenter = false,
                topLeft = topLeft,
                size = oval,
                style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            startAngle += sweep
        }
    }
}

private fun parseColor(hex: String): Color = try {
    Color(hex.toColorInt())
} catch (e: Exception) {
    Color.Gray
}
