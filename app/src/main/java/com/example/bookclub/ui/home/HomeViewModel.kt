package com.example.bookclub.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.bookclub.data.db.BookClubEntity

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val clubsRepo = ServiceLocator.clubsRepository(app)

    private val userId = MutableStateFlow<Long?>(null)
    fun setUser(id: Long) { userId.value = id }

    val allClubs: Flow<List<BookClubEntity>> =
        clubsRepo.listAll()

    val myClubs: Flow<List<BookClubEntity>> =
        userId.flatMapLatest { id -> id?.let { clubsRepo.listForUser(it) } ?: flowOf(emptyList()) }

    val followedUpcoming: Flow<List<BookClubEntity>> =
        userId.flatMapLatest { id -> id?.let { clubsRepo.listForFollowedBooks(it) } ?: flowOf(emptyList()) }

    fun joinClub(clubId: Long) = viewModelScope.launch {
        val uid = userId.value ?: return@launch
        clubsRepo.joinClub(uid, clubId)
    }

    fun leaveClub(clubId: Long) = viewModelScope.launch {
        val uid = userId.value ?: return@launch
        clubsRepo.leaveClub(uid, clubId)
    }
}
