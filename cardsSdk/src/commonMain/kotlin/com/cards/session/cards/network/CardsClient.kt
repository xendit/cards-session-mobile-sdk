package com.cards.session.cards.network

import com.cards.session.cards.models.CardsResponseDto

interface CardsClient {
  suspend fun paymentWithSession(authToken: String): CardsResponseDto
}
