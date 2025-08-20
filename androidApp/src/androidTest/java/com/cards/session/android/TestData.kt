package com.cards.session.android

/**
 * Test data constants and configurations for E2E tests
 * Mirrors the structure of Flutter's test_utils.dart
 *
 * Sensitive values can be provided via system properties:
 * - TEST_PUBLIC_KEY: Xendit public API key
 * - TEST_SECRET_KEY: Xendit secret API key (with Basic auth prefix)
 * - TEST_BASE_URL: API base URL
 *
 * Example: ./gradlew connectedAndroidTest -DTEST_PUBLIC_KEY=xxx -DTEST_SECRET_KEY=yyy
 */
object TestData {

    // Base URL for API calls
    const val baseUrl = "https://api.xendit.co"

    // Default API Keys (replace with real keys via environment variables)
    private const val defaultPublicKey = "Basic XXXX"
    private const val defaultSecretKey = "xnd_public_development_XXXX"
    private const val paymentTokenId = "pt-XXX";
    const val customerId = "cust-XXX"

    // Getters that check for environment variables or system properties
    fun getPublicKey(): String =
        System.getenv("TEST_PUBLIC_KEY")
            ?: System.getProperty("TEST_PUBLIC_KEY")
            ?: defaultPublicKey

    fun getSecretKey(): String =
        System.getenv("TEST_SECRET_KEY")
            ?: System.getProperty("TEST_SECRET_KEY")
            ?: defaultSecretKey

    fun getBaseUrl(): String =
        System.getenv("TEST_BASE_URL")
            ?: System.getProperty("TEST_BASE_URL")
            ?: baseUrl

    // Session Configurations
    val saveCardSession = mapOf(
        "description" to "Integration test session",
        "amount" to 0,
        "sessionType" to "SAVE",
        "country" to "ID",
        "locale" to "en",
        "currency" to "IDR",
        "metadata" to mapOf(
            "test_run" to "true",
            "integration_test" to "e2e"
        )
    )

    val cvnCollectionSession = mapOf(
        "description" to "CVN collection test",
        "amount" to 10000,
        "sessionType" to "PAY",
        "country" to "ID",
        "locale" to "en",
        "currency" to "IDR",
        "card_payment_token_id" to paymentTokenId,
        "metadata" to mapOf(
            "test_type" to "cvn_collection"
        )
    )

    val errorHandlingSession = mapOf(
        "description" to "Error handling test session",
        "amount" to 0,
        "sessionType" to "SAVE",
        "country" to "ID",
        "locale" to "en",
        "currency" to "IDR",
        "metadata" to mapOf(
            "test_type" to "error_handling"
        )
    )

    // Test Cards
    val validCardNumbers = mapOf(
        "visa" to "4111111111111111",
        "mastercard" to "5555555555554444",
        "amex" to "378282246310005",
        "discover" to "6011000000000004",
        "jcb" to "3528000000000007"
    )

    val invalidCardNumbers = mapOf(
        "too_short" to "411111111111",
        "too_long" to "41111111111111111",
        "letters" to "411111111111abcd",
        "special_chars" to "4111-1111-1111-1111",
        "invalid_format" to "1234567890123456"
    )

    // Default card data
    val validCard = mapOf(
        "cardNumber" to "4111111111111111",
        "expiryMonth" to "12",
        "expiryYear" to "2025",
        "cvn" to "123",
        "description" to "Valid Visa test card"
    )

    val invalidCard = mapOf(
        "cardNumber" to "1234567890123456",
        "expiryMonth" to "13",
        "expiryYear" to "2025",
        "cvn" to "123",
        "description" to "Invalid card number for error testing"
    )

    // Billing Information
    val defaultBillingInfo = mapOf(
        "firstName" to "Test",
        "lastName" to "User",
        "email" to "test.user@example.com",
        "phoneNumber" to "+6281234567890",
        "streetLine1" to "123 Test Street",
        "city" to "Jakarta",
        "provinceState" to "DKI Jakarta",
        "postalCode" to "12345",
        "country" to "ID"
    )

    val minimalBillingInfo = mapOf(
        "firstName" to "Test",
        "lastName" to "User",
        "email" to "test@example.com",
        "streetLine1" to "123 Test St",
        "city" to "Jakarta",
        "provinceState" to "DKI Jakarta",
        "postalCode" to "12345",
        "country" to "ID"
    )

    // Cardholder Information
    val defaultCardholderInfo = mapOf(
        "firstName" to "Test",
        "lastName" to "User",
        "email" to "test.user@example.com",
        "phoneNumber" to "+6281234567890"
    )

    // CVN Test Data
    const val testCvn = "456"
    const val exampleSessionId = "example_session_id"

    // Timeouts (in milliseconds)
    const val responseTimeout = 15000L
    const val cvnTimeout = 10000L
    const val initDelay = 500L
    const val stateDelay = 500L
    const val cleanupDelay = 100L

    // International Billing Addresses (for advanced testing)
    val internationalBillingAddresses = listOf(
        mapOf(
            "country" to "GB",
            "postalCode" to "SW1A 1AA",
            "provinceState" to "London",
            "city" to "London",
            "streetLine1" to "10 Downing Street",
            "firstName" to "Test",
            "lastName" to "User",
            "email" to "test.user@example.com"
        ),
        mapOf(
            "country" to "JP",
            "postalCode" to "100-0001",
            "provinceState" to "Tokyo",
            "city" to "Chiyoda",
            "streetLine1" to "1-1-1 Chiyoda",
            "firstName" to "Test",
            "lastName" to "User",
            "email" to "test.user@example.com"
        ),
        mapOf(
            "country" to "AU",
            "postalCode" to "2000",
            "provinceState" to "NSW",
            "city" to "Sydney",
            "streetLine1" to "1 Martin Place",
            "firstName" to "Test",
            "lastName" to "User",
            "email" to "test.user@example.com"
        ),
        mapOf(
            "country" to "CA",
            "postalCode" to "M5H 2N2",
            "provinceState" to "ON",
            "city" to "Toronto",
            "streetLine1" to "100 King Street West",
            "firstName" to "Test",
            "lastName" to "User",
            "email" to "test.user@example.com"
        )
    )

    // Test scenario configurations
    val invalidCardDataScenarios = listOf(
        mapOf(
            "description" to "Invalid card number",
            "cardNumber" to "1234567890123456",
            "expectedError" to "invalid_card_number"
        ),
        mapOf(
            "description" to "Expired card",
            "expiryYear" to "2020",
            "expectedError" to "card_expired"
        ),
        mapOf(
            "description" to "Invalid month",
            "expiryMonth" to "13",
            "expectedError" to "invalid_expiry_date"
        ),
        mapOf(
            "description" to "Invalid email",
            "email" to "not-an-email",
            "expectedError" to "invalid_email"
        )
    )
}