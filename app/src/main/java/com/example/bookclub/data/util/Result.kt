package com.example.bookclub.data.util

sealed class Result<out T> {
    data class Ok<T>(val value: T): Result<T>()
    data class Err(val throwable: Throwable): Result<Nothing>()
    inline fun <R> map(transform: (T)->R): Result<R> = when (this) {
        is Ok -> Ok(transform(value))
        is Err -> this
    }
}

inline fun <T> runResult(block: () -> T): Result<T> =
    try { Result.Ok(block()) } catch (t: Throwable) { Result.Err(t) }
