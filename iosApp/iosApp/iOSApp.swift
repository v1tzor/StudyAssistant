import SwiftUI
import Firebase
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
           return true
       }
    
    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
      print("Handle URI")
      return GIDSignIn.sharedInstance.handle(url)
    }
}

@main
struct iOSApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        FirebaseConfiguration.shared.setLoggerLevel(.min)
        FirebaseApp.configure()
        PlatformSDK().doInit(configuration: PlatformConfiguration())
    }
    
    var body: some Scene {
        WindowGroup {
            let _ = print("Scene")
            ContentView().onOpenURL(perform: { url in
                            print("onOpenURI")
                            GIDSignIn.sharedInstance.handle(url)
                        })
        }
    }
}
