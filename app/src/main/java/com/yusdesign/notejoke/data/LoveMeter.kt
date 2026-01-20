package com.yusdesign.notejoke.data

import java.util.Date

data class LoveMeter(
    val id: String,          // Unique ID like commit hash "a86b67b"
    val status: String,      // "LIKE", "LOVE", "SEGFAULT", etc.
    val chance: Double,      // e.g., 42.2
    val timestamp: Date,     // Parsed from "21/01/2026 01:42"
    val rawLine: String      // Original line text
)
