//
//  UUIDProvider.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 07.08.2024.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import shared
import FCUUID

public class UUIDProvider : CommonIosUUIDProvider {
    
    public func uuid() -> String {
        return FCUUID.uuid()
    }
    
    public func uuidForDevice() -> String {
        return FCUUID.uuidForDevice()
    }
    
    public func uuidForInstallation() -> String {
        return FCUUID.uuidForInstallation()
    }
    
    public func uuidForSession() -> String {
        return FCUUID.uuidForSession()
    }
    
    public func uuidForVendor() -> String {
        return FCUUID.uuidForVendor()
    }
}
