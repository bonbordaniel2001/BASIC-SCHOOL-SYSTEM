package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GhanaNavyPrimary

/**
 * Renders CSV data in a clean, scrollable tabular format with headers, alternating rows,
 * and column grid lines as requested by the Proprietor.
 */
@Composable
fun CsvTablePreview(
    csvText: String,
    modifier: Modifier = Modifier
) {
    val tableData = remember(csvText) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() && !it.startsWith("===") }
        lines.map { line ->
            // Parse comma separated values, respecting quoted tokens
            parseCsvLine(line)
        }
    }

    if (tableData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No table data available to display.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val headerRow = tableData.firstOrNull() ?: emptyList()
    val dataRows = if (tableData.size > 1) tableData.drop(1) else emptyList()
    val horizontalScrollState = rememberScrollState()
    val totalTableWidth = remember(headerRow) {
        val calculated = headerRow.mapIndexed { colIdx, colHeader -> getColumnWidth(colHeader, colIdx) }
            .fold(0.dp) { acc, w -> acc + w }
        if (calculated > 0.dp) calculated else 360.dp
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth()
            .testTag("csv_table_preview")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(totalTableWidth)
                    .padding(4.dp)
            ) {
                // --- Table Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(GhanaNavyPrimary)
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    headerRow.forEachIndexed { colIdx, colHeader ->
                        val colWidth = getColumnWidth(colHeader, colIdx)
                        Box(
                            modifier = Modifier
                                .width(colWidth)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = colHeader,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // --- Table Rows ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    dataRows.forEachIndexed { rowIdx, rowCells ->
                        val isEven = rowIdx % 2 == 0
                        val rowBg = if (isEven) {
                            MaterialTheme.colorScheme.surface
                        } else {
                            GhanaNavyPrimary.copy(alpha = 0.04f)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .border(
                                    width = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            headerRow.forEachIndexed { colIdx, colHeader ->
                                val cellVal = rowCells.getOrNull(colIdx) ?: ""
                                val colWidth = getColumnWidth(colHeader, colIdx)
                                Box(
                                    modifier = Modifier
                                        .width(colWidth)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = cellVal,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (colIdx == 0 || colIdx == 1) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getColumnWidth(headerName: String, colIdx: Int): androidx.compose.ui.unit.Dp {
    return when {
        headerName.contains("Name", ignoreCase = true) || headerName.contains("Student", ignoreCase = true) -> 160.dp
        headerName.contains("Subject", ignoreCase = true) -> 130.dp
        headerName.contains("Class", ignoreCase = true) -> 110.dp
        headerName.contains("Index", ignoreCase = true) -> 120.dp
        headerName.contains("Date", ignoreCase = true) -> 100.dp
        headerName.contains("Status", ignoreCase = true) -> 90.dp
        headerName.contains("Score", ignoreCase = true) || headerName.contains("Grade", ignoreCase = true) -> 80.dp
        else -> 100.dp
    }
}

private fun parseCsvLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    var inQuotes = false
    val sb = StringBuilder()

    for (c in line) {
        when {
            c == '\"' -> inQuotes = !inQuotes
            c == ',' && !inQuotes -> {
                tokens.add(sb.toString().trim().removeSurrounding("\""))
                sb.clear()
            }
            else -> sb.append(c)
        }
    }
    tokens.add(sb.toString().trim().removeSurrounding("\""))
    return tokens
}
