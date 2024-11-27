package com.cards.session.cards.network

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.util.Resource

class CardsPaymentSession(
  private val client: CardsClient,
) {
  suspend fun execute(authToken: String): Resource<CardsResponseDto> {
    return try {
      val cardsSession = client.paymentWithSession(authToken)
      Resource.Success(cardsSession)
    } catch (e: CardsSessionException) {
      e.printStackTrace()
      Resource.Error(e)
    }
  }
}