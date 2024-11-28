package com.cards.session.android.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cards.session.cards.network.CardsPaymentSession
import com.cards.session.cards.ui.CardSessionEvent
import com.cards.session.cards.ui.CardSessionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AndroidCardSessionViewModel @Inject constructor(
  private val cardsPaymentSession: CardsPaymentSession,
) : ViewModel() {

  private val viewModel by lazy {
    CardSessionViewModel(
      cardsPaymentSession = cardsPaymentSession,
      coroutineScope = viewModelScope
    )
  }

  val state = viewModel.state

  fun onEvent(event: CardSessionEvent) {
    viewModel.onEvent(event)
  }
}