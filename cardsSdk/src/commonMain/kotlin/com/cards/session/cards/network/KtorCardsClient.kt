package com.cards.session.cards.network

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.utils.io.errors.IOException

class KtorCardsClient(
  private val httpClient: HttpClient
) : CardsClient {
  private val logger = Logger("KtorCardsClient")

  override suspend fun paymentWithSession(authToken: String): CardsResponseDto {
    return try {
      logger.info("Making payment session request")
      val response = httpClient.get {
        url("${NetworkConstants.BASE_URL}/payment_with_session")
        header("Authorization", "Bearer $authToken")
      }.body<CardsResponseDto>()
      logger.info("Payment session request successful")
      response
    } catch (e: IOException) {
      logger.error("Service unavailable", e)
      throw CardsSessionException(
        CardsSessionError.SERVICE_UNAVAILABLE,
        e.message ?: "Service Unavailable"
      )
    } catch (e: Exception) {
      if (e.message?.lowercase()?.contains("invalid_oauth_token") == true) {
        logger.error("Invalid OAuth token", e)
        throw CardsSessionException(
          CardsSessionError.INVALID_OAUTH_TOKEN,
          e.message ?: "Invalid OAuth Token"
        )
      } else {
        logger.error("Unknown error occurred", e)
        throw CardsSessionException(CardsSessionError.UNKNOWN_ERROR, e.message ?: "Unknown Error")
      }
    }
  }
}