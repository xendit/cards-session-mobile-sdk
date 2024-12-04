package com.cards.session.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// TODO move this to core module, instead of here

expect class HttpClientFactory() {
  fun create(): HttpClient
}

internal fun HttpClientConfig<*>.commonConfig() {
  install(ContentNegotiation) {
    json(Json {
      ignoreUnknownKeys = true
      isLenient = true
      encodeDefaults = true
      prettyPrint = true
    })
  }
  
  defaultRequest {
    contentType(ContentType.Application.Json)
  }
}