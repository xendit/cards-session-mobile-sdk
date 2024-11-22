package com.cards.session.network

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

expect class HttpClientFactory() {
    fun create(): HttpClient
}

internal fun HttpClientConfig<*>.commonConfig() {
    install(ContentNegotiation) {
        json()
    }
}