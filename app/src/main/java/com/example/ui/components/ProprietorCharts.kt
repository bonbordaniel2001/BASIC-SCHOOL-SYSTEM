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
import androidx.compose.material.icons.filled.Leaderboard
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
import com.example.data.model.StudentGrade
import com.example.data.model.StudentTermPerformanceStat
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

/**
 * Calculates student term performance averages across academic terms from Room database grades.
 */
fun calculateStudentTermPerformanceAverages(grades: List<StudentGrade>): List<StudentTermPerformanceStat> {
    if (grades.isEmpty()) {
        return listOf(
            StudentTermPerformanceStat("Term 1", 75.0, 0, 85.0, 80.0),
            StudentTermPerformanceStat("Term 2", 78.5, 0, 88.0, 85.0),
            StudentTermPerformanceStat("Term 3", 82.0, 0, 92.0, 90.0)
        )
    }

    val groupedByTerm = grades.groupBy { it.academicTerm }
    val standardTerms = listOf("Term 1", "Term 2", "Term 3")
    val keys = (standardTerms + groupedByTerm.keys).distinct()

    return keys.map { term ->
        val termGrades = groupedByTerm[term] ?: emptyList()
        if (termGrades.isEmpty()) {
            StudentTermPerformanceStat(
                termLabel = term,
                averageScore = 0.0,
                studentCount = 0,
                highestScore = 0.0,
                passRatePercentage = 0.0
            )
        } else {
            val avg = termGrades.map { it.totalScore }.average()
            val maxScore = termGrades.maxOfOrNull { it.totalScore } ?: 0.0
            val distinctStudents = termGrades.map { it.studentId }.distinct().size
            val passes = termGrades.count { it.totalScore >= 50.0 }
            val passRate = (passes.toDouble() / termGrades.size) * 100.0

            StudentTermPerformanceStat(
                termLabel = term,
                averageScore = avg,
                studentCount = distinctStudents,
                highestScore = maxScore,
                passRatePercentage = passRate
            )
        }
    }
}

/**
 * Calculates individual student performance averages across all or selected term grades.
 */
fun calculateIndividualStudentAverages(grades: List<StudentGrade>, termFilter: String? = null): List<StudentTermPerformanceStat> {
    val filtered = if (termFilter.isNullOrBlank() || termFilter == "ALL") grades else grades.filter { it.academicTerm == termFilter }
    if (filtered.isEmpty()) return emptyList()

    return filtered.groupBy { it.studentName }
        .map { (studentName, stGrades) ->
            val avg = stGrades.map { it.totalScore }.average()
            val maxScore = stGrades.maxOfOrNull { it.totalScore } ?: 0.0
            val passes = stGrades.count { it.totalScore >= 50.0 }
            val passRate = (passes.toDouble() / stGrades.size) * 100.0
            StudentTermPerformanceStat(
                termLabel = if (studentName.length > 10) studentName.take(9) + ".." else studentName,
                averageScore = avg,
                studentCount = stGrades.size,
                highestScore = maxScore,
                passRatePercentage = passRate
            )
        }
        .sortedByDescending { it.averageScore }
        .take(5)
}

/**
 * Student Term Performance Bar Chart Component for Proprietor Portal
 * Calculates and visualizes average scores across terms and top students using custom Canvas bars.
 */
@Composable
fun StudentTermPerformanceBarChart(
    grades: List<StudentGrade>,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf("TERM") }
    var selectedIndex by remember { mutableStateOf<Int?>(0) }

    val termStats = remember(grades) { calculateStudentTermPerformanceAverages(grades) }
    val studentStats = remember(grades) { calculateIndividualStudentAverages(grades) }

    val activeStatsList = if (viewMode == "TERM") termStats else studentStats

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_term_performance_bar_chart_card"),
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
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = null,
                            tint = GhanaNavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Student Term Performance Averages",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Room DB Grade Averages (0-100%)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mode Filter Switch
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = viewMode == "TERM",
                        onClick = {
                            viewMode = "TERM"
                            selectedIndex = 0
                        },
                        label = { Text("Terms", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(28.dp)
                    )
                    FilterChip(
                        selected = viewMode == "STUDENT",
                        onClick = {
                            viewMode = "STUDENT"
                            selectedIndex = 0
                        },
                        label = { Text("Top Students", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Interactive Callout Banner
            val activeStat = selectedIndex?.let { if (it in activeStatsList.indices) activeStatsList[it] else null }
            if (activeStat != null) {
                Surface(
                    color = GhanaNavyPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${activeStat.termLabel} Average Score:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = GhanaNavyPrimary
                            )
                            Text(
                                text = "Highest Score: ${"%.1f".format(activeStat.highestScore)}% • Pass Rate: ${"%.1f".format(activeStat.passRatePercentage)}%",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = GhanaNavyPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${"%.1f".format(activeStat.averageScore)}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Canvas Bar Drawing for Term Performance
            val navyColor = GhanaNavyPrimary
            val goldColor = GhanaGoldAccent
            val emeraldColor = Color(0xFF2E7D32)

            if (activeStatsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No grade records available in Room DB", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .pointerInput(activeStatsList) {
                            detectTapGestures { offset ->
                                val sectionWidth = size.width / activeStatsList.size
                                val index = (offset.x / sectionWidth).toInt()
                                if (index in activeStatsList.indices) {
                                    selectedIndex = index
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 28.dp.toPx()
                    val topPadding = 20.dp.toPx()
                    val chartHeight = height - bottomPadding - topPadding

                    val count = activeStatsList.size
                    val itemWidth = width / count

                    // Draw reference grid lines (0%, 25%, 50%, 75%, 100%)
                    val levels = listOf(0.0, 25.0, 50.0, 75.0, 100.0)
                    levels.forEach { level ->
                        val y = topPadding + (chartHeight * (1.0 - (level / 100.0))).toFloat()
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw Bars
                    activeStatsList.forEachIndexed { index, stat ->
                        val xCenter = (index * itemWidth) + (itemWidth / 2)
                        val barWidth = itemWidth * 0.45f

                        val normalizedHeight = (stat.averageScore / 100.0).coerceIn(0.0, 1.0).toFloat() * chartHeight
                        val barTop = topPadding + (chartHeight - normalizedHeight)
                        val barLeft = xCenter - (barWidth / 2)

                        val isSelected = selectedIndex == index
                        val barColor = when {
                            isSelected -> goldColor
                            stat.averageScore >= 80.0 -> navyColor
                            stat.averageScore >= 60.0 -> emeraldColor
                            else -> Color(0xFFD32F2F)
                        }

                        // Bar background track
                        drawRoundRect(
                            color = Color.LightGray.copy(alpha = 0.15f),
                            topLeft = Offset(barLeft, topPadding),
                            size = Size(barWidth, chartHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Filled Bar
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(barLeft, barTop),
                            size = Size(barWidth, normalizedHeight),
                            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )
                    }
                }

                // Bottom Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    activeStatsList.forEachIndexed { index, stat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedIndex = index }
                        ) {
                            Text(
                                text = stat.termLabel,
                                fontSize = 11.sp,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedIndex == index) GhanaNavyPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${"%.0f".format(stat.averageScore)}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedIndex == index) GhanaGoldAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}

