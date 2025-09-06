package com.example.bookclub.data.network

import com.example.bookclub.data.model.BookSearchItem
import com.example.bookclub.data.model.BookWorkDetails

private fun coverUrlFromId(id: Int?, size: Char = 'M'): String? =
    id?.let { "https://covers.openlibrary.org/b/id/${it}-${size}.jpg" }

fun DocDto.toBookSearchItem(): BookSearchItem {
    val workId = (key ?: "").removePrefix("/works/")
    val author = authorName?.firstOrNull()
    return BookSearchItem(
        key = workId,
        title = title ?: workId,
        author = author,
        coverUrl = coverUrlFromId(coverId, 'M')
    )
}

fun WorkDetailsDto.toBookWorkDetails(workId: String): BookWorkDetails {
    val desc = when (val d = description) {
        is String -> d
        is Map<*, *> -> d["value"] as? String
        else -> null
    }
    val cover = covers?.firstOrNull()
    return BookWorkDetails(
        workId = workId,
        title = title ?: workId,
        description = desc,
        subjects = subjects ?: emptyList(),
        coverUrl = coverUrlFromId(cover, 'L')
    )
}
