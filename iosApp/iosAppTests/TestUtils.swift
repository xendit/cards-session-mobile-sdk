import Foundation
import cardsSdk

/// Test utility functions for E2E tests
/// Mirrors the structure of Android's TestUtils
struct TestUtils {
    
    /// Create a valid billing information object with default or custom values
    static func createValidBillingInfo(
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test.user@example.com",
        phoneNumber: String? = "+6281234567890",
        streetLine1: String = "123 Test Street",
        streetLine2: String? = nil,
        city: String = "Jakarta",
        provinceState: String = "DKI Jakarta",
        postalCode: String = "12345",
        country: String = "ID"
    ) -> BillingInformationDto {
        return BillingInformationDto(
            firstName: firstName,
            lastName: lastName,
            email: email,
            phoneNumber: phoneNumber,
            streetLine1: streetLine1,
            streetLine2: streetLine2,
            city: city,
            provinceState: provinceState,
            country: country,
            postalCode: postalCode
        )
    }
    
    /// Create valid card data dictionary with default or custom values
    static func createValidCardData(
        cardNumber: String = "4111111111111111",
        expiryMonth: String = "12",
        expiryYear: String = "2025",
        cvn: String = "123",
        firstName: String = "Test",
        lastName: String = "User",
        email: String = "test.user@example.com",
        phoneNumber: String = "+6281234567890",
        sessionId: String = TestData.exampleSessionId,
        confirmSave: Bool = false,
        billingInfo: BillingInformationDto? = nil
    ) -> [String: Any] {
        return [
            "cardNumber": cardNumber,
            "expiryMonth": expiryMonth,
            "expiryYear": expiryYear,
            "cvn": cvn,
            "cardholderFirstName": firstName,
            "cardholderLastName": lastName,
            "cardholderEmail": email,
            "cardholderPhoneNumber": phoneNumber,
            "paymentSessionId": sessionId,
            "confirmSave": confirmSave,
            "billingInformation": billingInfo ?? createValidBillingInfo()
        ]
    }
    
    /// Initialize SDK and ensure it's ready
    static func initializeSDK(apiKey: String) -> CardSessions {
        let cardSessions = CardSessionsFactory().create(
            apiKey: apiKey,
            isEnableLog: true
        )
        
        // Allow time for initialization
        Thread.sleep(forTimeInterval: TestData.initDelay)
        
        return cardSessions
    }
    
    /// Get invalid card data scenarios for testing
    static func getInvalidCardDataScenarios() -> [[String: Any]] {
        return [
            [
                "description": "Invalid card number",
                "data": createValidCardData(cardNumber: "1234567890123456"),
                "expectedError": "invalid_card_number"
            ],
            [
                "description": "Expired card",
                "data": createValidCardData(expiryYear: "2020"),
                "expectedError": "card_expired"
            ],
            [
                "description": "Invalid month",
                "data": createValidCardData(expiryMonth: "13"),
                "expectedError": "invalid_expiry_date"
            ],
            [
                "description": "Invalid email",
                "data": createValidCardData(email: "not-an-email"),
                "expectedError": "invalid_email"
            ],
            [
                "description": "Empty required fields",
                "data": createValidCardData(firstName: "", lastName: ""),
                "expectedError": "missing_required_fields"
            ]
        ]
    }
    
    /// Get international billing addresses for testing
    static func getInternationalBillingAddresses() -> [BillingInformationDto] {
        return [
            createValidBillingInfo(
                firstName: "John",
                lastName: "Smith",
                email: "john.smith@example.com",
                phoneNumber: "+447911123456",
                streetLine1: "10 Downing Street",
                streetLine2: "",
                city: "London",
                provinceState: "London",
                postalCode: "SW1A 1AA",
                country: "GB"
            ),
            createValidBillingInfo(
                firstName: "Taro",
                lastName: "Yamada",
                email: "taro.yamada@example.jp",
                phoneNumber: "+819012345678",
                streetLine1: "1-1-1 Chiyoda",
                streetLine2: "",
                city: "Chiyoda",
                provinceState: "Tokyo",
                postalCode: "100-0001",
                country: "JP"
            ),
            createValidBillingInfo(
                firstName: "Jack",
                lastName: "Williams",
                email: "jack.williams@example.au",
                phoneNumber: "+61412345678",
                streetLine1: "1 Martin Place",
                streetLine2: "",
                city: "Sydney",
                provinceState: "NSW",
                postalCode: "2000",
                country: "AU"
            ),
            createValidBillingInfo(
                firstName: "Michael",
                lastName: "Johnson",
                email: "michael.johnson@example.ca",
                phoneNumber: "+14165551234",
                streetLine1: "100 King Street West",
                streetLine2: "",
                city: "Toronto",
                provinceState: "ON",
                postalCode: "M5H 2N2",
                country: "CA"
            )
        ]
    }
    
    /// Verify response for success case
    static func verifySuccessResponse(
        _ response: CardsResponseDto,
        expectPaymentToken: Bool = true
    ) -> Bool {
        if expectPaymentToken {
            return response.paymentTokenId?.isEmpty == false
        } else {
            // For CVN collection, check for success message
            let lowercasedMessage = response.message.lowercased()
            return lowercasedMessage.contains("status updated") ||
                   lowercasedMessage.contains("success")
        }
    }
    
    /// Verify response for error case
    static func verifyErrorResponse(
        _ response: CardsResponseDto,
        expectedErrorKeywords: [String] = ["invalid", "error", "failed"]
    ) -> Bool {
        let message = response.message.lowercased()
        return expectedErrorKeywords.contains { keyword in
            message.contains(keyword)
        }
    }
    
    /// Wait for a specific condition with timeout
    static func waitForCondition(
        timeout: TimeInterval = 5.0,
        condition: () -> Bool
    ) -> Bool {
        let endTime = Date().timeIntervalSince1970 + timeout
        
        while Date().timeIntervalSince1970 < endTime {
            if condition() {
                return true
            }
            Thread.sleep(forTimeInterval: 0.1)
        }
        
        return false
    }
    
    /// Perform an async operation in a test context
    static func performAsync<T>(
        timeout: TimeInterval = TestData.responseTimeout,
        operation: @escaping () async throws -> T
    ) async throws -> T {
        // In standalone tests, we just run the operation directly
        return try await operation()
    }
}

/// Test utility error
enum TestUtilError: LocalizedError {
    case timeout
    
    var errorDescription: String? {
        switch self {
        case .timeout:
            return "Operation timed out"
        }
    }
}