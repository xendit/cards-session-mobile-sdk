package com.cards.session.cards.network

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.network.CardsSessionError.INVALID_OAUTH_TOKEN
import com.cards.session.cards.network.CardsSessionError.SERVICE_UNAVAILABLE
import com.cards.session.cards.network.CardsSessionError.UNKNOWN_ERROR
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.utils.io.errors.IOException

class KtorCardsClient(
  private val httpClient: HttpClient
) : CardsClient {
  override suspend fun paymentWithSession(authToken: String): CardsResponseDto {
    return try {
      httpClient.get {
        url("${NetworkConstants.BASE_URL}/payment_with_session")
        header("Authorization", "Bearer $authToken")
      }.body()
    } catch (e: IOException) {
      throw CardsSessionException(SERVICE_UNAVAILABLE, e.message ?: "Service Unavailable")
    } catch (e: Exception) {
      if (e.message?.lowercase()?.contains("invalid_oauth_token") == true) {
        throw CardsSessionException(INVALID_OAUTH_TOKEN, e.message ?: "Invalid OAuth Token")
      } else {
        throw CardsSessionException(UNKNOWN_ERROR, e.message ?: "Unknown Error")
      }
    }
  }
}