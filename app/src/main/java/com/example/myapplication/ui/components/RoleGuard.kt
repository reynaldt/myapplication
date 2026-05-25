package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.UserRole
import org.koin.compose.koinInject

/**
 * Renders [content] only if the currently logged-in user satisfies [permission].
 * Optionally renders [fallback] otherwise.
 *
 * Usage:
 * ```kotlin
 * RequireRole({ it.canCheckOut() }) {
 *     Button(onClick = ...) { Text("Checkout") }
 * }
 * ```
 */
@Composable
fun RequireRole(
    permission: (UserRole) -> Boolean,
    role: UserRole? = null,
    fallback: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    val actualRole = role ?: run {
        val sessionManager: SessionManager = koinInject()
        val user by sessionManager.currentUser.collectAsState()
        user?.role ?: UserRole.VIEWER
    }

    if (permission(actualRole)) content() else fallback()
}
