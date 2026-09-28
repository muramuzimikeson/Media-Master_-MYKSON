package com.example.document

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class CsvTableData(
    val headers: List<String>,
    val rows: List<List<String>>
)

data class SearchMatch(
    val lineIndex: Int,
    val startIndex: Int,
    val endIndex: Int,
    val preview: String
)

object DocumentParser {

    suspend fun readDocumentContent(context: Context, uriString: String, filePath: String?): String =
        withContext(Dispatchers.IO) {
            try {
                if (!filePath.isNullOrEmpty() && File(filePath).exists()) {
                    return@withContext File(filePath).readText()
                }
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BufferedReader(InputStreamReader(stream)).use { reader ->
                        reader.readText()
                    }
                } ?: "Error: Unable to open file stream."
            } catch (e: Exception) {
                "Error reading document: ${e.message}"
            }
        }

    fun parseCsv(rawCsv: String): CsvTableData {
        val lines = rawCsv.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return CsvTableData(emptyList(), emptyList())
        }

        fun parseLine(line: String): List<String> {
            val tokens = mutableListOf<String>()
            val sb = java.lang.StringBuilder()
            var inQuotes = false
            for (ch in line) {
                if (ch == '\"') {
                    inQuotes = !inQuotes
                } else if (ch == ',' && !inQuotes) {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                } else {
                    sb.append(ch)
                }
            }
            tokens.add(sb.toString().trim())
            return tokens
        }

        val headers = parseLine(lines[0])
        val rows = lines.drop(1).map { parseLine(it) }
        return CsvTableData(headers, rows)
    }

    fun formatJson(rawJson: String): String {
        return try {
            val trimmed = rawJson.trim()
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                rawJson
            }
        } catch (e: Exception) {
            rawJson
        }
    }

    fun searchMatches(content: String, query: String): List<SearchMatch> {
        if (query.isBlank()) return emptyList()
        val matches = mutableListOf<SearchMatch>()
        val lines = content.lines()
        val qLower = query.lowercase()

        lines.forEachIndexed { lineIdx, line ->
            var idx = line.lowercase().indexOf(qLower)
            while (idx != -1) {
                val start = (idx - 25).coerceAtLeast(0)
                val end = (idx + query.length + 25).coerceAtMost(line.length)
                val snippet = "..." + line.substring(start, end).trim() + "..."
                matches.add(SearchMatch(lineIdx, idx, idx + query.length, snippet))
                idx = line.lowercase().indexOf(qLower, idx + query.length)
            }
        }
        return matches
    }
}
