import SwiftUI
import cardsSdk

struct ContentView: View {
    let appModule: AppModule
    @StateObject private var viewModel: IOSCardSessionViewModel
    
    init(appModule: AppModule) {
        self.appModule = appModule
        _viewModel = StateObject(wrappedValue: IOSCardSessionViewModel(
            cardsPaymentSession: appModule.cardsPaymentSession
        ))
    }
    
    var body: some View {
        VStack(spacing: 16) {
            Spacer().frame(height: 48)
            
            Text("Welcome to Cards Session!")
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            
            Button(action: {
                viewModel.onEvent(event: CardSessionEvent.CollectCardData(
                    cardNumber: "4242424242424242",
                    expiryMonth: "12/22",
                    expiryYear: "2026",
                    cardholderFirstName: "First",
                    cardholderLastName: "Name",
                    cardholderEmail: "firstname@xendit.co",
                    cardholderPhoneNumber: "01231245242",
                    paymentSessionId: "1234567890",
                    deviceFingerprint: "1234567890"
                ))
            }) {
                Text("Collect Card Data")
            }
            
            Button(action: {
                viewModel.onEvent(event: CardSessionEvent.CollectCvn(
                    cvn: "123",
                    paymentSessionId: "1234567890",
                    deviceFingerprint: "1234567890"
                ))
            }) {
                Text("Collect CVN")
            }
            
            if viewModel.state.isLoading {
                ProgressView()
            } else if let error = viewModel.state.error {
                Text("Error: \(error)")
                    .foregroundColor(.red)
            } else if let response = viewModel.state.cardResponse {
                Text("Card Response: \(response)")
            }
            
            Spacer()
        }
        .padding()
        .onAppear {
            viewModel.startObserving()
        }
        .onDisappear {
            viewModel.dispose()
        }
    }
}



