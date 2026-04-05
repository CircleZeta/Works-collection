package com.example.healthplatform.presentation.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.healthplatform.domain.model.MetricType
import com.example.healthplatform.domain.model.TrendPoint
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun TrendsScreen(
    contentPadding: PaddingValues,
    viewModel: TrendsViewModel,
    refreshTick: Long
) {
    val uiState by viewModel.uiState.collectAsState()
    val userId = "demo_user"
    var lastLoadedRefreshTick by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(refreshTick) {
        if (lastLoadedRefreshTick != refreshTick) {
            lastLoadedRefreshTick = refreshTick
            viewModel.load(
                userId = userId,
                endTime = System.currentTimeMillis()
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Trends",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        item {
            if (uiState.loading) {
                CircularProgressIndicator()
            }
        }

        uiState.error?.let { message ->
            item {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        item {
            TrendSection(
                title = "Recent 7-Day Steps",
                points = uiState.stepTrend,
                unit = "steps"
            )
        }

        item {
            TrendSection(
                title = "Recent 7-Day Average Heart Rate",
                points = uiState.heartRateTrend,
                unit = "bpm"
            )
        }

        item {
            TrendSection(
                title = "Recent 7-Day Sleep Duration",
                points = uiState.sleepTrend,
                unit = "h"
            )
        }
    }
}

@Composable
private fun TrendSection(
    title: String,
    points: List<TrendPoint>,
    unit: String
) {
    val metricType = points.firstOrNull()?.metricType
    val latestValue = points.lastOrNull()?.value

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                latestValue?.let {
                    Text(
                        text = formatTrendValue(it, unit),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (points.isEmpty()) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                MetricLineChart(
                    points = points,
                    metricType = metricType ?: MetricType.STEPS
                )
                DayLabels(points = points)
            }
        }
    }
}

@Composable
private fun MetricLineChart(
    points: List<TrendPoint>,
    metricType: MetricType
) {
    val colorScheme = MaterialTheme.colorScheme
    val lineColor = lineColorForMetric(metricType, colorScheme)
    val trendVisual = remember(points, metricType, colorScheme) {
        trendVisualFor(points, metricType, colorScheme)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val chartLayout = remember(points, widthPx, heightPx) {
            buildChartLayout(points, widthPx, heightPx)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStrokeWidth = 1.dp.toPx()
                val lineStrokeWidth = 3.dp.toPx()
                val pointRadius = 4.dp.toPx()

                repeat(3) { index ->
                    val y = chartLayout.topPadding +
                        chartLayout.plotHeight * (index / 2f)
                    drawLine(
                        color = colorScheme.outlineVariant.copy(alpha = 0.4f),
                        start = Offset(chartLayout.leftPadding, y),
                        end = Offset(chartLayout.rightEdge, y),
                        strokeWidth = gridStrokeWidth
                    )
                }

                if (chartLayout.points.size > 1) {
                    val path = Path().apply {
                        moveTo(chartLayout.points.first().x, chartLayout.points.first().y)
                        chartLayout.points.drop(1).forEach { point ->
                            lineTo(point.x, point.y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = lineStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                chartLayout.points.lastOrNull()?.let { latestPoint ->
                    drawCircle(
                        color = lineColor,
                        radius = pointRadius,
                        center = latestPoint
                    )
                }
            }

            chartLayout.points.lastOrNull()?.let { latestPoint ->
                Text(
                    text = trendVisual.symbol,
                    color = trendVisual.color,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (latestPoint.x.roundToInt() + 10)
                                .coerceAtMost(widthPx.roundToInt() - 28),
                            y = (latestPoint.y.roundToInt() - 38)
                                .coerceIn(0, heightPx.roundToInt() - 40)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DayLabels(points: List<TrendPoint>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        points.forEachIndexed { index, point ->
            val alignment = when (index) {
                0 -> TextAlign.Start
                points.lastIndex -> TextAlign.End
                else -> TextAlign.Center
            }

            Text(
                text = point.dayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = alignment,
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 4.dp)
            )
        }
    }
}

private data class ChartLayout(
    val points: List<Offset>,
    val leftPadding: Float,
    val topPadding: Float,
    val plotHeight: Float,
    val rightEdge: Float
)

private enum class TrendDirection {
    UP,
    DOWN,
    STABLE
}

private data class TrendVisual(
    val symbol: String,
    val color: Color
)

private fun buildChartLayout(
    points: List<TrendPoint>,
    widthPx: Float,
    heightPx: Float
): ChartLayout {
    val leftPadding = 12f
    val topPadding = 16f
    val rightPadding = 28f
    val bottomPadding = 20f
    val plotWidth = (widthPx - leftPadding - rightPadding).coerceAtLeast(1f)
    val plotHeight = (heightPx - topPadding - bottomPadding).coerceAtLeast(1f)
    val values = points.map { it.value }
    val minValue = values.minOrNull() ?: 0.0
    val maxValue = values.maxOrNull() ?: minValue
    val baseRange = (maxValue - minValue).takeIf { it > 0.0 }
        ?: maxOf(abs(maxValue) * 0.1, 1.0)
    val expandedMin = minValue - (baseRange * 0.1)
    val expandedMax = maxValue + (baseRange * 0.1)
    val valueRange = (expandedMax - expandedMin).coerceAtLeast(1.0)

    val offsets = points.mapIndexed { index, point ->
        val xRatio = if (points.size == 1) {
            0.5f
        } else {
            index.toFloat() / points.lastIndex.toFloat()
        }
        val normalizedY = ((point.value - expandedMin) / valueRange)
            .toFloat()
            .coerceIn(0f, 1f)

        Offset(
            x = leftPadding + (plotWidth * xRatio),
            y = topPadding + (plotHeight * (1f - normalizedY))
        )
    }

    return ChartLayout(
        points = offsets,
        leftPadding = leftPadding,
        topPadding = topPadding,
        plotHeight = plotHeight,
        rightEdge = widthPx - rightPadding
    )
}

/* private fun trendVisualFor(
    points: List<TrendPoint>,
    metricType: MetricType,
    colorScheme: androidx.compose.material3.ColorScheme
): TrendVisual {
    val positiveTrendColor = Color(0xFF2E7D32)
    val negativeTrendColor = Color(0xFFC62828)

    return when (trendDirectionFor(points)) {
        TrendDirection.UP -> TrendVisual(
            symbol = "↑",
            color = when (metricType) {
                MetricType.HEART_RATE -> negativeTrendColor
                MetricType.STEPS, MetricType.SLEEP_DURATION -> positiveTrendColor
                else -> positiveTrendColor
            }
        )
        TrendDirection.DOWN -> TrendVisual(
            symbol = "↓",
            color = when (metricType) {
                MetricType.HEART_RATE -> positiveTrendColor
                MetricType.STEPS, MetricType.SLEEP_DURATION -> negativeTrendColor
                else -> negativeTrendColor
            }
        )
        TrendDirection.STABLE -> TrendVisual(
            symbol = "→",
            color = colorScheme.outline
        )
    }
} */

private fun trendVisualFor(
    points: List<TrendPoint>,
    metricType: MetricType,
    colorScheme: androidx.compose.material3.ColorScheme
): TrendVisual {
    val positiveTrendColor = Color(0xFF2E7D32)
    val negativeTrendColor = Color(0xFFC62828)

    return when (trendDirectionFor(points)) {
        TrendDirection.UP -> TrendVisual(
            symbol = "\u2191",
            color = when (metricType) {
                MetricType.HEART_RATE -> negativeTrendColor
                MetricType.STEPS, MetricType.SLEEP_DURATION -> positiveTrendColor
                else -> positiveTrendColor
            }
        )
        TrendDirection.DOWN -> TrendVisual(
            symbol = "\u2193",
            color = when (metricType) {
                MetricType.HEART_RATE -> positiveTrendColor
                MetricType.STEPS, MetricType.SLEEP_DURATION -> negativeTrendColor
                else -> negativeTrendColor
            }
        )
        TrendDirection.STABLE -> TrendVisual(
            symbol = "\u2192",
            color = colorScheme.outline
        )
    }
}

private fun trendDirectionFor(points: List<TrendPoint>): TrendDirection {
    if (points.isEmpty()) {
        return TrendDirection.STABLE
    }

    val recentPoints = points.takeLast(3)
    if (recentPoints.size == 1) {
        return TrendDirection.STABLE
    }

    val xs = recentPoints.indices.map { it.toDouble() }
    val ys = recentPoints.map { it.value }
    val xMean = xs.average()
    val yMean = ys.average()
    val numerator = xs.zip(ys).sumOf { (x, y) -> (x - xMean) * (y - yMean) }
    val denominator = xs.sumOf { (it - xMean) * (it - xMean) }
    val slope = if (denominator == 0.0) 0.0 else numerator / denominator
    val recentRange = (ys.maxOrNull() ?: yMean) - (ys.minOrNull() ?: yMean)
    val stabilityThreshold = if (recentRange == 0.0) 0.0 else recentRange * 0.05

    return when {
        abs(slope) <= stabilityThreshold -> TrendDirection.STABLE
        slope > 0 -> TrendDirection.UP
        else -> TrendDirection.DOWN
    }
}

private fun lineColorForMetric(
    metricType: MetricType,
    colorScheme: androidx.compose.material3.ColorScheme
): Color {
    return when (metricType) {
        MetricType.STEPS -> colorScheme.primary
        MetricType.HEART_RATE -> colorScheme.tertiary
        MetricType.SLEEP_DURATION -> colorScheme.secondary
        else -> colorScheme.primary
    }
}

private fun formatTrendValue(value: Double, unit: String): String {
    return when (unit) {
        "steps" -> "${value.toInt()} $unit"
        "bpm" -> "${value.toInt()} $unit"
        else -> String.format(Locale.US, "%.1f %s", value, unit)
    }
}
