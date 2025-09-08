import Foundation

/// Test data constants and configurations for E2E tests
/// Mirrors the structure of Android's TestData
///
/// Sensitive values can be provided via environment variables:
/// - TEST_PUBLIC_KEY: Xendit public API key
/// - TEST_SECRET_KEY: Xendit secret API key (with Basic auth prefix)
/// - TEST_BASE_URL: API base URL
struct TestData {
    
    // Base URL for API calls
    static let baseUrl = "https://api.xendit.co"
    
    // Default API Keys (replace with real keys via environment variables)
    private static let defaultPublicKey = "xnd_public_development_XXXXX"
    private static let defaultSecretKey = "Basic XXXXX"
    private static let paymentTokenId = "pt-XXXXX"
    static let customerId = "cust-XXXXX"
    
    // Getters that check for environment variables
    static func getPublicKey() -> String {
        ProcessInfo.processInfo.environment["TEST_PUBLIC_KEY"] ?? defaultPublicKey
    }
    
    static func getSecretKey() -> String {
        ProcessInfo.processInfo.environment["TEST_SECRET_KEY"] ?? defaultSecretKey
    }
    
    static func getBaseUrl() -> String {
        ProcessInfo.processInfo.environment["TEST_BASE_URL"] ?? baseUrl
    }
    
    // Session Configurations
    static let saveCardSession: [String: Any] = [
        "description": "Integration test session",
        "amount": 0,
        "sessionType": "SAVE",
        "country": "ID",
        "locale": "en",
        "currency": "IDR",
        "metadata": [
            "test_run": "true",
            "integration_test": "e2e"
        ]
    ]
    
    static let cvnCollectionSession: [String: Any] = [
        "description": "CVN collection test",
        "amount": 10000,
        "sessionType": "PAY",
        "country": "ID",
        "locale": "en",
        "currency": "IDR",
        "card_payment_token_id": paymentTokenId,
        "metadata": [
            "test_type": "cvn_collection"
        ]
    ]
    
    static let errorHandlingSession: [String: Any] = [
        "description": "Error handling test session",
        "amount": 0,
        "sessionType": "SAVE",
        "country": "ID",
        "locale": "en",
        "currency": "IDR",
        "metadata": [
            "test_type": "error_handling"
        ]
    ]
    
    // Test Cards
    static let validCardNumbers: [String: String] = [
        "visa": "4111111111111111",
        "mastercard": "5555555555554444",
        "amex": "378282246310005",
        "discover": "6011000000000004",
        "jcb": "3528000000000007"
    ]
    
    static let invalidCardNumbers: [String: String] = [
        "too_short": "411111111111",
        "too_long": "41111111111111111",
        "letters": "411111111111abcd",
        "special_chars": "4111-1111-1111-1111",
        "invalid_format": "1234567890123456"
    ]
    
    // Default card data
    static let validCard: [String: String] = [
        "cardNumber": "4111111111111111",
        "expiryMonth": "12",
        "expiryYear": "2025",
        "cvn": "123",
        "description": "Valid Visa test card"
    ]
    
    static let invalidCard: [String: String] = [
        "cardNumber": "1234567890123456",
        "expiryMonth": "13",
        "expiryYear": "2025",
        "cvn": "123",
        "description": "Invalid card number for error testing"
    ]
    
    // Billing Information
    static let defaultBillingInfo: [String: String] = [
        "firstName": "Test",
        "lastName": "User",
        "email": "test.user@example.com",
        "phoneNumber": "+6281234567890",
        "streetLine1": "123 Test Street",
        "city": "Jakarta",
        "provinceState": "DKI Jakarta",
        "postalCode": "12345",
        "country": "ID"
    ]
    
    static let minimalBillingInfo: [String: String] = [
        "firstName": "Test",
        "lastName": "User",
        "email": "test@example.com",
        "streetLine1": "123 Test St",
        "city": "Jakarta",
        "provinceState": "DKI Jakarta",
        "postalCode": "12345",
        "country": "ID"
    ]
    
    // Cardholder Information
    static let defaultCardholderInfo: [String: String] = [
        "firstName": "Test",
        "lastName": "User",
        "email": "test.user@example.com",
        "phoneNumber": "+6281234567890"
    ]
    
    // CVN Test Data
    static let testCvn = "456"
    static let exampleSessionId = "example_session_id"
    
    // Timeouts (in seconds)
    static let responseTimeout: TimeInterval = 15.0
    static let cvnTimeout: TimeInterval = 10.0
    static let initDelay: TimeInterval = 0.5
    static let stateDelay: TimeInterval = 0.5
    static let cleanupDelay: TimeInterval = 0.1
    
    // Test Cards dictionary for different card types
    static let testCards: [String: [String: String]] = [
        "VISA": validCard,
        "MASTERCARD": [
            "cardNumber": validCardNumbers["mastercard"]!,
            "expiryMonth": "12",
            "expiryYear": "2025",
            "cvn": "123",
            "description": "Valid MasterCard test card"
        ],
        "AMEX": [
            "cardNumber": validCardNumbers["amex"]!,
            "expiryMonth": "12",
            "expiryYear": "2025",
            "cvn": "1234",
            "description": "Valid Amex test card"
        ],
        "DISCOVER": [
            "cardNumber": validCardNumbers["discover"]!,
            "expiryMonth": "12",
            "expiryYear": "2025",
            "cvn": "123",
            "description": "Valid Discover test card"
        ],
        "JCB": [
            "cardNumber": validCardNumbers["jcb"]!,
            "expiryMonth": "12",
            "expiryYear": "2025",
            "cvn": "123",
            "description": "Valid JCB test card"
        ]
    ]
    
    // Generic test card (fallback)
    static let testCardGeneric = validCard
    
    // International Billing Addresses (for advanced testing)
    static let internationalBillingAddresses: [[String: String]] = [
        [
            "country": "GB",
            "postalCode": "SW1A 1AA",
            "provinceState": "London",
            "city": "London",
            "streetLine1": "10 Downing Street",
            "firstName": "Test",
            "lastName": "User",
            "email": "test.user@example.com"
        ],
        [
            "country": "JP",
            "postalCode": "100-0001",
            "provinceState": "Tokyo",
            "city": "Chiyoda",
            "streetLine1": "1-1-1 Chiyoda",
            "firstName": "Test",
            "lastName": "User",
            "email": "test.user@example.com"
        ],
        [
            "country": "AU",
            "postalCode": "2000",
            "provinceState": "NSW",
            "city": "Sydney",
            "streetLine1": "1 Martin Place",
            "firstName": "Test",
            "lastName": "User",
            "email": "test.user@example.com"
        ],
        [
            "country": "CA",
            "postalCode": "M5H 2N2",
            "provinceState": "ON",
            "city": "Toronto",
            "streetLine1": "100 King Street West",
            "firstName": "Test",
            "lastName": "User",
            "email": "test.user@example.com"
        ]
    ]
    
    // Test scenario configurations
    static let invalidCardDataScenarios: [[String: Any]] = [
        [
            "description": "Invalid card number",
            "cardNumber": "1234567890123456",
            "expectedError": "invalid_card_number"
        ],
        [
            "description": "Expired card",
            "expiryYear": "2020",
            "expectedError": "card_expired"
        ],
        [
            "description": "Invalid month",
            "expiryMonth": "13",
            "expectedError": "invalid_expiry_date"
        ],
        [
            "description": "Invalid email",
            "email": "not-an-email",
            "expectedError": "invalid_email"
        ]
    ]
}
