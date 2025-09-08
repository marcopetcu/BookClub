package com.example.bookclub.data.util

import java.security.MessageDigest

// hash parola cu SHA-256
object PasswordHasher {
    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}