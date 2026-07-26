package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EnrollmentTrendPoint
import com.example.data.model.MonthlyFeeStat
import com.example.ui.theme.GhanaGoldAccent
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Monthly Fee Collection Bar Chart Component
 */
@Composable
fun MonthlyFeeBarChart(
    feeStats: List<MonthlyFeeStat>,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_fee_bar_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .size(32.dp)
                            .background(GhanaNavyPrimary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = GhanaNavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Monthly Fee Collection",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Target vs Collected (GH₵)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(GhanaGoldAccent, RoundedCornerShape(2.dp))
                        )
                        Text("Collected", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(GhanaNavyPrimary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                        )
                        Text("Target", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Interactive Callout
            val activeStat = selectedIndex?.let { if (it in feeStats.indices) feeStats[it] else null }
            if (activeStat != null) {
                Surface(
                    color = GhanaNavyPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activeStat.monthLabel} Performance:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            text = "GH₵ ${"%.0f".format(activeStat.collectedGhc)} / GH₵ ${"%.0f".format(activeStat.targetGhc)} (${"%.1f".format((activeStat.collectedGhc / activeStat.targetGhc) * 100)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GhanaNavyPrimary
                        )
                    }
                }
            }

            // Canvas Bar Drawing
            val maxVal = (feeStats.maxOfOrNull { maxOf(it.targetGhc, it.collectedGhc) } ?: 30000.0) * 1.15
            val goldColor = GhanaGoldAccent
            val navyMutedColor = GhanaNavyPrimary.copy(alpha = 0.25f)
            val navyColor = GhanaNavyPrimary

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .pointerInput(feeStats) {
                        detectTapGestures { offset ->
                            val sectionWidth = size.width / feeStats.size
                            val index = (offset.x / sectionWidth).toInt()
                            if (index in feeStats.indices) {
                                selectedIndex = index
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val bottomPadding = 28.dp.toPx()
                val topPadding = 16.dp.toPx()
                val chartHeight = height - bottomPadding - topPadding

                val count = feeStats.size
                val itemWidth = width / count

                // Draw background horizontal reference lines (e.g., 3 levels)
                val lineCount = 3
                for (i in 0..lineCount) {
                    val y = topPadding + (chartHeight * (1 - i.toFloat() / lineCount))
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bars per month
                feeStats.forEachIndexed { index, stat ->
                    val xCenter = (index * itemWidth) + (itemWidth / 2)
                    val barGroupWidth = itemWidth * 0.55f
                    val singleBarWidth = barGroupWidth / 2.1f

                    val targetHeight = (stat.targetGhc / maxVal).toFloat() * chartHeight
                    val collectedHeight = (stat.collectedGhc / maxVal).toFloat() * chartHeight

                    val targetTop = topPadding + (chartHeight - targetHeight)
                    val collectedTop = topPadding + (chartHeight - collectedHeight)

                    val targetX = xCenter - barGroupWidth / 2
                    val collectedX = targetX + singleBarWidth + 4.dp.toPx()

                    // Draw Target Bar
                    drawRoundRect(
                        color = navyMutedColor,
                        topLeft = Offset(targetX, targetTop),
                        size = Size(singleBarWidth, targetHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Draw Collected Bar
                    drawRoundRect(
                        color = if (selectedIndex == index) navyColor else goldColor,
                        topLeft = Offset(collectedX, collectedTop),
                        size = Size(singleBarWidth, collectedHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }

            // Month Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                feeStats.forEachIndexed { index, stat ->
                    Text(
                        text = stat.monthLabel,
                        fontSize = 11.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedIndex == index) GhanaNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedIndex = index }
                    )
                }
            }
        }
    }
}

/**
 * Enrollment Trend Line Chart Component
 */
@Composable
fun EnrollmentTrendLineChart(
    trendPoints: List<EnrollmentTrendPoint>,
    modifier: Modifier = Modifier
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("enrollment_trend_line_chart_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                            .size(32.dp)
                            .background(Color(0xFF2E7D32).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Student Enrollment Growth",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Term-by-Term Trajectory",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "+33.7% Growth",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Callout inspection
            val activePoint = selectedPointIndex?.let { if (it in trendPoints.indices) trendPoints[it] else null }
            if (activePoint != null) {
                Surface(
                    color = GhanaGoldAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePoint.periodLabel}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = GhanaNavyPrimary
                        )
                        Text(
                            text = "${activePoint.totalStudents} Total (JHS: ${activePoint.jhsStudents} • Primary: ${activePoint.primaryStudents})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GhanaNavyPrimary
                        )
                    }
                }
            }

            val minVal = (trendPoints.minOfOrNull { it.totalStudents } ?: 300) * 0.9f
            val maxVal = (trendPoints.maxOfOrNull { it.totalStudents } ?: 450) * 1.05f
            val navyColor = GhanaNavyPrimary
            val goldColor = GhanaGoldAccent

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .pointerInput(trendPoints) {
                        detectTapGestures { offset ->
                            val sectionWidth = size.width / trendPoints.size
                            val index = (offset.x / sectionWidth).toInt()
                            if (index in trendPoints.indices) {
                                selectedPointIndex = index
                            }
                        }
                    }
            ) {
                val width = size.width
                val height = size.height
                val bottomPadding = 24.dp.toPx()
                val topPadding = 16.dp.toPx()
                val chartHeight = height - bottomPadding - topPadding

                val count = trendPoints.size
                val itemWidth = width / (count - 1).coerceAtLeast(1)

                val points = trendPoints.mapIndexed { index, point ->
                    val x = index * itemWidth
                    val normalizedY = (point.totalStudents - minVal) / (maxVal - minVal)
                    val y = topPadding + (chartHeight * (1 - normalizedY.toFloat()))
                    Offset(x, y)
                }

                // Grid lines
                for (i in 0..3) {
                    val y = topPadding + (chartHeight * (1 - i / 3f))
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Gradient Area Fill
                val fillPath = Path().apply {
                    moveTo(points.first().x, height - bottomPadding)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height - bottomPadding)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            navyColor.copy(alpha = 0.35f),
                            navyColor.copy(alpha = 0.02f)
                        )
                    )
                )

                // Smooth Line
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cx = (p1.x + p2.x) / 2
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                }

                drawPath(
                    path = linePath,
                    color = navyColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Point Markers
                points.forEachIndexed { index, pt ->
                    val isSelected = selectedPointIndex == index
                    drawCircle(
                        color = if (isSelected) goldColor else Color.White,
                        radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = navyColor,
                        radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }
            }

            // Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendPoints.forEachIndexed { index, point ->
                    Text(
                        text = point.periodLabel,
                        fontSize = 10.sp,
                        fontWeight = if (selectedPointIndex == index) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedPointIndex == index) GhanaNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { selectedPointIndex = index }
                    )
                }
            }
        }
    }
}
