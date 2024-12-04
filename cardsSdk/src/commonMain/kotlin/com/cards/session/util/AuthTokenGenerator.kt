package com.cards.session.util

import io.ktor.util.encodeBase64

object AuthTokenGenerator {
    /**
     * Generates an auth token from an API key using Basic Auth format
     * Format: Base64(apiKey:)
     * @param apiKey The API key to encode
     * @return The encoded auth token
     */
    fun generateAuthToken(apiKey: String): String {
        // Format as "apiKey:" (note the colon at the end with no password)
        val credentials = "$apiKey:"
        return credentials.encodeBase64()
    }
} 