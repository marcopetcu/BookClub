package com.example.bookclub.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.UserEntity
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.authRepository(app)

    private val _state = MutableStateFlow<UiState<UserEntity>>(UiState.Idle)
    val state: StateFlow<UiState<UserEntity>> = _state

    fun login(email: String, password: String) = viewModelScope.launch {
        if (email.isBlank() || password.isBlank()) {
            _state.value = UiState.Error(IllegalArgumentException("Email and password are required"))
            return@launch
        }
        _state.value = UiState.Loading
        try {
            val user = repo.login(email, password)
            _state.value = UiState.Success(user)
        } catch (t: Throwable) {
            _state.value = UiState.Error(t)
        }
    }

    fun reset() {
        _state.value = UiState.Idle
    }
}
