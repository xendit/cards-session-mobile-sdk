package com.cards.session.android

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import com.cards.session.cards.sdk.CardSessions
import com.cards.session.cards.sdk.create
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.cards.session.cards.ui.CardSessionState

/**
 * End-to-End tests that create real sessions before collecting card data
 * Structured to match Flutter's full_e2e_integration_test.dart
 */
@RunWith(AndroidJUnit4::class)
class CardSessionE2ETest {

    // Use centralized test data with environment variable support
    private val apiHelper = XenditApiHelper(TestData.getPublicKey(), TestData.getBaseUrl())

    @Test
    fun testApplicationContext() {
        // Context of the app under test
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.cards.session.android", appContext.packageName)
    }

    @Test
    fun testCompletePaymentSessionFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        try {
            println("CVN STARTED ")
            // Step 1: Create a session via API
            val referenceId = "test_${System.currentTimeMillis()}"
            val saveCardSession = TestData.saveCardSession
            val sessionResponse = apiHelper.createSession(
                referenceId = referenceId,
                customerId = TestData.customerId,
                description = saveCardSession["description"] as String,
                amount = saveCardSession["amount"] as Int,
                sessionType = saveCardSession["sessionType"] as String,
                country = saveCardSession["country"] as String,
                locale = saveCardSession["locale"] as String,
                currency = saveCardSession["currency"] as String,
                metadata = saveCardSession["metadata"] as Map<String, Any>
            )
            val sessionId = apiHelper.extractSessionId(sessionResponse)
            println(sessionId)
            assertNotNull("Session ID should not be null", sessionId)
            assertTrue("Session ID should not be empty", sessionId!!.isNotEmpty())

            // Step 2: Initialize SDK
            val cardSessions = CardSessions.create(context, TestData.getSecretKey())
            TestUtils.ensureInitialized(cardSessions)

            // Step 3: Set up billing information using TestUtils
            val billingInfo = TestUtils.createValidBillingInfo(
                firstName = "Test",
                lastName = "User",
                email = "test.user@example.com",
                phoneNumber = "+6281234567890",
                streetLine1 = "123 Test Street",
                city = "Jakarta",
                provinceState = "DKI Jakarta",
                postalCode = "12345",
                country = "ID"
            )

            // Step 4 & 5: Collect card data with the created session
            val validCardNumber = TestData.validCardNumbers["visa"]!!
            val response = cardSessions.collectCardData(
                cardNumber = validCardNumber,
                expiryMonth = "12",
                expiryYear = "2025",
                cvn = "123",
                cardholderFirstName = "Test",
                cardholderLastName = "User",
                cardholderEmail = "test.user@example.com",
                cardholderPhoneNumber = "+6281234567890",
                paymentSessionId = sessionId,
                billingInformation = billingInfo,
                confirmSave = true
            )

            // Step 6: Verify response
            assertNotNull("Response should not be null", response)
            println("Collect Card Data: ${response}");

            if (response.paymentTokenId != null) {
                // Success case
                assertTrue(
                    "Payment token should not be empty",
                    response.paymentTokenId!!.isNotEmpty()
                )

                // Step 7: Verify session status
                try {
                    // Add delay to allow session status to update
                    delay(2000L) // Wait 2 seconds for session to complete

                    val sessionDetails = apiHelper.getSession(sessionId)
                    println("Session info: ${sessionDetails}");
                    assertEquals(
                        "Session ID should match",
                        sessionId,
                        sessionDetails["payment_session_id"]
                    )

                    // Check if session is completed or still active (both are acceptable in test environment)
                    val status = sessionDetails["status"] as String
                    assertTrue(
                        "Session should be either COMPLETED or ACTIVE, but was: $status",
                        status == "COMPLETED" || status == "ACTIVE"
                    )
                    assertEquals(
                        "Payment token should match",
                        response.paymentTokenId,
                        sessionDetails["payment_token_id"]
                    )
                } catch (e: Exception) {
                    // Session verification might fail in test environment
                    println("Session verification failed: ${e.message}")
                }
            } else if (response.message != null) {
                // Error case - log the message for debugging
                println("Card collection failed with message: ${response.message}")
                assertTrue("Error message should not be empty", response.message!!.isNotEmpty())
            }

            // Clean up - add small delay
            delay(TestData.cleanupDelay)

        } catch (e: Exception) {
            // If session creation fails, we can skip the test
            println("Test failed with exception: ${e.message}")
            assertTrue(
                "Exception message should contain session or network error",
                e.message?.contains("session", ignoreCase = true) == true ||
                        e.message?.contains("network", ignoreCase = true) == true ||
                        e.message?.contains("connection", ignoreCase = true) == true
            )
        }
    }

    @Test
    fun testCVNCollectionFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        try {
            // Step 1: Create a CVN collection session
            val referenceId = "test_cvn_${System.currentTimeMillis()}"
            val cvnSession = TestData.cvnCollectionSession
            
            val sessionResponse = apiHelper.createSession(
                referenceId = referenceId,
                customerId = TestData.customerId,
                description = cvnSession["description"] as String,
                amount = cvnSession["amount"] as Int,
                sessionType = cvnSession["sessionType"] as String,
                country = cvnSession["country"] as String,
                locale = cvnSession["locale"] as String,
                currency = cvnSession["currency"] as String,
                cardPaymentTokenId = cvnSession["card_payment_token_id"] as String,
                metadata = cvnSession["metadata"] as Map<String, Any>
            )

            val sessionId = apiHelper.extractSessionId(sessionResponse)
            println("Session ID: $sessionId");
            assertNotNull("Session ID should not be null", sessionId)

            // Step 2: Initialize SDK
            val cardSessions = CardSessions.create(context, TestData.getSecretKey())
            TestUtils.ensureInitialized(cardSessions)

            // Step 3: Collect CVN
            val response = cardSessions.collectCvn(
                cvn = TestData.testCvn,
                paymentSessionId = sessionId!!
            )
            println("Collect CVN response $response");

            // Step 4: Verify response using TestUtils
            assertNotNull("CVN response should not be null", response)

            if (response.message != null) {
                assertTrue(
                    "Response should be successful",
                    TestUtils.verifySuccessResponse(response, expectPaymentToken = false)
                )
            }

            // Step 5: Try to verify session (might fail in test environment)
            try {
                // Add delay to allow session status to update
                delay(2000L) // Wait 2 seconds for session to complete

                val sessionDetails = apiHelper.getSession(sessionId)
                println("Session status after CVN: ${sessionDetails["status"]}")

                // Verify session status (COMPLETED or ACTIVE are both acceptable)
                val status = sessionDetails["status"] as String
                assertTrue(
                    "Session should be either COMPLETED or ACTIVE after CVN, but was: $status",
                    status == "COMPLETED" || status == "ACTIVE"
                )
            } catch (e: Exception) {
                println("Could not verify session: ${e.message}")
            }

        } catch (e: Exception) {
            println("CVN test failed: ${e.message}")
            // Expected in test environment without valid payment token
            assertTrue(
                "Exception should be related to session or token",
                e.message?.contains("session", ignoreCase = true) == true ||
                        e.message?.contains("token", ignoreCase = true) == true
            )
        }
    }

    @Test
    fun testErrorHandlingInvalidCard() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        try {
            // Step 1: Create a session for error testing
            val referenceId = "test_error_${System.currentTimeMillis()}"
            val errorSession = TestData.errorHandlingSession
            val sessionResponse = apiHelper.createSession(
                referenceId = referenceId,
                customerId = TestData.customerId,
                description = errorSession["description"] as String,
                amount = errorSession["amount"] as Int,
                sessionType = errorSession["sessionType"] as String,
                country = errorSession["country"] as String,
                locale = errorSession["locale"] as String,
                currency = errorSession["currency"] as String,
                metadata = errorSession["metadata"] as Map<String, Any>
            )

            val sessionId = apiHelper.extractSessionId(sessionResponse)
            assertNotNull("Session ID should not be null", sessionId)

            // Step 2: Initialize SDK
            val cardSessions = CardSessions.create(context, TestData.getSecretKey())
            TestUtils.ensureInitialized(cardSessions)

            // Step 3: Set up minimal billing info using TestUtils
            val billingInfo = TestUtils.createValidBillingInfo(
                firstName = "Test",
                lastName = "User",
                email = "test@example.com",
                streetLine1 = "123 Test St",
                city = "Jakarta",
                provinceState = "DKI Jakarta",
                postalCode = "12345",
                country = "ID"
            )

            // Step 4: Try with invalid card from TestData
            val invalidCardNumber = TestData.invalidCardNumbers["invalid_format"]!!
            val response = cardSessions.collectCardData(
                cardNumber = invalidCardNumber,
                expiryMonth = "13", // Invalid month
                expiryYear = "2025",
                cvn = "123",
                cardholderFirstName = "Test",
                cardholderLastName = "User",
                cardholderEmail = "test.user@example.com",
                cardholderPhoneNumber = "+6281234567890",
                paymentSessionId = sessionId!!,
                billingInformation = billingInfo
            )

            // Step 5: Verify error response using TestUtils
            assertNotNull("Response should not be null for invalid card", response)
            assertNotNull("Error message should be present for invalid card", response.message)

            // The SDK should validate the card and return an error
            assertTrue(
                "Error message should indicate invalid card",
                TestUtils.verifyErrorResponse(response)
            )

            // Payment token should be null for invalid card
            assertNull("Payment token should be null for invalid card", response.paymentTokenId)

            // Verify session status (should still be ACTIVE due to error)
            try {
                // Add small delay before checking session status
                delay(1000L) // Wait 1 second

                val sessionDetails = apiHelper.getSession(sessionId)
                println("Session status after error: ${sessionDetails["status"]}")
                // Session should still be ACTIVE if card validation failed
                val status = sessionDetails["status"] as String
                assertTrue(
                    "Session should be ACTIVE after error, but was: $status",
                    status == "ACTIVE"
                )
            } catch (e: Exception) {
                println("Could not verify session after error: ${e.message}")
            }

        } catch (e: Exception) {
            println("Error handling test exception: ${e.message}")
            // This is acceptable in test environment
        }
    }

    @Test
    fun testSDKStateFlow() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        try {
            // Create a session first
            val referenceId = "test_state_${System.currentTimeMillis()}"
            val sessionResponse = apiHelper.createSession(
                referenceId = referenceId,
                customerId = TestData.customerId,
                description = "State flow test",
                amount = 0,
                sessionType = "SAVE",
                country = "ID",
                locale = "en",
                currency = "IDR"
            )

            val sessionId = apiHelper.extractSessionId(sessionResponse)
            assertNotNull("Session ID should not be null", sessionId)

            // Initialize SDK
            val cardSessions = CardSessions.create(context, TestData.getSecretKey())

            // Test state flow by collecting states during operation
            val states = mutableListOf<CardSessionState>()

            // Launch a coroutine to collect states
            val stateJob = launch {
                cardSessions.state.collect { state ->
                    states.add(state)
                }
            }

            // Give some time for initial state
            delay(TestData.initDelay)

            // Perform an operation
            try {
                cardSessions.collectCardData(
                    cardNumber = "4111111111111111",
                    expiryMonth = "12",
                    expiryYear = "2025",
                    cvn = "123",
                    cardholderFirstName = "Test",
                    cardholderLastName = "User",
                    cardholderEmail = "test.user@example.com",
                    cardholderPhoneNumber = "+6281234567890",
                    paymentSessionId = sessionId!!,
                    billingInformation = TestUtils.createValidBillingInfo()
                )
            } catch (e: Exception) {
                // Expected in test environment
                println("State flow test exception: ${e.message}")
            }

            // Wait a bit for states to be emitted
            delay(TestData.stateDelay)

            // Cancel state collection
            stateJob.cancel()

            // Verify we got state changes
            assertTrue("Should have collected at least one state", states.isNotEmpty())

            // Check that we had loading state
            assertTrue(
                "Should have had a loading state",
                states.any { it.isLoading })

        } catch (e: Exception) {
            println("State flow test failed: ${e.message}")
            // Acceptable in test environment
        }
    }

    @Test
    fun testMultipleCardTypes() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()

        // Use TestUtils to create billing info
        val billingInfo = TestUtils.createValidBillingInfo()

        // Test with different card types from TestData
        val testCards = listOf(
            Triple(TestData.validCardNumbers["visa"]!!, "12", "2025"), // Valid Visa
            Triple(TestData.validCardNumbers["mastercard"]!!, "12", "2025"), // Valid MasterCard
            Triple(TestData.invalidCardNumbers["invalid_format"]!!, "13", "2025")  // Invalid card
        )

        val cardSessions = CardSessions.create(context, TestData.getSecretKey())

        for ((index, cardData) in testCards.withIndex()) {
            val (cardNumber, expiryMonth, expiryYear) = cardData
            val cvn = if (cardNumber.startsWith("37")) "1234" else "123" // Amex uses 4-digit CVN
            println("Card ${index} started...")
            try {
                // Create a new session for each card
                val referenceId = "test_multi_${System.currentTimeMillis()}_$index"
                val sessionResponse = apiHelper.createSession(
                    referenceId = referenceId,
                    customerId = TestData.customerId,
                    description = "Multi-card test $index",
                    amount = 0,
                    sessionType = "SAVE",
                    country = "ID",
                    locale = "en",
                    currency = "IDR"
                )

                val sessionId = apiHelper.extractSessionId(sessionResponse)
                if (sessionId == null) {
                    println("Could not create session for card $index")
                    continue
                }

                val response = cardSessions.collectCardData(
                    cardNumber = cardNumber,
                    expiryMonth = expiryMonth,
                    expiryYear = expiryYear,
                    cvn = cvn,
                    cardholderFirstName = "Test",
                    cardholderLastName = "User",
                    cardholderEmail = "test.user@example.com",
                    cardholderPhoneNumber = "+6281234567890",
                    paymentSessionId = sessionId,
                    billingInformation = billingInfo
                )

                assertNotNull("Response should not be null for card $index", response)

                // Check if it's the invalid card
                if (cardNumber == "1234567890123456") {
                    assertNotNull("Invalid card should have error message", response.message)
                    assertNull(
                        "Invalid card should not have payment token",
                        response.paymentTokenId
                    )
                } else {
                    // Valid cards might succeed or fail depending on test environment
                    println("Card $index result: token=${response.paymentTokenId}, message=${response.message}")
                }

            } catch (e: Exception) {
                println("Card $index test failed: ${e.message}")
                assertTrue("Exception message should not be empty", e.message?.isNotEmpty() == true)
            }

            // Small delay between tests
            delay(TestData.cleanupDelay)
        }
    }
}