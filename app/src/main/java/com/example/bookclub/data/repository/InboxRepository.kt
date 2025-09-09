// file: com/example/bookclub/data/repository/InboxRepository.kt
package com.example.bookclub.data.repository

import com.example.bookclub.data.db.InboxEntity
import com.example.bookclub.data.db.dao.InboxDao
import com.example.bookclub.ui.inbox.InboxUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import java.time.Instant

/**
 * Informațiile de carte (title/cover) NU există în payloadJson.
 * Completăm dintr-o sursă externă (ex. ClubsRepository/ClubDao) prin [clubLookup].
 */
class InboxRepository(
    private val inboxDao: InboxDao,
    // injectezi aici un lookup real: { id -> clubsRepository.getLiteById(id) }
    private val clubLookup: suspend (Long) -> ClubLite? = { _ -> null }
) {

    fun listUiForUser(userId: Long): Flow<List<InboxUi>> =
        inboxDao.listForUser(userId).map { list ->
            // transformare suspend pentru fiecare element, în paralel
            coroutineScope {
                list.map { entity ->
                    async { entity.toUi(clubLookup) }
                }.map { it.await() }
            }
        }

    suspend fun markRead(id: Long) = inboxDao.markRead(id)
    suspend fun markAllRead(userId: Long) = inboxDao.markAllRead(userId)
}

/** Model minim pentru lookup-ul unui club (titlu + copertă). */
data class ClubLite(
    val title: String,
    val coverUrl: String?
)

/* -------------------- Helpers JSON -------------------- */

private fun JSONObject.optStringOrNull(key: String): String? =
    optString(key).takeIf { it.isNotBlank() }

private fun JSONObject.optLongPositive(key: String): Long? =
    optLong(key, 0L).takeIf { it > 0 }

/* -------------------- Mapping InboxEntity -> InboxUi -------------------- */

private suspend fun InboxEntity.toUi(
    clubLookup: suspend (Long) -> ClubLite?
): InboxUi {
    val p = try {
        JSONObject(payloadJson ?: "{}")
    } catch (_: Throwable) {
        JSONObject()
    }

    val clubId: Long = p.optLongPositive("clubId") ?: 0L

    // Din payload (dacă ar exista)
    var titleFromPayload: String? = p.optStringOrNull("title")
    var coverFromPayload: String? = p.optStringOrNull("coverUrl")

    // startAt (dacă apare cândva în payload)
    val startAt: Instant? = p.optStringOrNull("startAt")
        ?.let { runCatching { Instant.parse(it) }.getOrNull() }

    // Dacă payload-ul NU conține titlul/cover, încercăm să le luăm din lookup
    if (titleFromPayload.isNullOrBlank() || coverFromPayload.isNullOrBlank()) {
        val club = if (clubId > 0) runCatching { clubLookup(clubId) }.getOrNull() else null
        if (titleFromPayload.isNullOrBlank()) titleFromPayload = club?.title
        if (coverFromPayload.isNullOrBlank()) coverFromPayload = club?.coverUrl
    }

    val finalTitle = titleFromPayload ?: if (clubId > 0) "Club #$clubId" else "Untitled"

    return InboxUi(
        id = id,
        clubId = clubId,
        title = finalTitle,
        coverUrl = coverFromPayload,
        startAt = startAt,
        createdAt = createdAt,
        isRead = isRead
    )
}
