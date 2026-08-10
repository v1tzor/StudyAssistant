import SwiftUI
import shared

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    var stateKeeper: StateKeeperDispatcher = StateKeeperDispatcher(savedState: nil)
    var backDispatcher: BackDispatcher = BackDispatcher()
    let deepLinkReceiver = DeepLinkReceiver()

    lazy var componentContext: ComponentContext = DefaultComponentContext(
        lifecycle: ApplicationLifecycle(),
        stateKeeper: stateKeeper,
        instanceKeeper: nil,
        backHandler: backDispatcher
    )

    func requestNotificationPermission() {
        let center = UNUserNotificationCenter.current()

        center.requestAuthorization(options: [.alert, .sound]) { (granted, error) in
            if let error = error {
                print("Ошибка запроса разрешений: \(error.localizedDescription)")
                return
            }

            if granted {} else {}
        }
    }

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        requestNotificationPermission()
        return true
    }

    func application(
        _ app: UIApplication,
        open url: URL,
        options: [UIApplication.OpenURLOptionsKey: Any] = [:]
    ) -> Bool {
        if url.scheme == "studyassistant" {
            deepLinkReceiver.open(url: url.absoluteString)
            return true
        }
        return false
    }
}

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    let crashlyticsService = CrashlyticsServiceImpl()
    let reviewService = ReviewServiceImpl()
    let analyticsService = AnalyticsServiceImpl()
    let uuidProvider = UUIDProvider()
    init() {
        let configuration = PlatformConfiguration(
            analyticsService: analyticsService,
            crashlyticsService: crashlyticsService,
            reviewService: reviewService,
            uuidProvider: uuidProvider
        )

        PlatformSDK().doInit(configuration: configuration)
    }

    var body: some Scene {
        WindowGroup {
            ContentView(
                componentContext: delegate.componentContext,
                backDispatcher: delegate.backDispatcher,
                deepLinkReceiver: delegate.deepLinkReceiver
            ).onOpenURL(
                perform: { url in
                    if url.scheme == "studyassistant" {
                        delegate.deepLinkReceiver.open(url: url.absoluteString)
                    }
                }
            )
        }
    }
}
