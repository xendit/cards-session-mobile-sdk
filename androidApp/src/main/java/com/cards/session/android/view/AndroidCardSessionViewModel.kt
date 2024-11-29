package com.cards.session.android.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cards.session.cards.network.CardsPaymentSession
import com.cards.session.cards.ui.CardSessionEvent
import com.cards.session.cards.ui.CardSessionViewModel
import com.xendit.fingerprintsdk.XenditFingerprintSDK
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidCardSessionViewModel @Inject constructor(
  private val cardsPaymentSession: CardsPaymentSession,
) : ViewModel() {

  private val viewModel by lazy {
    // TODO user should enter proper api key
    CardSessionViewModel(
      cardsPaymentSession = cardsPaymentSession,
      coroutineScope = viewModelScope,
      apiKey = "TODO"
    )
  }

  val state = viewModel.state

  fun onEvent(event: CardSessionEvent) {
    when (event) {
      is CardSessionEvent.CollectCardData -> {
        val sessionId = XenditFingerprintSDK.getSessionId()
        XenditFingerprintSDK.scan(
          customerEventName = "collect_card_data",
          customerEventID = sessionId,
          onSuccess = {
            viewModel.onEvent(event.copy(
              paymentSessionId = sessionId,
              deviceFingerprint = sessionId
            ))
          },
          onError = { error ->
            viewModel.onEvent(event.copy(
              paymentSessionId = sessionId,
              deviceFingerprint = sessionId
            ))
          }
        )
      }
      is CardSessionEvent.CollectCvn -> {
        val sessionId = XenditFingerprintSDK.getSessionId()
        XenditFingerprintSDK.scan(
          customerEventName = "collect_cvn",
          customerEventID = sessionId,
          onSuccess = {
            viewModel.onEvent(event.copy(
              deviceFingerprint = sessionId
            ))
          },
          onError = { error ->
            viewModel.onEvent(event.copy(
              deviceFingerprint = sessionId
            ))
          }
        )
      }
      else -> viewModel.onEvent(event)
    }
  }
}