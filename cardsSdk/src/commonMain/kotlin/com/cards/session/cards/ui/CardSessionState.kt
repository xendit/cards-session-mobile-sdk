package com.cards.session.cards.ui

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.network.CardsSessionException

data class CardSessionState(
  val cardResponse: CardsResponseDto? = null,
  val isLoading: Boolean = false,
  val exception: CardsSessionException? = null
)