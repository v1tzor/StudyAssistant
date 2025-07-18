//
//  ReviewServiceImpl.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 18.07.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared

public class ReviewServiceImpl : CommonReviewService {
    
    @nonobjc
    public func __requestReview(completionHandler: @escaping (Bool, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __requestReview()
                completionHandler(result, nil)
            } catch {
                completionHandler(false, error)
            }
        }
    }
    
    public func __requestReview() async throws -> Bool {
        return false
    }
}
