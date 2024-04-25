import SwiftUI
import Firebase
import shared

@main
struct iOSApp: App {

    init() {
        FirebaseApp.configure()
        PlatformSDK().doInit(configuration: PlatformConfiguration())
    }

	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
