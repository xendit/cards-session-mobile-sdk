package com.cards.session.cards.sdk

import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.ui.CardSessionState
import kotlinx.coroutines.flow.StateFlow

interface CardSessions {
    val state: StateFlow<CardSessionState>

    suspend fun collectCardData(
        cardNumber: String,
        expiryMonth: String,
        expiryYear: String,
        cvn: String?,
        cardholderFirstName: String,
        cardholderLastName: String,
        cardholderEmail: String,
        cardholderPhoneNumber: String,
        paymentSessionId: String
    ): CardsResponseDto

    suspend fun collectCvn(
        cvn: String,
        paymentSessionId: String
    ): CardsResponseDto

    companion object Factory
}

expect fun CardSessions.Factory.create(apiKey: String): CardSessions 