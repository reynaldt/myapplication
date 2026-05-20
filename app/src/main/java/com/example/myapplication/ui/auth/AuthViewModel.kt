package com.example.myapplication.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.LoggedInUser
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: LoggedInUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val currentUser: StateFlow<LoggedInUser?> = sessionManager.currentUser

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            authRepository.login(username.trim(), password)
                .onSuccess { user -> _authState.value = AuthUiState.Success(user) }
                .onFailure { _authState.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun logout() {
        authRepository.logout()
        _authState.value = AuthUiState.Idle
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}
