import SwiftUI
import shared

@main
struct iOSApp: App {

    init() {
        PlatformSDK().doInit(configuration: PlatformConfiguration())
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}