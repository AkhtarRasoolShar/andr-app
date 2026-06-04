package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Json

data class UserSession(
    val userId: Int,
    val name: String,
    val email: String,
    val role: String
)

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("snowwhite_user_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, name: String, email: String, role: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("name", name)
            putString("email", email)
            putString("role", role)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun fetchSession(): UserSession? {
        val isLoggedIn = prefs.getBoolean("is_logged_in", false)
        if (!isLoggedIn) return null
        
        val userId = prefs.getInt("user_id", 1)
        val name = prefs.getString("name", "") ?: ""
        val email = prefs.getString("email", "") ?: ""
        val role = prefs.getString("role", "customer") ?: "customer"
        return UserSession(userId, name, email, role)
    }

    fun clearSession() {
        prefs.edit()
            .remove("is_logged_in")
            .remove("user_id")
            .remove("name")
            .remove("email")
            .remove("role")
            .apply()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("biometric_enabled", enabled).apply()
    }

    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean("biometric_enabled", false)
    }

    // Secure token cache for restoring session
    fun cacheSecureSession(email: String, name: String, role: String) {
        prefs.edit().apply {
            putString("cached_email", email)
            putString("cached_name", name)
            putString("cached_role", role)
            apply()
        }
    }

    fun getCachedEmail(): String? = prefs.getString("cached_email", null)
    fun getCachedName(): String? = prefs.getString("cached_name", null)
    fun getCachedRole(): String? = prefs.getString("cached_role", null)
}
