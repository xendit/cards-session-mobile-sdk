package com.cards.session.cards.sdk

import cocoapods.XenditFingerprintSDK.FingerprintSDK
import cocoapods.XenditFingerprintSDK.LogModeAll
import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.models.DeviceFingerprint
import com.cards.session.cards.network.CardsSessionError.UNKNOWN_ERROR
import com.cards.session.cards.network.CardsSessionException
import com.cards.session.cards.network.KtorCardsClient
import com.cards.session.cards.ui.CardSessionState
import com.cards.session.network.HttpClientFactory
import com.cards.session.util.AuthTokenGenerator
import com.cards.session.util.Logger
import com.cardsession.sdk.CreditCardUtil
import io.ktor.client.HttpClient
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.Foundation.NSLog

internal class CardSessionsImpl @OptIn(ExperimentalForeignApi::class)
private constructor(
  private val apiKey: String,
  private val httpClient: HttpClient,
  private val fingerprintSDK: FingerprintSDK
) : CardSessions {
  private val TAG = "CardSessionsImpl"
  private val logger = Logger(TAG)
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
    paymentSessionId: String,
    confirmSave: Boolean
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
        confirmSave = confirmSave,
        device = DeviceFingerprint(deviceFingerprint)
      )

      val authToken = AuthTokenGenerator.generateAuthToken(apiKey)
      val response = client.paymentWithSession(request, authToken)
      _state.update { it.copy(isLoading = false, cardResponse = response) }
      return response
    } catch (e: CardsSessionException) {
      logger.e("API request failed: ${e.message}")
      _state.update { CardSessionState(isLoading = false, exception = e) }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    } catch (e: Exception) {
      logger.e("API request failed: ${e.message}")
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
      logger.e("API request failed: ${e.message}")
      _state.update { CardSessionState(isLoading = false, exception = e) }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    } catch (e: Exception) {
      logger.e("API request failed: ${e.message}")
      _state.update {
        CardSessionState(
          isLoading = false,
          exception = CardsSessionException(errorCode = UNKNOWN_ERROR, e.message ?: "Unknown error")
        )
      }
      return CardsResponseDto(message = e.message ?: "Unknown error")
    }
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun getFingerprint(eventName: String): String {
    fingerprintSDK.scanWithEvent_name(
      event_name = eventName,
      event_id = eventName,
      completion = { response, errorMsg ->
        if (errorMsg != null) {
          logger.e("Scan failed for event: $eventName, error: $errorMsg")
        }
      }
    )

    return fingerprintSDK.getSessionId()
  }

  companion object {
    @OptIn(ExperimentalForeignApi::class)
    fun create(apiKey: String, isEnableLog: Boolean = false): CardSessions {
      val fingerprint = FingerprintSDK()

      if (isEnableLog) {
              Logger("").debugBuild()
      }

      try {
        fingerprint.initSDKWithApiKey(apiKey)
      } catch (e: Exception) {
//        Logger("CardSessions").e( "Failed to initialize XenditFingerprintSDK", e)
      }

      return CardSessionsImpl(
        apiKey = apiKey,
        httpClient = HttpClientFactory().create(),
        fingerprintSDK = fingerprint
      )
    }
  }
} 
