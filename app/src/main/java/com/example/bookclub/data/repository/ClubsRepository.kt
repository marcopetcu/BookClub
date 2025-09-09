// file: com/example/bookclub/data/repository/ClubsRepository.kt
package com.example.bookclub.data.repository

import com.example.bookclub.data.db.*
import com.example.bookclub.data.db.dao.*
import com.example.bookclub.data.model.ClubComment
import com.example.bookclub.data.model.ClubStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

class ClubsRepository(
    private val clubDao: BookClubDao,
    private val membershipDao: MembershipDao,
    private val followBookDao: FollowBookDao,
    private val inboxDao: InboxDao,
    private val commentDao: CommentDao
) {
    fun listAll(): Flow<List<BookClubEntity>> = clubDao.getAllOrderByStart()
    fun search(query: String): Flow<List<BookClubEntity>> = clubDao.search(query)
    fun listForUser(userId: Long): Flow<List<BookClubEntity>> =
        membershipDao.getClubsForUser(userId)
    fun listForFollowedBooks(userId: Long): Flow<List<BookClubEntity>> =
        clubDao.listForFollowedBooks(userId)

    fun clubFlow(clubId: Long) = clubDao.getByIdFlow(clubId)

    // Flow cu setul clubId-urilor unde userul e membru (pentru UI)
    fun membershipsForUser(userId: Long): Flow<Set<Long>> =
        membershipDao.getClubIdsForUser(userId).map { it.toSet() }

    // Punctual
    suspend fun isMember(userId: Long, clubId: Long) = membershipDao.isMember(userId, clubId)

    /**
     * Creează clubul și notifică followerii cărții (inbox items) cu payload bogat:
     * type, clubId, workId, title, coverUrl, startAt.
     */
    suspend fun createClub(
        adminId: Long,
        workId: String,
        title: String,
        author: String,
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
                createdBy = adminId,
                status = status,
                startAt = startAt,
                closeAt = closeAt
            )
        )

        // notifică followerii cărții
        val followers = followBookDao.getFollowerIdsForWork(workId)

        val payload = """
            {
              "type":"NEW_CLUB_FOR_FOLLOWED_BOOK",
              "clubId":$clubId,
              "workId":${JSONObject.quote(workId)},
              "title":${JSONObject.quote(title)},
              "coverUrl":${JSONObject.quote(coverUrl ?: "")},
              "startAt":${JSONObject.quote(startAt.toString())}
            }
        """.trimIndent()

        val inboxNow = Instant.now()
        followers.forEach { uid ->
            inboxDao.insert(
                InboxEntity(
                    userId = uid,
                    type = "NEW_CLUB_FOR_FOLLOWED_BOOK",
                    payloadJson = payload,
                    isRead = false,
                    createdAt = inboxNow
                )
            )
        }

        return clubId
    }

    /**
     * Userul se alătură unui club -> trimitem un inbox item cu payload bogat
     * (ca să nu mai apară "Untitled" în Inbox).
     */
    suspend fun joinClub(userId: Long, clubId: Long) {
        membershipDao.upsert(MembershipEntity(clubId = clubId, userId = userId))

        val club = clubDao.getById(clubId)
        val payload = if (club != null) {
            """
            {
              "type":"JOIN_CONFIRMED",
              "clubId":$clubId,
              "title":${JSONObject.quote(club.title)},
              "coverUrl":${JSONObject.quote(club.coverUrl ?: "")},
              "startAt":${JSONObject.quote(club.startAt.toString())}
            }
            """.trimIndent()
        } else {
            // fallback minim dacă (teoretic) nu găsim clubul
            """{"type":"JOIN_CONFIRMED","clubId":$clubId}"""
        }

        inboxDao.insert(
            InboxEntity(
                userId = userId,
                type = "JOIN_CONFIRMED",
                payloadJson = payload,
                isRead = false,
                createdAt = Instant.now()
            )
        )
    }

    suspend fun leaveClub(userId: Long, clubId: Long) {
        membershipDao.delete(userId, clubId)
    }

    /* ===== Comentarii ===== */
    fun commentsFlow(clubId: Long): Flow<List<ClubComment>> =
        commentDao.getTopLevelWithAuthor(clubId).map { list ->
            list.map { e ->
                ClubComment(
                    id = e.id,
                    clubId = e.clubId,
                    userId = e.userId,
                    content = e.content,
                    authorName = e.authorNickname?.takeIf { it.isNotBlank() } ?: e.authorEmail,
                    createdAt = e.createdAt
                )
            }
        }

    suspend fun getClub(id: Long): BookClubEntity? = clubDao.getById(id)

    fun isLive(club: BookClubEntity): Boolean {
        val now = Instant.now()
        return now.isAfter(club.startAt) && now.isBefore(club.closeAt)
    }

    suspend fun addComment(
        clubId: Long,
        userId: Long,
        content: String,
        parentId: Long? = null
    ) {
        commentDao.insert(
            CommentEntity(
                id = 0L,
                clubId = clubId,
                userId = userId,
                content = content,
                createdAt = Instant.now(),
                parentId = parentId
            )
        )
    }

    /**
     * Mic helper: titlu + copertă pentru un club (poate fi folosit ca lookup).
     * (Dacă ai definit `ClubLite` în `InboxRepository`, îl poți reutiliza.)
     */
    suspend fun getLiteById(id: Long): ClubLite? =
        clubDao.getById(id)?.let { ClubLite(title = it.title, coverUrl = it.coverUrl) }
}
