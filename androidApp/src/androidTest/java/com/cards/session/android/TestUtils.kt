package com.cards.session.android

import com.cards.session.cards.models.BillingInformationDto
import com.cards.session.cards.sdk.CardSessions
import com.cards.session.cards.ui.CardSessionState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Test utility functions for E2E tests
 * Mirrors the structure of Flutter's TestHelpers class
 */
object TestUtils {
    
    /**
     * Create a valid billing information object with default or custom values
     */
    fun createValidBillingInfo(
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test.user@example.com",
        phoneNumber: String? = "+6281234567890",
        streetLine1: String = "123 Test Street",
        streetLine2: String? = null,
        city: String = "Jakarta",
        provinceState: String = "DKI Jakarta",
        postalCode: String = "12345",
        country: String = "ID"
    ): BillingInformationDto {
        return BillingInformationDto(
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber,
            streetLine1 = streetLine1,
            streetLine2 = streetLine2,
            city = city,
            provinceState = provinceState,
            postalCode = postalCode,
            country = country
        )
    }
    
    /**
     * Create valid card data map with default or custom values
     */
    fun createValidCardData(
        cardNumber: String = "4111111111111111",
        expiryMonth: String = "12",
        expiryYear: String = "2025",
        cvn: String = "123",
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test.user@example.com",
        phoneNumber: String = "+6281234567890",
        sessionId: String = TestData.exampleSessionId,
        confirmSave: Boolean = false,
        billingInfo: BillingInformationDto? = null
    ): Map<String, Any?> {
        return mapOf(
            "cardNumber" to cardNumber,
            "expiryMonth" to expiryMonth,
            "expiryYear" to expiryYear,
            "cvn" to cvn,
            "cardholderFirstName" to firstName,
            "cardholderLastName" to lastName,
            "cardholderEmail" to email,
            "cardholderPhoneNumber" to phoneNumber,
            "paymentSessionId" to sessionId,
            "confirmSave" to confirmSave,
            "billingInformation" to (billingInfo ?: createValidBillingInfo())
        )
    }
    
    /**
     * Wait for a specific state condition with timeout
     */
    suspend fun waitForState(
        cardSessions: CardSessions,
        condition: (CardSessionState) -> Boolean,
        timeout: Duration = 5.seconds
    ): CardSessionState? {
        return withTimeoutOrNull(timeout) {
            cardSessions.state.first { state ->
                condition(state)
            }
        }
    }
    
    /**
     * Collect states during an operation
     */
    suspend fun collectStates(
        cardSessions: CardSessions,
        action: suspend () -> Unit,
        collectDuration: Long = 500L
    ): List<CardSessionState> = coroutineScope {
        val states = mutableListOf<CardSessionState>()
        
        val job = launch {
            cardSessions.state.collect { state ->
                states.add(state)
            }
        }
        
        // Perform the action
        action()
        
        // Wait for states to be collected
        delay(collectDuration)
        
        // Cancel collection
        job.cancel()
        
        return@coroutineScope states
    }
    
    /**
     * Ensure SDK is initialized and ready
     */
    suspend fun ensureInitialized(
        cardSessions: CardSessions,
        delayMs: Long = TestData.initDelay
    ) {
        // Wait for initialization
        delay(delayMs)
        
        // Wait for non-loading state
        waitForState(
            cardSessions,
            condition = { !it.isLoading },
            timeout = 2.seconds
        )
    }
    
    /**
     * Get invalid card data scenarios for testing
     */
    fun getInvalidCardDataScenarios(): List<Map<String, Any>> {
        return listOf(
            mapOf(
                "description" to "Invalid card number",
                "data" to createValidCardData(cardNumber = "1234567890123456"),
                "expectedError" to "invalid_card_number"
            ),
            mapOf(
                "description" to "Expired card",
                "data" to createValidCardData(expiryYear = "2020"),
                "expectedError" to "card_expired"
            ),
            mapOf(
                "description" to "Invalid month",
                "data" to createValidCardData(expiryMonth = "13"),
                "expectedError" to "invalid_expiry_date"
            ),
            mapOf(
                "description" to "Invalid email",
                "data" to createValidCardData(email = "not-an-email"),
                "expectedError" to "invalid_email"
            ),
            mapOf(
                "description" to "Empty required fields",
                "data" to createValidCardData(firstName = "", lastName = ""),
                "expectedError" to "missing_required_fields"
            )
        )
    }
    
    /**
     * Get international billing addresses for testing
     */
    fun getInternationalBillingAddresses(): List<BillingInformationDto> {
        return listOf(
            createValidBillingInfo(
                country = "GB",
                postalCode = "SW1A 1AA",
                provinceState = "London",
                city = "London",
                streetLine1 = "10 Downing Street"
            ),
            createValidBillingInfo(
                country = "JP",
                postalCode = "100-0001",
                provinceState = "Tokyo",
                city = "Chiyoda",
                streetLine1 = "1-1-1 Chiyoda"
            ),
            createValidBillingInfo(
                country = "AU",
                postalCode = "2000",
                provinceState = "NSW",
                city = "Sydney",
                streetLine1 = "1 Martin Place"
            ),
            createValidBillingInfo(
                country = "CA",
                postalCode = "M5H 2N2",
                provinceState = "ON",
                city = "Toronto",
                streetLine1 = "100 King Street West"
            )
        )
    }
    
    /**
     * Verify response for success case
     */
    fun verifySuccessResponse(
        response: com.cards.session.cards.models.CardsResponseDto,
        expectPaymentToken: Boolean = true
    ): Boolean {
        return if (expectPaymentToken) {
            response.paymentTokenId?.isNotEmpty() == true
        } else {
            response.message?.contains("Status updated", ignoreCase = true) == true
        }
    }
    
    /**
     * Verify response for error case
     */
    fun verifyErrorResponse(
        response: com.cards.session.cards.models.CardsResponseDto,
        expectedErrorKeywords: List<String> = listOf("invalid", "error", "failed")
    ): Boolean {
        val message = response.message?.lowercase() ?: return false
        return expectedErrorKeywords.any { keyword ->
            message.contains(keyword)
        }
    }
}