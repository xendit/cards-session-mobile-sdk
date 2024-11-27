package com.cards.session.di

import com.cards.session.cards.network.CardsClient
import com.cards.session.cards.network.CardsPaymentSession
import com.cards.session.cards.network.KtorCardsClient
import com.cards.session.network.HttpClientFactory

interface AppModule {
  val cardsClient: CardsClient
  val cardsPaymentSession: CardsPaymentSession
}

class AppModuleImpl : AppModule {
  override val cardsClient: CardsClient by lazy {
    KtorCardsClient(
      HttpClientFactory().create()
    )
  }

  override val cardsPaymentSession: CardsPaymentSession by lazy {
    CardsPaymentSession(cardsClient)
  }
}