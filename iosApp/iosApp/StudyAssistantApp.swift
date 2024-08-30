import SwiftUI
import Firebase
import FirebaseCore
import FirebaseAuth
import FirebaseMessaging
import GoogleSignIn
import shared
import OAuth2
import BackgroundTasks

class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    
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
        FirebaseConfiguration.shared.setLoggerLevel(.min)
        let filePath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist")
        guard let fileopts = FirebaseOptions(contentsOfFile: filePath!) else { assert(false, "Couldn't load config file") }
        FirebaseApp.configure(options: fileopts)
        
        let tokenProvider = GoogleAuthTokenProvider()
        let uuidProvider = UUIDProvider()
        let configuration = PlatformConfiguration(serviceTokenProvider: tokenProvider, uuidProvider: uuidProvider)
        
        PlatformSDK().doInit(configuration: configuration)
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in GIDSignIn.sharedInstance.handle(url)})
        }
    }
}
