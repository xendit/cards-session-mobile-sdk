package com.cards.session.cards.network

import com.cards.session.cards.models.CardsRequestDto
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.util.Logger
import com.cards.session.util.Resource

class CardsPaymentSession(
  private val client: CardsClient,
  private val logger: Logger = Logger("CardsPaymentSession")
) {
  suspend fun execute(body: CardsRequestDto, authToken: String): Resource<CardsResponseDto> {
    return try {
      val cardsSession = client.paymentWithSession(body, authToken)
      logger.d("CardsSession: $cardsSession")
      Resource.Success(cardsSession)
    } catch (e: CardsSessionException) {
      logger.e("CardsSessionException: ${e.message}")
      Resource.Error(e)
    }
  }
}