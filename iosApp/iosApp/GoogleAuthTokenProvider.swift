//
//  TokenProvider.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 06.08.2024.
//  Copyright © 2024 StudyAssistant. All rights reserved.
//

import Foundation
import OAuth2
import shared

public class GoogleAuthTokenProvider : PlatformGoogleAuthTokenProvider {
    
    public func fetchAccessToken(scope: String) async throws -> String? {
        // Locate the credentials URL
        guard let credentialsURL = Bundle.main.url(forResource: "service-account-file", withExtension: "json") else {
            throw NSError(domain: "FetchAccessTokenError", code: 404, userInfo: [NSLocalizedDescriptionKey: "Credentials file not found."])
        }
        
        // Initialize the token provider
        guard let tokenProvider = ServiceAccountTokenProvider(credentialsURL: credentialsURL, scopes: [scope]) else {
            throw NSError(domain: "FetchAccessTokenError", code: 500, userInfo: [NSLocalizedDescriptionKey: "Failed to create token provider."])
        }
        
        // Fetch the token
        let token = try await withToken(tokenProvider: tokenProvider)
        
        return token
    }
    
    public func withToken(tokenProvider: TokenProvider) async throws -> String? {
        return try await withCheckedThrowingContinuation { continuation in
            do {
                try tokenProvider.withToken { (token, error) in
                    // If an error occurs in the closure, resume with the error
                    if let error = error {
                        continuation.resume(throwing: error)
                    } else {
                        // Otherwise, resume with the access token
                        continuation.resume(returning: token?.AccessToken)
                    }
                }
            } catch {
                // Handle error thrown from tokenProvider.withToken
                continuation.resume(throwing: error)
            }
        }
        
    }
}

