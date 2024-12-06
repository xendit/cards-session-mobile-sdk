package com.cards.session.cards.sdk

import android.content.Context
import android.util.Log
import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.models.DeviceFingerprint
import com.cards.session.cards.network.CardsSessionError.UNKNOWN_ERROR
import com.cards.session.cards.network.CardsSessionException
import com.cards.session.cards.network.KtorCardsClient
import com.cards.session.cards.ui.CardSessionState
import com.cards.session.network.HttpClientFactory
import com.cards.session.util.AuthTokenGenerator
import com.cardsession.sdk.CreditCardUtil
import com.xendit.fingerprintsdk.XenditFingerprintSDK
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class CardSessionsImpl private constructor(
  private val apiKey: String,
  private val httpClient: HttpClient
) : CardSessions {
  private val TAG = "CardSessionsImpl"
  private val client = KtorCardsClient(httpClient)
  private val _state = MutableStateFlow(CardSessionState())
  override val state: StateFlow<CardSessionState> = _state.asStateFlow()

  override suspend fun collectCardData(
    cardNumber: String,
    expiryMonth: String,
    expiryYear: String,
    cvn: String?,
    cardholderFirstName: String,
    cardholderLastName: String,
    cardholderEmail: String,
    cardholderPhoneNumber: String,
    paymentSessionId: String
  ): CardsResponseDto {
    _state.update { it.copy(isLoading = true, exception = null) }

    try {
      // Validate card data
      when {
        !CreditCardUtil.isCreditCardNumberValid(cardNumber) -> {
          throw IllegalArgumentException("Card number is invalid")
        }

        !CreditCardUtil.isCreditCardExpirationDateValid(expiryMonth, expiryYear) -> {
          throw IllegalArgumentException("Card expiration date is invalid")
        }

        !CreditCardUtil.isCreditCardCVNValid(cvn) -> {
          throw IllegalArgumentException("Card CVN is invalid")
        }
      }

      val deviceFingerprint = getFingerprint("collect_card_data")
      val request = CardsRequestDto(
        cardNumber = cardNumber,
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        cvn = cvn,
        cardholderFirstName = cardholderFirstName,
        cardholderLastName = cardholderLastName,
        cardholderEmail = cardholderEmail,
        cardholderPhoneNumber = cardholderPhoneNumber,
        paymentSessionId = paymentSessionId,
        device = DeviceFingerprint(deviceFingerprint)
      )

      val authToken = AuthTokenGenerator.generateAuthToken(apiKey)
      val response = client.paymentWithSession(request, authToken)
      _state.update { it.copy(isLoading = false, cardResponse = response) }
      return response
    } catch (e: CardsSessionException) {
      Log.e(TAG, "API request failed", e)
      _state.update { CardSessionState(isLoading = false, exception = e) }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    } catch (e: Exception) {
      Log.e(TAG, "API request failed", e)
      _state.update {
        CardSessionState(
          isLoading = false,
          exception = CardsSessionException(errorCode = UNKNOWN_ERROR, e.message ?: "Unknown error")
        )
      }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    }
  }

  override suspend fun collectCvn(
    cvn: String,
    paymentSessionId: String
  ): CardsResponseDto {
    _state.update { it.copy(isLoading = true, exception = null) }

    try {
      if (!CreditCardUtil.isCreditCardCVNValid(cvn)) {
        throw IllegalArgumentException("Card CVN is invalid")
      }

      val deviceFingerprint = getFingerprint("collect_cvn")
      val request = CardsRequestDto(
        cvn = cvn,
        paymentSessionId = paymentSessionId,
        device = DeviceFingerprint(deviceFingerprint)
      )

      val authToken = AuthTokenGenerator.generateAuthToken(apiKey)
      val response = client.paymentWithSession(request, authToken)
      _state.update { it.copy(isLoading = false, cardResponse = response) }
      return response
    } catch (e: CardsSessionException) {
      Log.e(TAG, "API request failed", e)
      _state.update { CardSessionState(isLoading = false, exception = e) }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    } catch (e: Exception) {
      Log.e(TAG, "API request failed", e)
      _state.update {
        CardSessionState(
          isLoading = false,
          exception = CardsSessionException(errorCode = UNKNOWN_ERROR, e.message ?: "Unknown error")
        )
      }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    }
  }

  private suspend fun getFingerprint(eventName: String): String =
    suspendCancellableCoroutine { continuation ->
      try {
        val sessionId = XenditFingerprintSDK.getSessionId()
        XenditFingerprintSDK.scan(
          customerEventName = eventName,
          customerEventID = sessionId,
          onSuccess = {
            continuation.resume(sessionId)
          },
          onError = { error ->
            Log.e(TAG, "Scan failed for event: $eventName, error: $error")
            // Even if there's an error, we still return the sessionId as per the original implementation
            continuation.resume(sessionId)
          }
        )
      } catch (e: Exception) {
        Log.e(TAG, "Exception during fingerprint collection", e)
        continuation.resume("") // Return empty string on failure
      }
    }

  companion object {
    fun create(context: Context, apiKey: String): CardSessions {
      try {
        XenditFingerprintSDK.init(context, apiKey)
      } catch (e: Exception) {
        Log.e("CardSessions", "Failed to initialize XenditFingerprintSDK", e)
      }
      return CardSessionsImpl(
        apiKey = apiKey,
        httpClient = HttpClientFactory().create()
      )
    }
  }
}
