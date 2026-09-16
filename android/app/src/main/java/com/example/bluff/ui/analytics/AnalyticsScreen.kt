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
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    fun selectPeriod(period: AnalyticsPeriod) {
        selectedPeriod = period
    }

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
                val container = AppContainer.instance
                AnalyticsViewModel(container.getAnalyticsUseCase)
            }
        }
    }
}

@Composable
fun AnalyticsScreen() {
    val vm: AnalyticsViewModel = viewModel(factory = AnalyticsViewModel.Factory)
    val summary = vm.analyticsState(vm.selectedPeriod)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
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

        // Period selector
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                items(AnalyticsPeriod.entries) { period ->
                    val selected = vm.selectedPeriod == period
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) Primary else CardColor,
                        modifier = Modifier.clickable { vm.selectPeriod(period) }
                    ) {
                        Text(
                            period.label,
                            color = if (selected) TextPrimary else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // Summary cards
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                SummaryCard(
                    label = "Spent",
                    amount = summary.totalSpentMinor,
                    color = ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Income",
                    amount = summary.totalIncomeMinor,
                    color = IncomeColor,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                SummaryCard(
                    label = "Saved",
                    amount = summary.totalSavingsMinor,
                    color = if (summary.totalSavingsMinor >= 0) IncomeColor else ExpenseColor,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Daily Avg",
                    amount = summary.averageDailySpendingMinor,
                    color = Primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(20.dp))
        }

        // Empty state when no data exists
        if (summary.totalSpentMinor == 0L && summary.totalIncomeMinor == 0L) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("No data for this period", color = TextSecondary, fontSize = 16.sp)
                    }
                }
            }
        }

        // Donut chart + breakdown
        if (summary.categoryBreakdown.isNotEmpty()) {
            item {
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = CardColor
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Spending by Category", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DonutChart(
                                categories = summary.categoryBreakdown,
                                modifier = Modifier.size(140.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                summary.categoryBreakdown.take(5).forEach { cat ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(Modifier.size(10.dp).clip(CircleShape).background(parseColor(cat.categoryColor)))
                                        Text(cat.categoryName, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                        Text("${cat.percentage.toInt()}%", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
        }

        // Insights
        if (summary.insights.isNotEmpty()) {
            item {
                Text("Insights", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(12.dp))
            }
            items(summary.insights) { insight ->
                val bgColor = when (insight.type) {
                    InsightType.POSITIVE -> IncomeColor.copy(alpha = 0.1f)
                    InsightType.WARNING -> WarningColor.copy(alpha = 0.1f)
                    InsightType.INFO -> CardColor
                }
                val textColor = when (insight.type) {
                    InsightType.POSITIVE -> IncomeColor
                    InsightType.WARNING -> WarningColor
                    InsightType.INFO -> TextSecondary
                }
                Surface(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor
                ) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(insight.icon, fontSize = 20.sp)
                        Text(insight.message, color = textColor, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Long, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = CardColor
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(amount.toDisplayAmount(), color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DonutChart(categories: List<CategorySpending>, modifier: Modifier = Modifier) {
    val colors = categories.map { parseColor(it.categoryColor) }
    Canvas(modifier = modifier) {
        var startAngle = -90f
        val stroke = 28f
        val inset = stroke / 2f
        val oval = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(inset, inset)

        categories.forEachIndexed { i, cat ->
            val sweep = (cat.percentage / 100f) * 360f
            drawArc(
                color = colors.getOrElse(i) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweep - 2f,
                useCenter = false,
                topLeft = topLeft,
                size = oval,
                style = Stroke(width = stroke)
            )
            startAngle += sweep
        }
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(hex.toColorInt())
    } catch (e: Exception) {
        Color.Gray
    }
}
