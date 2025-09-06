package com.example.bookclub.ui.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.UserEntity
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.authRepository(app)

    private val _state = MutableStateFlow<UiState<UserEntity>>(UiState.Idle)
    val state: StateFlow<UiState<UserEntity>> = _state

    fun register(email: String, nickname: String, password: String, confirm: String) = viewModelScope.launch {
        if (email.isBlank() || nickname.isBlank() || password.isBlank() || confirm.isBlank()) {
            _state.value = UiState.Error(IllegalArgumentException("Please fill in all fields"))
            return@launch
        }
        if (!email.contains("@")) {
            _state.value = UiState.Error(IllegalArgumentException("Invalid email address"))
            return@launch
        }
        if (password.length < 6) {
            _state.value = UiState.Error(IllegalArgumentException("Password must be at least 6 characters"))
            return@launch
        }
        if (password != confirm) {
            _state.value = UiState.Error(IllegalArgumentException("Passwords do not match"))
            return@launch
        }
        _state.value = UiState.Loading
        try {
            val user = repo.register(email, nickname, password)
            _state.value = UiState.Success(user)
        } catch (t: Throwable) {
            _state.value = UiState.Error(t)
        }
    }

    fun reset() {
        _state.value = UiState.Idle
    }
}
