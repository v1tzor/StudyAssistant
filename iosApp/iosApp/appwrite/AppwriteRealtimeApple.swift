//
//  AppwriteRealtime.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 30.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import Appwrite
import AppwriteEnums
import AppwriteModels
import JSONCodable

public class AppwriteRealtimeApple : RemoteAppwriteRealtimeApple {
    
    private var realtime: Realtime
    
    init(realtime: Realtime) {
        self.realtime = realtime
    }
    
    @nonobjc
    public override func __subscribe(
        channels: [String],
        callback: @escaping (RemoteRealtimeResponseEvent<AnyObject>) -> Void,
        completionHandler: @escaping (RemoteRealtimeSubscription?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __subscribe(channels: channels, callback: callback)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __subscribe(
        channels: [String],
        callback: @escaping (RemoteRealtimeResponseEvent<AnyObject>) -> Void
    ) async throws -> RemoteRealtimeSubscription {
        let anyCallback: (RealtimeResponseEvent) -> Void = { data in
            let converted = data.payload?.mapValues { value in
                return convertToKotlinCompatibleAnyObject(value)
            }
            
            let response = RemoteRealtimeResponseEvent<AnyObject>.init(
                events: data.events ?? [],
                channels: data.channels ?? [],
                timestamp: data.timestamp ?? "",
                payload: converted as? AnyObject
            )
            callback(response)
        }
        
        let subscription = try await realtime.subscribe(channels: Set(channels), callback: anyCallback)
        
        return RemoteRealtimeSubscription.init(close: { Task { try await subscription.close() } })
    }
}

