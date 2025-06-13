package com.cards.session.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpClientFactory {
  fun create(): HttpClient {
    return HttpClient(Android) {
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
  }
}