package com.cards.session.android.sdk

import android.util.Log
import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.network.KtorCardsClient
import com.cards.session.cards.ui.CardSessionState
import com.cards.session.util.Resource
import com.cardsession.sdk.CreditCardUtil
import com.xendit.fingerprintsdk.XenditFingerprintSDK
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

internal class CardSessionsImpl(
  private val authToken: String,
  httpClient: HttpClient = HttpClient()
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
    Log.d(TAG, "Starting collectCardData")
    _state.update { it.copy(isLoading = true, error = null) }

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
      Log.d(TAG, "Making API request")
      val request = CardsRequestDto(
        card_number = cardNumber,
        expiry_month = expiryMonth,
        expiry_year = expiryYear,
        cvn = cvn,
        cardholder_first_name = cardholderFirstName,
        cardholder_last_name = cardholderLastName,
        cardholder_email = cardholderEmail,
        cardholder_phone_number = cardholderPhoneNumber,
        payment_session_id = paymentSessionId,
        device_fingerprint = deviceFingerprint
      )

      val response = client.paymentWithSession(request, authToken)
      Log.d(TAG, "API request successful: $response")
      _state.update { it.copy(isLoading = false, cardResponse = response) }
      return response
    } catch (e: Exception) {
      Log.e(TAG, "API request failed", e)
      _state.update { it.copy(isLoading = false) }
      throw e
    }
  }

  override suspend fun collectCvn(
    cvn: String,
    paymentSessionId: String
  ): CardsResponseDto {
    Log.d(TAG, "Starting collectCvn")
    _state.update { it.copy(isLoading = true, error = null) }

    try {
      if (!CreditCardUtil.isCreditCardCVNValid(cvn)) {
        throw IllegalArgumentException("Card CVN is invalid")
      }

      val deviceFingerprint = getFingerprint("collect_cvn")
      Log.d(TAG, "Making API request for CVN")
      val request = CardsRequestDto(
        cvn = cvn,
        payment_session_id = paymentSessionId,
        device_fingerprint = deviceFingerprint
      )

      val response = client.paymentWithSession(request, authToken)
      Log.d(TAG, "API request successful: $response")
      _state.update { it.copy(isLoading = false, cardResponse = response) }
      return response
    } catch (e: Exception) {
      Log.e(TAG, "API request failed", e)
      _state.update { it.copy(isLoading = false) }
      throw e
    }
  }

  private suspend fun getFingerprint(eventName: String): String =
    suspendCancellableCoroutine { continuation ->
      try {
        Log.d(TAG, "Starting fingerprint collection for event: $eventName")
        val sessionId = XenditFingerprintSDK.getSessionId()
        Log.d(TAG, "Got session ID: $sessionId")

        XenditFingerprintSDK.scan(
          customerEventName = eventName,
          customerEventID = sessionId,
          onSuccess = {
            Log.d(TAG, "Scan successful for event: $eventName")
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
} 