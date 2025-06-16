package com.cards.session.cards.sdk

import android.content.Context
import com.cards.session.cards.models.CardsResponseDto
import com.cards.session.cards.models.BillingInformationDto
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
        paymentSessionId: String,
        confirmSave: Boolean = false,
        billingInformation: BillingInformationDto? = null
    ): CardsResponseDto

    suspend fun collectCvn(
        cvn: String,
        paymentSessionId: String
    ): CardsResponseDto

    companion object Factory
}

fun CardSessions.Factory.create(context: Context, apiKey: String): CardSessions{
    return CardSessionsImpl.create(context, apiKey)
}