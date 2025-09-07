// file: com/example/bookclub/ui/club/ClubsViewModel.kt
package com.example.bookclub.ui.club

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookclub.data.ServiceLocator
import com.example.bookclub.data.db.BookClubEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant

class ClubsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.clubsRepository(app)

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

    //pentru Join
    fun joinClub(userId: Long, clubId: Long) = viewModelScope.launch {
        repo.joinClub(userId, clubId)
    }
}
