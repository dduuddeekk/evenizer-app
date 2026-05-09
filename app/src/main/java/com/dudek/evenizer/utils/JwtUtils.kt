package com.dudek.evenizer.utils

import android.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

object JwtUtils {
    private val json = Json { ignoreUnknownKeys = true }

    fun getSubFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null
            
            val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charset.forName("UTF-8"))
            val jwtPayload = json.decodeFromString<JwtPayload>(payload)
            jwtPayload.sub
        } catch (e: Exception) {
            null
        }
    }

    @Serializable
    private data class JwtPayload(
        val sub: String? = null
    )
}
