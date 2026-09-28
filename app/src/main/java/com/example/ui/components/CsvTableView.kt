package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.document.CsvTableData

@Composable
fun CsvTableView(
    data: CsvTableData,
    textColor: Color,
    surfaceColor: Color,
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()

    if (data.headers.isEmpty() && data.rows.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Empty spreadsheet data", color = textColor.copy(alpha = 0.6f))
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(horizontalScrollState)
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            // Header Row
            item {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    // Index column header
                    Box(
                        modifier = Modifier
                            .widthIn(min = 40.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    data.headers.forEach { header ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 220.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = header,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Data Rows
            itemsIndexed(data.rows) { rowIndex, row ->
                val isEven = rowIndex % 2 == 0
                val rowBg = if (isEven) surfaceColor else surfaceColor.copy(alpha = 0.8f)

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    // Row number
                    Box(
                        modifier = Modifier
                            .widthIn(min = 40.dp)
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rowIndex + 1}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }

                    row.forEach { cell ->
                        Box(
                            modifier = Modifier
                                .widthIn(min = 120.dp, max = 220.dp)
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cell,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
