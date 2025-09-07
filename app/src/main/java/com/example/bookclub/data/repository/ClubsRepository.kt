package com.example.bookclub.data.repository

import com.example.bookclub.data.db.BookClubEntity
import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.MembershipEntity
import com.example.bookclub.data.db.dao.BookClubDao
import com.example.bookclub.data.db.dao.FollowBookDao
import com.example.bookclub.data.db.dao.InboxDao
import com.example.bookclub.data.db.dao.MembershipDao
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow
import java.time.Duration
import java.time.Instant

class ClubsRepository(
    private val clubDao: BookClubDao,
    private val membershipDao: MembershipDao,
    private val followBookDao: FollowBookDao,
    private val inboxDao: InboxDao
) {
    fun listAll(): Flow<List<BookClubEntity>> = clubDao.getAllOrderByStart()

    /** DAO adaugă wildcard în SQL, deci trimitem DOAR query-ul */
    fun search(query: String): Flow<List<BookClubEntity>> = clubDao.search(query)

    fun listForUser(userId: Long): Flow<List<BookClubEntity>> =
        membershipDao.getClubsForUser(userId)

    fun listForFollowedBooks(userId: Long): Flow<List<BookClubEntity>> =
        clubDao.listForFollowedBooks(userId)

    /**
     * Creează un club și returnează id-ul.
     * - blochează dublurile ACTIVE (SCHEDULED/LIVE) pe același workId
     * - status: SCHEDULED dacă startAt > now, altfel LIVE
     * - closeAt = startAt + 72h
     * - notifică followerii cărții în Inbox
     */
    suspend fun createClub(
        adminId: Long,
        workId: String,
        title: String,
        author: String,          // non-null în entitate
        coverUrl: String?,
        description: String?,
        startAt: Instant
    ): Long {
        if (clubDao.existsActiveForWork(workId)) {
            throw IllegalStateException("Active club already exists for this work")
        }

        val now = Instant.now()
        val status = if (startAt.isAfter(now)) ClubStatus.SCHEDULED else ClubStatus.LIVE
        val closeAt = startAt.plus(Duration.ofHours(72))

        val clubId = clubDao.insert(
            BookClubEntity(
                id = 0L,
                workId = workId,
                title = title,
                author = author,
                coverUrl = coverUrl,
                description = description,
                createdBy = adminId,    // denumirea din entitate
                status = status,
                startAt = startAt,      // Instant
                closeAt = closeAt       // Instant
            )
        )

        // Notifică followerii cărții
        val followers = followBookDao.getFollowerIdsForWork(workId)
        followers.forEach { uid ->
            inboxDao.insert(
                InboxEntity(
                    userId = uid,
                    type = "NEW_CLUB_FOR_FOLLOWED_BOOK",
                    payloadJson = """{"clubId":$clubId,"workId":"$workId","title":"$title"}""",
                    isRead = false,
                    createdAt = now
                )
            )
        }
        return clubId
    }

    suspend fun joinClub(userId: Long, clubId: Long) {
        membershipDao.upsert(MembershipEntity(clubId = clubId, userId = userId))
        inboxDao.insert(
            InboxEntity(
                userId = userId,
                type = "JOIN_CONFIRMED",
                payloadJson = """{"clubId":$clubId}""",
                isRead = false,
                createdAt = Instant.now()
            )
        )
    }

    suspend fun leaveClub(userId: Long, clubId: Long) {
        membershipDao.delete(userId, clubId)
    }
}
