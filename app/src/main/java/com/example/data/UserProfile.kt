package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val occupation: String = "",
    val city: String = "",
    val loginCount: Long = 1L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val lastLoginAt: Timestamp? = null,
)

data class RegistrationDraft(
    val fullName: String,
    val username: String,
    val occupation: String,
    val city: String,
)

object ProfileValidation {
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_.]+$")

    fun validateFullName(value: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.length < 2 -> "Nama lengkap minimal 2 karakter."
            trimmed.length > 80 -> "Nama lengkap maksimal 80 karakter."
            else -> null
        }
    }

    fun validateUsername(value: String): String? {
        val trimmed = value.trim()
        return when {
            trimmed.length < 3 -> "Username minimal 3 karakter."
            trimmed.length > 30 -> "Username maksimal 30 karakter."
            !USERNAME_REGEX.matches(trimmed) -> "Username hanya boleh berisi huruf, angka, titik, atau garis bawah."
            else -> null
        }
    }

    fun validateOccupation(value: String): String? {
        val trimmed = value.trim()
        return if (trimmed.length > 60) "Profesi/peran maksimal 60 karakter." else null
    }

    fun validateCity(value: String): String? {
        val trimmed = value.trim()
        return if (trimmed.length > 60) "Kota domisili maksimal 60 karakter." else null
    }

    fun suggestUsernameFromEmail(email: String, displayName: String): String {
        val rawBase = email.substringBefore("@").ifBlank { displayName }
        val sanitized = rawBase.lowercase()
            .replace(Regex("[^a-z0-9_.]"), "_")
            .trim('_', '.')
            .take(24)
        return if (sanitized.length >= 3) sanitized else "pengguna_samudra"
    }
}
