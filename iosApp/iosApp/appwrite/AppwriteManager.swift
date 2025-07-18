//
//  Appwrite.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 26.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import Foundation
import shared
import Appwrite
import AppwriteModels
import JSONCodable

class AppwriteManager: ObservableObject {
    var client: Client
    var account: Account
    var realtime: Realtime
    var databases: Databases
    var storage: Storage
    
    public init() {
        self.client = Client()
            .setEndpoint("https://fra.cloud.appwrite.io/v1")
            .setEndpointRealtime("wss://fra.cloud.appwrite.io/v1/realtime")
            .setProject("685aefd7003bf3aab9fc")
        
        self.account = Account(client)
        self.realtime = Realtime(client)
        self.databases = Databases(client)
        self.storage = Storage(client)
    }
}

func convertToKotlinCompatibleDictionary(_ dict: [String: AnyCodable]) -> NSDictionary {
    let result = NSMutableDictionary()
    
    for (key, value) in dict {
        switch value.value {
        case let string as String:
            result[key] = string
        case let number as NSNumber:
            result[key] = number
        case let bool as Bool:
            result[key] = NSNumber(value: bool)
        case let int as Int:
            result[key] = NSNumber(value: int)
        case let double as Double:
            result[key] = NSNumber(value: double)
        case let float as Float:
            result[key] = NSNumber(value: float)
        case let array as [Any]:
            result[key] = array.map { convertAnyToKotlinCompatible($0) }
        case let nestedDict as [String: Any]:
            let codableDict = nestedDict.mapValues { AnyCodable($0) }
            result[key] = convertToKotlinCompatibleDictionary(codableDict)
        case let null as NSNull:
            result[key] = null
        default:
            result[key] = String(describing: value)
        }
    }
    
    return result.copy() as! NSDictionary
}

private func convertAnyToKotlinCompatible(_ value: Any) -> Any {
    switch value {
    case let string as String:
        return string
    case let number as NSNumber:
        return number
    case let bool as Bool:
        return NSNumber(value: bool)
    case let int as Int:
        return NSNumber(value: int)
    case let double as Double:
        return NSNumber(value: double)
    case let float as Float:
        return NSNumber(value: float)
    case let array as [Any]:
        return array.map { convertAnyToKotlinCompatible($0) }
    case let nestedDict as [String: Any]:
        let codableDict = nestedDict.mapValues { AnyCodable($0) }
        return convertToKotlinCompatibleDictionary(codableDict)
    case let null as NSNull:
        return null
    default:
        return String(describing: value)
    }
}

func convertToKotlinCompatibleAnyObject(_ value: Any) -> AnyObject {
    switch value {
    case let string as String:
        return string as AnyObject
    case let number as NSNumber:
        return number
    case let bool as Bool:
        return NSNumber(value: bool)
    case let int as Int:
        return NSNumber(value: int)
    case let double as Double:
        return NSNumber(value: double)
    case let float as Float:
        return NSNumber(value: float)
    case let array as [Any]:
        return array.map { convertToKotlinCompatibleAnyObject($0) } as AnyObject
    case let dict as [String: Any]:
        let converted = dict.mapValues { convertToKotlinCompatibleAnyObject($0) }
        return converted as AnyObject
    case let null as NSNull:
        return null
    default:
        return String(describing: value) as AnyObject
    }
}
