import XCTest
@testable import iosApp
import cardsSdk

/// End-to-End tests that create real sessions before collecting card data
final class XenditCardsSessionE2ETests: XCTestCase {
    
    private var apiHelper: XenditApiHelper!
    private var cardSessions: CardSessions!
    
    override func setUpWithError() throws {
        try super.setUpWithError()
        continueAfterFailure = false
        
        // Initialize API helper with test credentials
        apiHelper = XenditApiHelper(
            apiKey: TestData.getSecretKey(),
            baseUrl: TestData.getBaseUrl()
        )
        
        // Initialize CardSessions SDK
        cardSessions = CardSessionsFactory().create(
            apiKey: TestData.getPublicKey(),
            isEnableLog: true
        )
    }
    
    override func tearDownWithError() throws {
        apiHelper = nil
        cardSessions = nil
        try super.tearDownWithError()
    }
    
    /// Test complete payment session flow
    /// Mirrors Android's testCompletePaymentSessionFlow
    func testCompletePaymentSessionFlow() async throws {
        print("Starting complete payment session flow test")
        
        // Step 1: Create a session via API
        let referenceId = "test_\(Int(Date().timeIntervalSince1970))"
        let saveCardSession = TestData.saveCardSession
        
        let sessionResponse = try await apiHelper.createSession(
            referenceId: referenceId,
            amount: saveCardSession["amount"] as! Int,
            locale: saveCardSession["locale"] as! String,
            country: saveCardSession["country"] as! String,
            currency: saveCardSession["currency"] as! String,
            customerId: TestData.customerId,
            sessionType: saveCardSession["sessionType"] as! String,
            description: saveCardSession["description"] as? String,
            metadata: saveCardSession["metadata"] as? [String: Any]
        )
        
        guard let sessionId = apiHelper.extractSessionId(from: sessionResponse) else {
            XCTFail("Failed to extract session ID from response")
            return
        }
        
        print("Created session with ID: \(sessionId)")
        
        // Step 2: Collect card data using SDK
        let cardData = TestData.validCard
        let billingInfo = TestUtils.createValidBillingInfo()
        
        let response = try await cardSessions.collectCardData(
            cardNumber: cardData["cardNumber"] as! String,
            expiryMonth: cardData["expiryMonth"] as! String,
            expiryYear: cardData["expiryYear"] as! String,
            cvn: cardData["cvn"] as? String,
            cardholderFirstName: TestData.defaultCardholderInfo["firstName"]!,
            cardholderLastName: TestData.defaultCardholderInfo["lastName"]!,
            cardholderEmail: TestData.defaultCardholderInfo["email"]!,
            cardholderPhoneNumber: TestData.defaultCardholderInfo["phoneNumber"]!,
            paymentSessionId: sessionId,
            confirmSave: true,
            billingInformation: billingInfo
        )
        
        // Step 3: Verify response
        XCTAssertNotNil(response, "Response should not be null")
        
        print("Collect Card Data Response: \(response)")
        
        // Check for successful payment token
        if let paymentToken = response.paymentTokenId {
            // Success case
            XCTAssertTrue(
                !paymentToken.isEmpty,
                "Payment token should not be empty"
            )
            print("Payment token received: \(paymentToken)")
        } else if response.message.contains("Status updated") {
            // Alternative success message
            XCTAssertTrue(
                response.message.contains("Status updated"),
                "Response message should indicate success"
            )
        } else {
            XCTFail("Response should have either paymentTokenId or success message, got: \(response.message)")
        }
        
        // Step 4: Verify session status via API
        let sessionStatus = try await apiHelper.getSession(sessionId: sessionId)
        if let status = sessionStatus["status"] as? String {
            XCTAssertTrue(
                status == "COMPLETED",
                "Session status should be COMPLETED, got: \(status)"
            )
        }
        
        print("Payment session flow test completed successfully")
    }
    
    /// Test CVN collection flow
    func testCVNCollectionFlow() async throws {
        print("Starting CVN collection flow test")
        
        // Step 1: Create a CVN session via API
        let referenceId = "test_cvn_\(Int(Date().timeIntervalSince1970))"
        let cvnSession = TestData.cvnCollectionSession
        
        let sessionResponse = try await apiHelper.createSession(
            referenceId: referenceId,
            amount: cvnSession["amount"] as! Int,
            locale: cvnSession["locale"] as! String,
            country: cvnSession["country"] as! String,
            currency: cvnSession["currency"] as! String,
            customerId: TestData.customerId,
            sessionType: cvnSession["sessionType"] as! String,
            description: cvnSession["description"] as? String,
            cardPaymentTokenId: cvnSession["card_payment_token_id"] as! String,
            metadata: cvnSession["metadata"] as? [String: Any]
        )
        
        guard let sessionId = apiHelper.extractSessionId(from: sessionResponse) else {
            XCTFail("Failed to extract session ID from response")
            return
        }
        
        print("Created CVN session with ID: \(sessionId)")
        
        // Step 2: Collect CVN using SDK
        let response = try await cardSessions.collectCvn(
            cvn: TestData.testCvn,
            paymentSessionId: sessionId
        )
        
        print("Collect CVN response: \(response)")
        // Step 3: Verify response
        XCTAssertNotNil(response, "CVN response should not be null")
        
        // CVN collection typically doesn't return a payment token
        if response.message != nil {
            XCTAssertTrue(
                response.message.contains("Status updated") || response.message.contains("success"),
                "Response should be successful, but got: \(response.message)"
            )
        }
        
        print("CVN collection flow test completed successfully")
    }
    
    /// Test error handling with invalid card
    /// Mirrors Android's testErrorHandlingInvalidCard
    func testErrorHandlingInvalidCard() async throws {
        print("Starting error handling test with invalid card")
        
        // Step 1: Create a session via API
        let referenceId = "test_error_\(Int(Date().timeIntervalSince1970))"
        let saveCardSession = TestData.saveCardSession
        
        let sessionResponse = try await apiHelper.createSession(
            referenceId: referenceId,
            amount: saveCardSession["amount"] as! Int,
            locale: saveCardSession["locale"] as! String,
            country: saveCardSession["country"] as! String,
            currency: saveCardSession["currency"] as! String,
            customerId: TestData.customerId,
            sessionType: saveCardSession["sessionType"] as! String,
            description: saveCardSession["description"] as? String,
            metadata: saveCardSession["metadata"] as? [String: Any]
        )
        
        guard let sessionId = apiHelper.extractSessionId(from: sessionResponse) else {
            XCTFail("Failed to extract session ID from response")
            return
        }
        
        print("Created session for error testing: \(sessionId)")
        
        // Step 2: Try to collect with invalid card number
        let response = try await cardSessions.collectCardData(
            cardNumber: "1234567890123456", // Invalid card number
            expiryMonth: "13", // Invalid month to match Android test
            expiryYear: "2025",
            cvn: "123",
            cardholderFirstName: "Test",
            cardholderLastName: "User",
            cardholderEmail: "test@example.com",
            cardholderPhoneNumber: "+1234567890",
            paymentSessionId: sessionId,
            confirmSave: false,
            billingInformation: nil
        )
        
        // Step 3: Verify error response
        XCTAssertNotNil(response, "Response should not be null for invalid card")
        XCTAssertNotNil(response.message, "Error message should be present for invalid card")
        
        // The SDK should validate the card and return an error
        let messageLower = response.message.lowercased()
        XCTAssertTrue(
            messageLower.contains("invalid") || messageLower.contains("error") || messageLower.contains("failed"),
            "Error message should indicate invalid card, but got: \(response.message)"
        )
        
        // Payment token should be null for invalid card
        XCTAssertNil(response.paymentTokenId, "Payment token should be null for invalid card")
        
        print("Error handling test completed successfully")
    }
    
    /// Test SDK not initialized error
    /// Mirrors Android's testSDKNotInitialized
    func testSDKNotInitialized() async throws {
        print("Starting SDK not initialized test")
        
        // Create a new SDK instance without proper initialization
        // This test verifies that the SDK handles improper initialization gracefully
        let uninitializedSdk = CardSessionsFactory().create(
            apiKey: "", // Empty API key
            isEnableLog: false
        )
        
        let response = try await uninitializedSdk.collectCardData(
            cardNumber: "4242424242424242",
            expiryMonth: "12",
            expiryYear: "2026",
            cvn: "123",
            cardholderFirstName: "Test",
            cardholderLastName: "User",
            cardholderEmail: "test@example.com",
            cardholderPhoneNumber: "+1234567890",
            paymentSessionId: "test_session",
            confirmSave: false,
            billingInformation: nil
        )
        
        // Verify error response for uninitialized SDK
        XCTAssertNotNil(response, "Response should not be null")
        XCTAssertNotNil(response.message, "Error message should be present for uninitialized SDK")
        
        print("Uninitialized SDK response: \(response.message)")
        
        // Check that the response indicates an API key error
        let messageLower = response.message.lowercased()
        XCTAssertTrue(
            messageLower.contains("api key") || messageLower.contains("invalid") || messageLower.contains("unauthorized"),
            "Invalid API key should cause a validation error, but got: \(response.message)"
        )
        
        // Payment token should be null for uninitialized SDK
        XCTAssertNil(response.paymentTokenId, "Payment token should be null for uninitialized SDK")
        
        print("SDK not initialized test completed successfully")
    }
    
    /// Test multiple card types
    /// Mirrors Android's testMultipleCardTypes
    func testMultipleCardTypes() async throws {
        print("Starting multiple card types test")
        
        let cardTypes = ["VISA", "MASTERCARD"]
        
        for cardType in cardTypes {
            print("Testing \(cardType) card")
            
            // Step 1: Create a session for this card type
            let referenceId = "test_\(cardType.lowercased())_\(Int(Date().timeIntervalSince1970))"
            let saveCardSession = TestData.saveCardSession
            
            let sessionResponse = try await apiHelper.createSession(
                referenceId: referenceId,
                amount: saveCardSession["amount"] as! Int,
                locale: saveCardSession["locale"] as! String,
                country: saveCardSession["country"] as! String,
                currency: saveCardSession["currency"] as! String,
                customerId: TestData.customerId,
                sessionType: saveCardSession["sessionType"] as! String,
                description: "Testing \(cardType) card",
                metadata: ["cardType": cardType]
            )
            
            guard let sessionId = apiHelper.extractSessionId(from: sessionResponse) else {
                XCTFail("Failed to extract session ID for \(cardType)")
                continue
            }
            
            // Step 2: Get test card data for this type
            guard let cardData = TestData.testCards[cardType] else {
                XCTFail("No test card data for \(cardType)")
                continue
            }
            
            // Step 3: Collect card data
            let response = try await cardSessions.collectCardData(
                cardNumber: cardData["cardNumber"]!,
                expiryMonth: cardData["expiryMonth"]!,
                expiryYear: cardData["expiryYear"]!,
                cvn: cardData["cvn"],
                cardholderFirstName: TestData.defaultCardholderInfo["firstName"]!,
                cardholderLastName: TestData.defaultCardholderInfo["lastName"]!,
                cardholderEmail: TestData.defaultCardholderInfo["email"]!,
                cardholderPhoneNumber: TestData.defaultCardholderInfo["phoneNumber"]!,
                paymentSessionId: sessionId,
                confirmSave: false,
                billingInformation: nil
            )
            
            // Step 4: Verify response
            XCTAssertNotNil(response, "\(cardType) response should not be null")
            
            // Check for successful response
            if let paymentToken = response.paymentTokenId {
                XCTAssertTrue(
                    !paymentToken.isEmpty,
                    "\(cardType) payment token should not be empty"
                )
                print("\(cardType) payment token received: \(paymentToken)")
            } else if response.message != nil {
                XCTAssertTrue(
                    response.message.contains("Status updated") || response.message.contains("success"),
                    "\(cardType) response message should indicate success, but got: \(response.message)"
                )
            } else {
                XCTFail("\(cardType) response should have either paymentTokenId or success message")
            }
            
            print("✓ \(cardType) card processed successfully")
        }
        
        print("Multiple card types test completed successfully")
    }
}
