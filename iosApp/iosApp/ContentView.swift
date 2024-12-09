import SwiftUI
import cardsSdk

struct ContentView: View {
    let appModule: AppModule
    private let cardSessions: CardSessions
    @State private var isLoading = false
    @State private var error: String?
    @State private var cardResponse: String?
    
    init(appModule: AppModule) {
        self.appModule = appModule
        self.cardSessions = CardSessionsFactory().create(
            apiKey: "xnd_public_development_1TFSZ1eyExAAhfR48LRlbTIH2WFT6jjvoLDFsjQUV3lJWuNghjCMJakP0hq0ZCV"
        )
    }
    
    var body: some View {
        VStack(spacing: 16) {
            Spacer().frame(height: 48)
            
            Text("Welcome to Cards Session!")
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            
            Button(action: {
                Task {
                    isLoading = true
                    error = nil
                    do {
                        let response = try await cardSessions.collectCardData(
                            cardNumber: "4242424242424242",
                            expiryMonth: "12",
                            expiryYear: "2026",
                            cvn: nil,
                            cardholderFirstName: "First",
                            cardholderLastName: "Name",
                            cardholderEmail: "firstname@xendit.co",
                            cardholderPhoneNumber: "01231245242",
                            paymentSessionId: "session_id MUST be 27 chars"
                        )
                        cardResponse = response.description
                    } catch {
                        self.error = error.localizedDescription
                    }
                    isLoading = false
                }
            }) {
                Text("Collect Card Data")
            }
            
            Button(action: {
                Task {
                    isLoading = true
                    error = nil
                    do {
                        let response = try await cardSessions.collectCvn(
                            cvn: "123",
                            paymentSessionId: "1234567890"
                        )
                        cardResponse = response.description
                    } catch {
                        self.error = error.localizedDescription
                    }
                    isLoading = false
                }
            }) {
                Text("Collect CVN")
            }
            
            if isLoading {
                ProgressView()
            } else if let error = error {
                Text("Error: \(error)")
                    .foregroundColor(.red)
            } else if let response = cardResponse {
                Text("Card Response: \(response)")
            }
            
            Spacer()
        }
        .padding()
    }
}



