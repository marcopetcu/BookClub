// file: com/example/bookclub/ui/club/ClubsViewModel.kt
package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.BookClubEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

class ClubsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.clubsRepository(app)
    private val session = ServiceLocator.sessionManager(app) // ia userId curent
    private val userId: Long get() = session.currentUserId ?: 1L

    // all clubs + clubs for current user  -> UiClub (cu flag de membru)
    val uiClubs: StateFlow<List<UiClub>> =
        combine(
            repo.listAll(),            // Flow<List<BookClubEntity>>
            repo.listForUser(userId)   // Flow<List<BookClubEntity>>
        ) { all, mine ->
            val mineIds = mine.map { it.id }.toSet()
            all.map { club -> UiClub(club = club, isMember = club.id in mineIds) }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // dacă încă folosești direct lista simplă pe undeva
    val clubs: StateFlow<List<BookClubEntity>> =
        repo.listAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun createClub(
        adminId: Long,
        workId: String,
        title: String,
        author: String,
        coverUrl: String?,
        description: String?,
        startAt: Instant
    ) = viewModelScope.launch {
        repo.createClub(adminId, workId, title, author, coverUrl, description, startAt)
    }

    fun joinClub(clubId: Long) = viewModelScope.launch {
        repo.joinClub(userId, clubId)
    }

    fun leaveClub(clubId: Long) = viewModelScope.launch {
        repo.leaveClub(userId, clubId)
    }
}
