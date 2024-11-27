import Foundation
import cardsSdk
import Combine

@MainActor
class IOSCardSessionViewModel: ObservableObject {
    private let viewModel: CardSessionViewModel
    private var handle: DisposableHandle?

    @Published var state: CardSessionState = CardSessionState(
        cardResponse: nil,
        isLoading: false,
        error: nil
    )

    init(cardsPaymentSession: CardsPaymentSession) {
        self.viewModel = CardSessionViewModel(
            cardsPaymentSession: cardsPaymentSession,
            coroutineScope: nil
        )
    }

    func onEvent(event: CardSessionEvent) {
        viewModel.onEvent(event: event)
    }

    func startObserving() {
        handle = viewModel.state.subscribe { [weak self] state in
            if let state = state {
                self?.state = state
            }
        }
    }

    func dispose() {
        handle?.dispose()
    }
}