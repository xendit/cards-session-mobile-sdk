package com.cards.session.android.di

import com.cards.session.cards.network.CardsClient
import com.cards.session.cards.network.CardsPaymentSession
import com.cards.session.cards.network.KtorCardsClient
import com.cards.session.network.HttpClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

  @Provides
  @Singleton
  fun provideHttpClient(): HttpClient {
    return HttpClientFactory().create()
  }

  @Provides
  @Singleton
  fun provideCardsClient(httpClient: HttpClient): CardsClient {
    return KtorCardsClient(httpClient)
  }

  @Provides
  @Singleton
  fun provideCardsPaymentSession(client: CardsClient): CardsPaymentSession {
    return CardsPaymentSession(client)
  }
}