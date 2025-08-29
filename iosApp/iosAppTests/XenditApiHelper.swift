import Foundation

/// Test error types for API operations
enum TestError: Error {
    case invalidURL
    case invalidResponse
    case invalidJSONResponse
    case apiError(statusCode: Int, message: String)
}

/// Helper class for making API calls to Xendit for testing purposes
/// Mirrors the Android XenditApiHelper structure
class XenditApiHelper {
    
    private let apiKey: String
    private let baseUrl: String
    
    init(apiKey: String, baseUrl: String) {
        self.apiKey = apiKey
        self.baseUrl = baseUrl
    }
    
    /// Create a session for card payments
    func createSession(
        referenceId: String,
        amount: Int,
        locale: String,
        country: String,
        currency: String,
        customerId: String,
        sessionType: String,
        description: String? = nil,
        cardPaymentTokenId: String? = nil,
        metadata: [String: Any]? = nil
    ) async throws -> [String: Any] {
        
        guard let url = URL(string: "\(baseUrl)/sessions") else {
            throw TestError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        // Set authorization header
        let authHeader: String
        if apiKey.hasPrefix("Basic ") {
            authHeader = apiKey
        } else {
            let authData = "\(apiKey):".data(using: .utf8)!
            authHeader = "Basic \(authData.base64EncodedString())"
        }
        request.setValue(authHeader, forHTTPHeaderField: "Authorization")
        
        // Create request body
        var requestBody: [String: Any] = [
            "mode": "CARDS_SESSION_JS",
            "amount": amount,
            "locale": locale,
            "country": country,
            "currency": currency,
            "customer_id": customerId,
            "reference_id": referenceId,
            "session_type": sessionType
        ]
        
        if let description = description {
            requestBody["description"] = description
        }
        
        // Channel properties
        requestBody["channel_properties"] = [
            "cards": ["skip_three_ds": true]
        ]
        
        // Cards session JS properties
        var cardsSessionJs: [String: Any] = [
            "success_return_url": "https://yourcompany.com/success",
            "failure_return_url": "https://yourcompany.com/failure"
        ]
        
        if let cardPaymentTokenId = cardPaymentTokenId {
            cardsSessionJs["card_payment_token_id"] = cardPaymentTokenId
        }
        
        requestBody["cards_session_js"] = cardsSessionJs
        
        if let metadata = metadata {
            requestBody["metadata"] = metadata
        }
        
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)
        
        // Log the request
        print("\n========== API REQUEST ==========")
        print("URL: \(url)")
        print("Method: POST")
        print("Headers: Authorization: \(authHeader.prefix(20))...")
        print("Body: \(String(data: request.httpBody ?? Data(), encoding: .utf8) ?? "nil")")
        print("=================================\n")
        
        // Make the request
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw TestError.invalidResponse
        }
        
        // Log the response
        let responseText = String(data: data, encoding: .utf8) ?? "Unable to decode response"
        print("\n========== API RESPONSE ==========")
        print("Status Code: \(httpResponse.statusCode)")
        print("Body: \(responseText)")
        print("==================================\n")
        
        guard (200...299).contains(httpResponse.statusCode) else {
            throw TestError.apiError(statusCode: httpResponse.statusCode, message: responseText)
        }
        
        guard let jsonResponse = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw TestError.invalidJSONResponse
        }
        
        return jsonResponse
    }
    
    /// Extract session ID from session response
    func extractSessionId(from sessionResponse: [String: Any]) -> String? {
        return sessionResponse["payment_session_id"] as? String
    }
    
    /// Get session details
    func getSession(sessionId: String) async throws -> [String: Any] {
        guard let url = URL(string: "\(baseUrl)/sessions/\(sessionId)") else {
            throw TestError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        // Set authorization header
        let authHeader: String
        if apiKey.hasPrefix("Basic ") {
            authHeader = apiKey
        } else {
            let authData = "\(apiKey):".data(using: .utf8)!
            authHeader = "Basic \(authData.base64EncodedString())"
        }
        request.setValue(authHeader, forHTTPHeaderField: "Authorization")
        
        // Log the GET request
        print("\n========== API REQUEST (GET) ==========")
        print("URL: \(url)")
        print("Method: GET")
        print("Headers: Authorization: \(authHeader.prefix(20))...")
        print("=======================================\n")

        // Make the request
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw TestError.invalidResponse
        }
        
        // Log the response
        let responseText = String(data: data, encoding: .utf8) ?? "Unable to decode response"
        print("\n========== API RESPONSE (GET) ==========")
        print("Status Code: \(httpResponse.statusCode)")
        print("Body: \(responseText)")
        print("=========================================\n")
        
        guard httpResponse.statusCode == 200 else {
            throw TestError.apiError(statusCode: httpResponse.statusCode, message: responseText)
        }
        
        guard let jsonResponse = try JSONSerialization.jsonObject(with: data) as? [String: Any] else {
            throw TestError.invalidJSONResponse
        }
        
        return jsonResponse
    }
}

