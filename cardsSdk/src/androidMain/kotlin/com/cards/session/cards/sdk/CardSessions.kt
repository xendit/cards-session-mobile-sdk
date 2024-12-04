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
    /**
     * Current state of the card session including loading, response and error states
     */
    val state: StateFlow<CardSessionState>

    /**
     * Collects card data from the user.
     * @param cardNumber Card number
     * @param expiryMonth Card expiry month (2 digits)
     * @param expiryYear Card expiry year (4 digits)
     * @param cvn Card verification number (optional)
     * @param cardholderFirstName Cardholder's first name
     * @param cardholderLastName Cardholder's last name
     * @param cardholderEmail Cardholder's email
     * @param cardholderPhoneNumber Cardholder's phone number
     * @param paymentSessionId Payment session ID from the backend
     * @return CardsResponseDto representing the current state of card data collection
     */
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

    /**
     * Collects CVN (Card Verification Number) from the user.
     * @param cvn Card verification number
     * @param paymentSessionId Session ID received from the backend
     * @return CardsResponseDto representing the current state of CVN collection
     */
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