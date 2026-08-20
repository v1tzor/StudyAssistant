//
//  AnalyticsServiceImpl.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 18.04.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import AppMetricaCore

public class AnalyticsServiceImpl : CommonAnalyticsService {

    public func setupUserId(id: String) {
        AppMetrica.userProfileID = id
    }

    public func initializeService() {
        guard let apiKey = Bundle.main.object(forInfoDictionaryKey: "AppMetricaApiKey") as? String, !apiKey.isEmpty else {
            return
        }
        if let configuration = AppMetricaConfiguration(apiKey: apiKey) {
            AppMetrica.activate(with: configuration)
        }
    }

    public func trackEvent(name: String, eventParams: [String : String]) {
        AppMetrica.reportEvent(name: name, parameters: eventParams, onFailure: nil)
    }
}
