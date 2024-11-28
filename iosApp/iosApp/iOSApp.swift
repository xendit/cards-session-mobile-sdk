import SwiftUI
import cardsSdk

@main
struct iOSApp: App {

    private var appModule: any AppModule = AppModuleImpl()

	var body: some Scene {
		WindowGroup {
            NavigationView {
                ContentView(appModule: appModule)
            }
		}
	}
}
