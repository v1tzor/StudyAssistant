import SwiftUI
import Firebase
import Appwrite
import FirebaseCore
import FirebaseAuth
import FirebaseMessaging
import GoogleSignIn
import shared
import OAuth2
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
    var stateKeeper: StateKeeperDispatcher = StateKeeperDispatcher(savedState: nil)
    var backDispatcher: BackDispatcher = BackDispatcher()
    
    lazy var componentContext: ComponentContext = DefaultComponentContext(
        lifecycle: ApplicationLifecycle(),
        stateKeeper: stateKeeper,
        instanceKeeper: nil,
        backHandler: backDispatcher
    )
    
    func scene(_ scene: UIScene, openURLContexts URLContexts: Set<UIOpenURLContext>) {
        guard let url = URLContexts.first?.url,
            url.absoluteString.contains("appwrite-callback") else {
            return
        }

        WebAuthComponent.handleIncomingCookie(from: url)
    }
    
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
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        application.registerForRemoteNotifications()
        requestNotificationPermission()
        return true
    }
    
    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
}

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    let appService = AppServiceImpl()
    let crashlyticsService = CrashlyticsServiceImpl()
    let reviewService = ReviewServiceImpl()
    let analyticsService = AnalyticsServiceImpl()
    let tokenProvider = GoogleAuthTokenProvider()
    let iapService = IapServiceImpl()
    let uuidProvider = UUIDProvider()
    
    init() {
        let configuration = PlatformConfiguration(
            appService: appService,
            analyticsService: analyticsService,
            crashlyticsService: crashlyticsService,
            reviewService: reviewService,
            iapService: iapService,
            serviceTokenProvider: tokenProvider,
            uuidProvider: uuidProvider
        )
        
        PlatformSDK().doInit(configuration: configuration)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView(
                componentContext: delegate.componentContext,
                backDispatcher: delegate.backDispatcher
            ).onOpenURL(
                perform: { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
            )
        }
    }
}
