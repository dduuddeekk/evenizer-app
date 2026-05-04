package com.dudek.evenizer.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    /**
     * Memformat string tanggal (yyyy-MM-dd) ke format yang sesuai dengan bahasa (Locale).
     * Contoh ID: 2024-06-15 -> 15 Juni 2024
     */
    fun formatLocaleDate(dateString: String, languageCode: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(dateString) ?: return dateString
            
            val locale = Locale.forLanguageTag(languageCode)
            
            // Format berbeda untuk setiap bahasa agar lebih natural
            val pattern = when (languageCode) {
                "id" -> "d MMMM yyyy"    // 15 Juni 2024
                "zh" -> "yyyy年M月d日"   // 2024年6月15日
                "ru" -> "d MMMM yyyy 'г.'" // 15 июня 2024 г.
                "es" -> "d 'de' MMMM 'de' yyyy" // 15 de junio de 2024
                else -> "MMMM d, yyyy"   // June 15, 2024
            }
            
            val outputFormat = SimpleDateFormat(pattern, locale)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}
