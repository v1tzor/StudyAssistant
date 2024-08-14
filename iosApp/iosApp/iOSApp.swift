import SwiftUI
import Firebase
import FirebaseCore
import FirebaseAuth
import GoogleSignIn
import shared
import OAuth2

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
            ContentView().onOpenURL(perform: { url in
                            print("onOpenURI")
                            GIDSignIn.sharedInstance.handle(url)
                        })
        }
    }
}
