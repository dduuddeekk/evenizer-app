package com.dudek.evenizer.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun formatLocaleDate(dateString: String, languageCode: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = inputFormat.parse(dateString) ?: return dateString
            
            val locale = Locale.forLanguageTag(languageCode)
            
            val pattern = when (languageCode) {
                "id" -> "d MMMM yyyy"    // 15 Juni 2024
                "zh" -> "yyyy年M月d日"   // 2024年6月15日
                "ru" -> "d MMMM yyyy 'г.'" // 15 июня 2024 г.
                "es" -> "d 'de' MMMM 'de' yyyy" // 15 de junio de 2024
                else -> "MMMM d, yyyy"   // June 15, 2024
            }
            
            val outputFormat = SimpleDateFormat(pattern, locale)
            outputFormat.format(date)
        } catch (_: Exception) {
            dateString
        }
    }

    fun formatLocaleDateTime(dateTimeString: String, languageCode: String): String {
        return try {
            // ISO 8601 format usually returned by APIs: 2024-06-15T10:00:00.000Z
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = inputFormat.parse(dateTimeString) ?: return dateTimeString
            
            val locale = Locale.forLanguageTag(languageCode)
            
            val pattern = when (languageCode) {
                "id" -> "d MMMM yyyy, HH:mm"
                "zh" -> "yyyy年M月d日 HH:mm"
                "ru" -> "d MMMM yyyy 'г.', HH:mm"
                "es" -> "d 'de' MMMM 'de' yyyy, HH:mm"
                else -> "MMMM d, yyyy, HH:mm"
            }
            
            val outputFormat = SimpleDateFormat(pattern, locale)
            outputFormat.format(date)
        } catch (_: Exception) {
            dateTimeString
        }
    }
}
