package com.example.bookclub.ui.books

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.model.BookSearchItem
import com.example.bookclub.data.model.BookWorkDetails
import com.example.bookclub.data.util.Result
import com.example.bookclub.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BooksViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ServiceLocator.booksRepository(app)

    // Search state (lista)
    private val _searchState = MutableStateFlow<UiState<List<BookSearchItem>>>(UiState.Idle)
    val searchState: StateFlow<UiState<List<BookSearchItem>>> = _searchState

    // Details state (work details)
    private val _detailsState = MutableStateFlow<UiState<BookWorkDetails>>(UiState.Idle)
    val detailsState: StateFlow<UiState<BookWorkDetails>> = _detailsState

    // Follow state (single source of truth)
    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed

    /* ---------- Search ---------- */
    fun search(query: String) = viewModelScope.launch {
        _searchState.value = UiState.Loading
        when (val res = repo.searchBooks(query)) {
            is Result.Ok  -> _searchState.value = UiState.Success(res.value)
            is Result.Err -> _searchState.value = UiState.Error(res.throwable)
        }
    }

    /* ---------- Details ---------- */
    fun loadWorkDetails(workId: String) = viewModelScope.launch {
        _detailsState.value = UiState.Loading
        when (val res = repo.getWorkDetails(workId)) {
            is Result.Ok  -> _detailsState.value = UiState.Success(res.value)
            is Result.Err -> _detailsState.value = UiState.Error(res.throwable)
        }
    }

    /* ---------- Follow / Unfollow (Varianta A) ---------- */
    fun loadFollowState(userId: Long, workId: String) = viewModelScope.launch {
        _isFollowed.value = repo.isFollowing(userId, workId)
    }

    fun followBook(userId: Long, workId: String) = viewModelScope.launch {
        repo.followBook(userId, workId)
        _isFollowed.value = true
    }

    fun unfollowBook(userId: Long, workId: String) = viewModelScope.launch {
        repo.unfollowBook(userId, workId)
        _isFollowed.value = false
    }
}
