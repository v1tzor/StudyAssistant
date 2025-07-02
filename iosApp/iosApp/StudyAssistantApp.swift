import SwiftUI
import Appwrite
import Firebase
import FirebaseCore
import FirebaseAuth
import FirebaseMessaging
import GoogleSignIn
import shared
import OAuth2
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
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
    
    init() {
        let appwriteManager = AppwriteManager()
        let appwrite = RemoteAppwriteApple.init(
            auth: AppwriteAuthApple(account: appwriteManager.account),
            databases: AppwriteDatabaseApple(database: appwriteManager.databases),
            realtime: AppwriteRealtimeApple(realtime: appwriteManager.realtime),
            storage: AppwriteStorageApple(storage: appwriteManager.storage)
        )
        let appService = AppServiceImpl()
        let crashlyticsService = CrashlyticsServiceImpl()
        let analyticsService = AnalyticsServiceImpl()
        let tokenProvider = GoogleAuthTokenProvider()
        let iapService = IapServiceImpl()
        let uuidProvider = UUIDProvider()
        let configuration = PlatformConfiguration(
            appService: appService,
            analyticsService: analyticsService,
            crashlyticsService: crashlyticsService,
            iapService: iapService,
            appwrite: appwrite,
            serviceTokenProvider: tokenProvider,
            uuidProvider: uuidProvider
        )
        
        PlatformSDK().doInit(configuration: configuration)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in GIDSignIn.sharedInstance.handle(url)})
        }
    }
}
