package com.example.myapplication.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.domain.model.LoggedInUser
import com.example.myapplication.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "secure_user_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _currentUser = MutableStateFlow<LoggedInUser?>(loadUserFromPrefs())
    val currentUser: StateFlow<LoggedInUser?> = _currentUser.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> get() = MutableStateFlow(_currentUser.value != null).asStateFlow()

    // ── Write ────────────────────────────────────────────────────────────────

    fun saveSession(userId: String, username: String, role: String, displayName: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_ROLE, role)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()
        _currentUser.value = LoggedInUser(
            id = userId,
            username = username,
            displayName = displayName,
            role = UserRole.fromString(role)
        )
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .remove(KEY_ROLE)
            .remove(KEY_DISPLAY_NAME)
            .apply()
        _currentUser.value = null
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    fun getLoggedInUser(): LoggedInUser? = _currentUser.value

    fun getRole(): UserRole = _currentUser.value?.role ?: UserRole.VIEWER

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun loadUserFromPrefs(): LoggedInUser? {
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val role = prefs.getString(KEY_ROLE, null) ?: return null
        val displayName = prefs.getString(KEY_DISPLAY_NAME, username) ?: username
        return LoggedInUser(
            id = userId,
            username = username,
            displayName = displayName,
            role = UserRole.fromString(role)
        )
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "user_role"
        private const val KEY_DISPLAY_NAME = "display_name"
    }
}
