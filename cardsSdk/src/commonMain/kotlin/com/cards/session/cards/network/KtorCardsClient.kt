package com.cards.session.cards.network

import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.errors.IOException

class KtorCardsClient(
  private val httpClient: HttpClient
) : CardsClient {
  private val logger = Logger("KtorCardsClient")

  override suspend fun paymentWithSession(
    body: CardsRequestDto,
    authToken: String
  ): CardsResponseDto {
    return try {
      logger.i("Making payment session request with body: $body and authToken $authToken")
      val response: HttpResponse = httpClient.get {
        url("${NetworkConstants.BASE_URL}/payment_with_session")
        header("Authorization", "Bearer $authToken")
      }
      
      if (response.status.value in 200..299) {
        val responseBody = response.body<CardsResponseDto>()
        logger.i("Payment session request successful. Response: $responseBody")
        responseBody
      } else {
        val errorBody = response.body<CardsErrorResponse>()
        logger.e("Payment session request failed. Error: $errorBody")
        throw CardsSessionException(
          errorCode = when (errorBody.error_code) {
            "INVALID_TOKEN_ERROR" -> CardsSessionError.INVALID_TOKEN_ERROR
            "INVALID_OAUTH_TOKEN" -> CardsSessionError.INVALID_OAUTH_TOKEN
            else -> CardsSessionError.UNKNOWN_ERROR
          },
          errorMessage = errorBody.message
        )
      }
    } catch (e: IOException) {
      logger.e("Service unavailable", e)
      throw CardsSessionException(
        CardsSessionError.SERVICE_UNAVAILABLE,
        e.message ?: "Service Unavailable"
      )
    } catch (e: Exception) {
      logger.e("Unknown error occurred", e)
      throw CardsSessionException(
        CardsSessionError.UNKNOWN_ERROR,
        e.message ?: "Unknown Error"
      )
    }
  }
}