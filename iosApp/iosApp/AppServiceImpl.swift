//
//  AppServiceImpl.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 13.09.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import Firebase
import FirebaseCore
import shared

public class AppServiceImpl : CommonAppService {
    
    public var flavor: CommonFlavor = CommonFlavor.apple
    
    public var isAvailableServices: Bool {
        return FirebaseApp.app() != nil
    }
    
    public func initializeApp() {
        FirebaseConfiguration.shared.setLoggerLevel(.min)
        let filePath = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist")
        guard let fileopts = FirebaseOptions(contentsOfFile: filePath!) else { assert(false, "Couldn't load config file") }
        FirebaseApp.configure(options: fileopts)
    }
}
