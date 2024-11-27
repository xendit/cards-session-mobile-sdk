package com.cards.session.cards.ui

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.network.CardsSessionError

data class CardSessionState(
  val cardResponse: CardsResponseDto? = null,
  val isLoading: Boolean = false,
  val error: CardsSessionError? = null
)