// File: TimeUtils.kt
package com.log3990.kapoot.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    fun processTime(rawTime: String, isProduction: Boolean = true): String {
        return rawTime // Original implementation preserved
    }
}