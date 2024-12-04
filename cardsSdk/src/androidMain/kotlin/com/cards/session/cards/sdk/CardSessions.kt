package com.cards.session.cards.sdk

import android.content.Context
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.ui.CardSessionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Core interface for card data collection functionality.
 * This interface provides methods to collect card data and CVN in a platform-independent way.
 */
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

    companion object {
        fun create(context: Context, apiKey: String): CardSessions {
            return CardSessionsImpl.create(context, apiKey)
        }
    }
} 