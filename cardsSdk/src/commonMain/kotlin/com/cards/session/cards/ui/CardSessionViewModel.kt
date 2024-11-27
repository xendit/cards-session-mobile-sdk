package com.cards.session.cards.ui

import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.network.CardsPaymentSession
import com.cards.session.cards.network.CardsSessionException
import com.cards.session.util.Resource
import com.cards.session.util.toCommonStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardSessionViewModel(
  private val cardsPaymentSession: CardsPaymentSession,
  coroutineScope: CoroutineScope? // prioritize scope from platform level, if any
) {
  private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

  private val _state = MutableStateFlow(CardSessionState())
  val state = _state.asStateFlow().toCommonStateFlow()

  fun onEvent(event: CardSessionEvent) {
    when (event) {
      is CardSessionEvent.CollectCardData -> {
        collectCardData(
          CardsRequestDto(
            card_number = event.cardNumber,
            expiry_month = event.expiryMonth,
            expiry_year = event.expiryYear,
            cardholder_first_name = event.cardholderFirstName,
            cardholder_last_name = event.cardholderLastName,
            cardholder_email = event.cardholderEmail,
            cardholder_phone_number = event.cardholderPhoneNumber,
            payment_session_id = event.paymentSessionId,
            device_fingerprint = event.deviceFingerprint
          )
        )
      }

      is CardSessionEvent.CollectCvn -> {
        collectCvn(
          CardsRequestDto(
            cvn = event.cvn,
            payment_session_id = event.paymentSessionId,
            device_fingerprint = event.deviceFingerprint
          )
        )
      }

      CardSessionEvent.RetryCardSession -> {
        // TODO: Implement retry logic
      }

      CardSessionEvent.OnErrorSeen -> {
        _state.update { it.copy(error = null) }
      }
    }
  }

  private fun collectCardData(request: CardsRequestDto) {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      when (val result = cardsPaymentSession.execute(request, "nbsp")) {
        is Resource.Success -> {
          _state.update {
            it.copy(
              cardResponse = result.data,
              isLoading = false,
              error = null
            )
          }
        }

        is Resource.Error -> {
          _state.update {
            it.copy(
              isLoading = false,
              error = (result.throwable as? CardsSessionException)?.errorCode
            )
          }
        }
      }
    }
  }

  private fun collectCvn(request: CardsRequestDto) {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      when (val result = cardsPaymentSession.execute(request, "nbcp")) {
        is Resource.Success -> {
          _state.update {
            it.copy(
              cardResponse = result.data,
              isLoading = false,
              error = null
            )
          }
        }

        is Resource.Error -> {
          _state.update {
            it.copy(
              isLoading = false,
              error = (result.throwable as? CardsSessionException)?.errorCode
            )
          }
        }
      }
    }
  }
}