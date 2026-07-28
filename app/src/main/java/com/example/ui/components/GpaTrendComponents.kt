package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhanaEmeraldGreen
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary
import com.example.util.GpaCalculator
import com.example.util.GpaCalculator.GpaTrendDirection
import com.example.util.GpaCalculator.StudentGpaTrend

/**
 * Visual Badge for GPA Trend Direction.
 */
@Composable
fun GpaTrendBadge(
    direction: GpaTrendDirection,
    delta: Double?,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, labelText) = when (direction) {
        GpaTrendDirection.IMPROVING -> {
            val formattedDelta = delta?.let { String.format("+%.2f", it) } ?: ""
            Quadruple(
                GhanaEmeraldGreen.copy(alpha = 0.15f),
                GhanaEmeraldGreen,
                Icons.Default.TrendingUp,
                "Improving ($formattedDelta GPA)"
            )
        }
        GpaTrendDirection.STEADY -> {
            Quadruple(
                Color(0xFF0288D1).copy(alpha = 0.15f),
                Color(0xFF0288D1),
                Icons.Default.TrendingFlat,
                "Steady (Consistent GPA)"
            )
        }
        GpaTrendDirection.DECLINING -> {
            val formattedDelta = delta?.let { String.format("%.2f", it) } ?: ""
            Quadruple(
                Color(0xFFD32F2F).copy(alpha = 0.15f),
                Color(0xFFD32F2F),
                Icons.Default.TrendingDown,
                "Declining ($formattedDelta GPA)"
            )
        }
        GpaTrendDirection.NEW_ENTRY -> {
            Quadruple(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
                Icons.Default.Timeline,
                "Initial Term Data"
            )
        }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = labelText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Custom Canvas Sparkline for Term-by-Term GPA Trajectory (0.0 to 4.0 scale).
 */
@Composable
fun GpaSparklineCanvas(
    term1Gpa: Double?,
    term2Gpa: Double?,
    term3Gpa: Double?,
    modifier: Modifier = Modifier
        .height(55.dp)
        .fillMaxWidth()
) {
    val points = listOfNotNull(
        term1Gpa?.let { Pair("T1", it) },
        term2Gpa?.let { Pair("T2", it) },
        term3Gpa?.let { Pair("T3", it) }
    )

    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No term history for trend graph", fontSize = 10.sp, color = Color.Gray)
        }
        return
    }

    val navyColor = GhanaNavyPrimary
    val greenColor = GhanaEmeraldGreen
    val goldColor = GhanaGoldAccent

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp, horizontal = 12.dp)
        ) {
            val width = size.width
            val height = size.height

            // Max GPA is 4.0, Min is 0.0
            fun getY(gpa: Double): Float {
                val clamped = gpa.coerceIn(0.0, 4.0)
                return height - ((clamped / 4.0f) * height).toFloat()
            }

            fun getX(index: Int): Float {
                if (points.size <= 1) return width / 2f
                return (width / (points.size - 1)) * index
            }

            if (points.size > 1) {
                val path = Path()
                val fillPath = Path()

                points.forEachIndexed { idx, pair ->
                    val x = getX(idx)
                    val y = getY(pair.second)
                    if (idx == 0) {
                        path.moveTo(x, y)
                        fillPath.moveTo(x, height)
                        fillPath.lineTo(x, y)
                    } else {
                        val prevX = getX(idx - 1)
                        val prevY = getY(points[idx - 1].second)
                        val controlX1 = prevX + (x - prevX) / 2f
                        val controlY1 = prevY
                        val controlX2 = prevX + (x - prevX) / 2f
                        val controlY2 = y

                        path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    }
                }

                fillPath.lineTo(getX(points.size - 1), height)
                fillPath.close()

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(navyColor.copy(alpha = 0.25f), Color.Transparent)
                    )
                )

                drawPath(
                    path = path,
                    color = navyColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // Draw data dots and values
            points.forEachIndexed { idx, pair ->
                val x = getX(idx)
                val y = getY(pair.second)

                drawCircle(
                    color = goldColor,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = navyColor,
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        // Labels under sparkline
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEach { (term, gpa) ->
                Text(
                    text = "$term: ${String.format("%.2f", gpa)} GPA",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = GhanaNavyPrimary
                )
            }
        }
    }
}

/**
 * Overview Card summarizing Class-Wide GPA trends.
 */
@Composable
fun ClassGpaTrendOverviewCard(
    summary: GpaCalculator.ClassGpaTrendSummary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth().testTag("class_gpa_trend_overview_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = GhanaNavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "GPA Trend Analytics — ${summary.className}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            text = "Term-over-Term performance trajectory across 4.0 scale",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GhanaGoldAccent.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = "Class Avg: ${String.format("%.2f", summary.classAverageGpa)} GPA",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = GhanaNavyPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Multi-Term Class Average Progression Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Term 1 Avg GPA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            text = summary.term1AverageGpa?.let { String.format("%.2f", it) } ?: "--",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GhanaNavyPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Term 2 Avg GPA", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        Text(
                            text = summary.term2AverageGpa?.let { String.format("%.2f", it) } ?: "--",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GhanaEmeraldGreen.copy(alpha = 0.15f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Term 3 Avg GPA", fontSize = 10.sp, color = GhanaEmeraldGreen)
                        Text(
                            text = summary.term3AverageGpa?.let { String.format("%.2f", it) } ?: "--",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = GhanaEmeraldGreen
                        )
                    }
                }
            }

            // Student Trend Velocity Distribution Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GhanaEmeraldGreen,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = "${summary.improvingStudentsCount} Improving 📈",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GhanaEmeraldGreen
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0288D1),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = "${summary.steadyStudentsCount} Steady ➡️",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0288D1)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Text(
                        text = "${summary.decliningStudentsCount} Support Needed 📉",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                }
            }
        }
    }
}
