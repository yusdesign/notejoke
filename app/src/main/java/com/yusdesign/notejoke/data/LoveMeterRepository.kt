package com.yusdesign.notejoke.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

class LoveMeterRepository(private val context: Context) {

    // Base URLs to check (website or raw text file)
    private val targetUrls = listOf(
        "https://yusdesign.github.io/lovepy/",
        "https://raw.githubusercontent.com/yusdesign/lovepy/main/result.txt"
    )

    suspend fun fetchLatestEntries(): List<LoveMeter> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<LoveMeter>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK)

        for (url in targetUrls) {
            try {
                val doc = Jsoup.connect(url).timeout(10000).get()
                // Logic to parse the page (explained below)
                val parsedEntries = parseHtmlContent(doc.html(), dateFormat)
                entries.addAll(parsedEntries)
                if (entries.isNotEmpty()) break // Stop if we got data from one source
            } catch (e: Exception) {
                // If website fails, maybe try the raw text file next
                continue
            }
        }
        return@withContext entries.distinctBy { it.id }.sortedByDescending { it.timestamp }
    }

    private fun parseResultTxt(content: String): List<LoveMeter> {
        val entries = mutableListOf<LoveMeter>()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.UK)
    
        // Parse result.txt format (one entry per line)
        content.lines().forEach { line ->
            if (line.isNotBlank()) {
                try {
                    // Format: "21/01/2026 01:42 💖 Love update: LIKE (42.2%) (1091810)"
                    val pattern = """(\d{2}/\d{2}/\d{4} \d{2}:\d{2})\s*💖 Love update:\s*(\w+)\s*\((\d+\.?\d*)%\)\s*\(([a-f0-9]+)\)"""
                    val regex = Regex(pattern)
                
                    regex.find(line)?.let { match ->
                        val (dateStr, status, chanceStr, hash) = match.destructured
                        val date = dateFormat.parse(dateStr)
                    
                        entries.add(LoveMeter(
                            id = hash,
                            status = status,
                            chance = chanceStr.toDouble(),
                            timestamp = date ?: Date(),
                            rawLine = line
                        ))
                    }
                } catch (e: Exception) {
                    // Skip invalid lines
                }
            }
        }
        return entries
    }

    
    private fun parseHtmlContent(html: String, dateFormat: SimpleDateFormat): List<LoveMeter> {
        val entries = mutableListOf<LoveMeter>()
        // This regex looks for the list items in the "Recent History" section
        val pattern = Regex("""<li>.*?(\d{2}/\d{2}/\d{4} \d{2}:\d{2})<br>\s*💖 Love update: (\w+) \((\d+\.?\d*)%\) \(([a-f0-9]+)\)""")

        val matches = pattern.findAll(html)
        matches.forEach { matchResult ->
            val (dateTimeStr, status, chanceStr, hash) = matchResult.destructured
            try {
                val date = dateFormat.parse(dateTimeStr)
                val loveMeter = LoveMeter(
                    id = hash,
                    status = status,
                    chance = chanceStr.toDouble(),
                    timestamp = date ?: Date(),
                    rawLine = matchResult.value
                )
                entries.add(loveMeter)
            } catch (e: Exception) {
                // Skip a line if parsing fails
            }
        }
        return entries
    }

    // Saves the last checked entry ID to SharedPreferences
    fun getLastNotifiedId(): String {
        val prefs = context.getSharedPreferences("notejoke_prefs", Context.MODE_PRIVATE)
        return prefs.getString("last_notified_id", "") ?: ""
    }

    fun setLastNotifiedId(id: String) {
        val prefs = context.getSharedPreferences("notejoke_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_notified_id", id).apply()
    }
}
