//
//  AppwriteAuth.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 29.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import Appwrite
import AppwriteModels
import JSONCodable


public class AppwriteAuthApple : RemoteAppwriteAuthApple {
    
    private var account: Account
    
    init(account: Account) {
        self.account = account
    }
    
    public override func __createUserWithEmail(email: String, password: String) async throws -> RemoteUser<NSDictionary>? {
        do {
            let user = try await account.create(
                userId: UUID().uuidString,
                email: email,
                password: password
            )
            return user.toRemoteUser()
        } catch let error as AppwriteError {
//            RemoteAppwriteException(message: error.message, code: error.code, type: error.type, response: error.response)
            throw error
        } catch {
            throw error
        }
    }

    
    public override func __fetchCurrentUser() async throws -> RemoteUser<NSDictionary>? {
        do {
            return try await account.get().toRemoteUser()
        } catch {
            return nil
        }
    }
    
    public override func __sendPasswordRecoveryEmail(email: String) async throws {
        let result = try await account.createRecovery(email: email, url: "deeplink://openMain")
    }
    
    public override func __sendVerifyEmail() async throws {
        let result = try await account.createVerification(url: "deeplink://openMain")
    }
    
    public override func __signInViaGoogle(idToken: String?) async throws -> RemoteUser<NSDictionary>? {
        return nil // TODO: signInViaGoogle
    }
    
    public override func __signInWithEmail(email: String, password: String) async throws -> RemoteUser<NSDictionary>? {
        let session = try await account.createEmailPasswordSession(email: email, password: password)
        let user = try await account.get()
        return user.toRemoteUser()
    }
    
    public override func __signOut() async throws {
        let result = try await account.deleteSession(sessionId: "current")
    }
    
    public override func __updatePassword(oldPassword: String?, newPassword: String) async throws {
        let _ = try await account.updateSession(sessionId: "current")
        let result = try await account.updatePassword(password: newPassword, oldPassword: oldPassword)
    }
    
    public override func __updateRecoveredPassword(secret: String, password: String) async throws {
        let user = try await account.get()
        let result = try await account.updateRecovery(userId: user.id, secret: secret, password: password)
    }
    
    public override func __updateVerification(secret: String) async throws {
        let user = try await account.get()
        let result = try await account.updateVerification(userId: user.id, secret: secret)
    }
    
    public override func __reloadUser(secret: String) async throws -> RemoteUser<NSDictionary>? {
        let _ = try await account.updateSession(sessionId: "current")
        let user = try await account.get()
        return user.toRemoteUser()
    }
}

extension AppwriteModels.User<[String: AnyCodable]> {
    func toRemoteUser() -> RemoteUser<NSDictionary> {
        return RemoteUser(
            id: self.id,
            createdAt: self.createdAt,
            updatedAt: self.updatedAt,
            name: self.name,
            password: self.password,
            hash: self.hash,
            hashOptions: self.hashOptions,
            registration: self.registration,
            status: self.status,
            labels: self.labels,
            passwordUpdate: self.passwordUpdate,
            email: self.email,
            phone: self.phone,
            emailVerification: self.emailVerification,
            phoneVerification: self.phoneVerification,
            mfa: self.mfa,
            prefs: RemotePreferences<NSDictionary>(data: self.prefs.data as NSDictionary),
            targets: self.targets.map { target in
                RemoteTarget(
                    id: target.id,
                    createdAt: target.createdAt,
                    updatedAt: target.updatedAt,
                    name: target.name,
                    userId: target.userId,
                    providerId: target.providerId,
                    providerType: target.providerType,
                    identifier: target.identifier,
                    expired: target.expired
                )
            },
            accessedAt: self.accessedAt
        )
    }
}
