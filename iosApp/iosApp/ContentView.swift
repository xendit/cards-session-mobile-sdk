import SwiftUI
import cardsSdk

struct ContentView: View {
    let appModule: AppModule
    private let cardSessions: CardSessions
    @State private var isLoading = false
    @State private var error: String?
    @State private var cardResponse: String?
    @State private var paymentSessionId = ""
    @State private var isConfirmedSaved = false

    init(appModule: AppModule) {
        self.appModule = appModule
        self.cardSessions = CardSessionsFactory().create(
            apiKey: "API_KEY_HERE",
            isEnableLog: true
        )
    }
    
    var body: some View {
        VStack(spacing: 16) {
            Spacer().frame(height: 48)
            
            Text("Welcome to Cards Session!")
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            
            VStack(spacing: 8) {
                Text("paymentSessionId: ")
                TextField("Key in payment session id", text: $paymentSessionId)
                    .textFieldStyle(CustomTextFieldStyle())
                    .keyboardType(.default)
                HStack(spacing: 8) {
                    Text("Confirm Save")
                        .bold()
                    Toggle("", isOn: $isConfirmedSaved)
                }
            }
            
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
                            cardholderPhoneNumber: "+123456789",
                            paymentSessionId: paymentSessionId,
                            confirmSave: isConfirmedSaved
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
                            paymentSessionId: paymentSessionId
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
