// TimeUtils.kt
package com.log3990.kapoot.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.*

object TimeUtils {
    @SuppressLint("ConstantLocale")
    private val timeFormatter = SimpleDateFormat("HH:mm:ss", getDefault())

    fun getCurrentTime(): String {
        return "[${timeFormatter.format(Date())}]"
    }

    fun processTime(rawTime: String): String {
        return try {
            val parsed = SimpleDateFormat("HH:mm", getDefault()).parse(rawTime)
            "[${parsed?.let { timeFormatter.format(it) }}]"
        } catch (e: Exception) {
            getCurrentTime()
        }
    }
}