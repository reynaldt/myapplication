package com.example.myapplication.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.SessionManager
import com.example.myapplication.domain.model.LoggedInUser
import com.example.myapplication.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: LoggedInUser) : LoginState()
    data class Error(val message: String) : LoginState()
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            authRepository.login(username.trim(), password)
                .onSuccess { user -> _loginState.value = LoginState.Success(user) }
                .onFailure { _loginState.value = LoginState.Error(it.message ?: "Login failed") }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
