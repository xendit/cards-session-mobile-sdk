package com.cards.session.cards.network

import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

class KtorCardsClient(
  private val httpClient: HttpClient
) : CardsClient {
  private val logger = Logger("KtorCardsClient")

  @OptIn(ExperimentalSerializationApi::class)
  override suspend fun paymentWithSession(
    body: CardsRequestDto,
    authToken: String
  ): CardsResponseDto {
    return try {
      val json = Json { explicitNulls = false }
      val jsonString = json.encodeToString(CardsRequestDto.serializer(), body)
      logger.i("Making payment session request with body: $jsonString")

      val response: HttpResponse = httpClient.post {
        url("${NetworkConstants.STG_URL}/payment_with_session")
        header("Authorization", "Basic $authToken")
        header("Content-Type", "application/json")
        setBody(jsonString)

        logger.i("Full request: ${this.url} with headers: ${this.headers}")
      }
      logger.i("PaymentResponse: $response")

      if (response.status.value in 200..299) {
        val responseBody = response.body<CardsResponseDto>()
        logger.i("Payment session request successful. Response: $responseBody")
        responseBody
      } else {
        val errorBody = response.body<CardsErrorResponse>()
        logger.e("Payment session request failed. Error ${errorBody.error_code}: $errorBody")
        throw CardsSessionException(
          errorCode = when (errorBody.error_code) {
            "SERVICE_UNAVAILABLE" -> CardsSessionError.SERVICE_UNAVAILABLE
            "INVALID_OAUTH_TOKEN" -> CardsSessionError.INVALID_OAUTH_TOKEN
            "INVALID_TOKEN_ERROR" -> CardsSessionError.INVALID_TOKEN_ERROR
            "SERVER_ERROR" -> CardsSessionError.SERVER_ERROR
            "API_VALIDATION_ERROR" -> CardsSessionError.API_VALIDATION_ERROR
            else -> CardsSessionError.UNKNOWN_ERROR
          },
          errorMessage = errorBody.message
        )
      }
    } catch (e: CardsSessionException) {
      throw e
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