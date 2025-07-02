//
//  CrashlyticsServiceImpl.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 13.09.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import Firebase
import FirebaseCore
import shared

public class CrashlyticsServiceImpl : CommonCrashlyticsService {
    public func sendLog(message: String) {}
    public func initializeService() {}
    public func recordException(tag: String, message: String, exception: KotlinThrowable) {}
}
