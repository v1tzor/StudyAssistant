//
//  IapServiceImpl.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 26.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared

@objc(CommonIapServiceImpl)
public class IapServiceImpl: NSObject, CommonIapService {

    public func __confirmPurchase(purchaseId: String, developerPayload: String?, completionHandler: @escaping ((any Error)?) -> Void) {
        Task {
            do {
                try await __confirmPurchase(purchaseId: purchaseId, developerPayload: developerPayload)
                completionHandler(nil)
            } catch {
                completionHandler(error)
            }
        }
    }

    @nonobjc
    public func __confirmPurchase(purchaseId: String, developerPayload: String?) async throws {
        try await confirmPurchase(purchaseId: purchaseId, developerPayload: developerPayload)
    }

    public func __deletePurchase(purchaseId: String, completionHandler: @escaping ((any Error)?) -> Void) {
        Task {
            do {
                try await __deletePurchase(purchaseId: purchaseId)
                completionHandler(nil)
            } catch {
                completionHandler(error)
            }
        }
    }

    @nonobjc
    public func __deletePurchase(purchaseId: String) async throws {
        try await deletePurchase(purchaseId: purchaseId)
    }

    public func __fetchProducts(ids: [String], completionHandler: @escaping ([CommonIapProduct]?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __fetchProducts(ids: ids)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __fetchProducts(ids: [String]) async throws -> [CommonIapProduct] {
        return try await fetchProducts(ids: ids)
    }

    public func __fetchPurchaseInfo(purchaseId: String, completionHandler: @escaping (CommonIapPurchase?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __fetchPurchaseInfo(purchaseId: purchaseId)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __fetchPurchaseInfo(purchaseId: String) async throws -> CommonIapPurchase? {
        return try await fetchPurchaseInfo(purchaseId: purchaseId)
    }

    public func __fetchPurchases(completionHandler: @escaping ([CommonIapPurchase]?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __fetchPurchases()
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __fetchPurchases() async throws -> [CommonIapPurchase] {
        return try await fetchPurchases()
    }

    public func __fetchServiceAvailability(completionHandler: @escaping (IapServiceAvailability?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __fetchServiceAvailability()
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __fetchServiceAvailability() async throws -> IapServiceAvailability {
        return try await fetchServiceAvailability()
    }

    public func __isAuthorizedUser(completionHandler: @escaping (KotlinBoolean?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __isAuthorizedUser()
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __isAuthorizedUser() async throws -> KotlinBoolean {
        return try await isAuthorizedUser()
    }

    public func __purchaseProduct(params: CommonIapProductPurchaseParams, completionHandler: @escaping ((any IapPaymentResult)?, (any Error)?) -> Void) {
        Task {
            do {
                let result = try await __purchaseProduct(params: params)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }

    @nonobjc
    public func __purchaseProduct(params: CommonIapProductPurchaseParams) async throws -> any IapPaymentResult {
        return try await purchaseProduct(params: params)
    }

    public func confirmPurchase(purchaseId: String, developerPayload: String?, completionHandler: @escaping (NSError?) -> Void) {
        Task {
            do {
                try await confirmPurchase(purchaseId: purchaseId, developerPayload: developerPayload)
                completionHandler(nil)
            } catch {
                completionHandler(error as NSError)
            }
        }
    }

    public func confirmPurchase(purchaseId: String, developerPayload: String?) async throws {
        // Your real implementation here
    }

    public func deletePurchase(purchaseId: String, completionHandler: @escaping (NSError?) -> Void) {
        Task {
            do {
                try await deletePurchase(purchaseId: purchaseId)
                completionHandler(nil)
            } catch {
                completionHandler(error as NSError)
            }
        }
    }

    public func deletePurchase(purchaseId: String) async throws {
        // Your real implementation here
    }

    public func fetchProducts(ids: [String], completionHandler: @escaping ([CommonIapProduct]?, NSError?) -> Void) {
        Task {
            do {
                let products = try await fetchProducts(ids: ids)
                completionHandler(products, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func fetchProducts(ids: [String]) async throws -> [CommonIapProduct] {
        return []
    }

    public func fetchPurchaseInfo(purchaseId: String, completionHandler: @escaping (CommonIapPurchase?, NSError?) -> Void) {
        Task {
            do {
                let purchase = try await fetchPurchaseInfo(purchaseId: purchaseId)
                completionHandler(purchase, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func fetchPurchaseInfo(purchaseId: String) async throws -> CommonIapPurchase? {
        return nil
    }

    public func fetchPurchases(completionHandler: @escaping ([CommonIapPurchase]?, NSError?) -> Void) {
        Task {
            do {
                let purchases = try await fetchPurchases()
                completionHandler(purchases, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func fetchPurchases() async throws -> [CommonIapPurchase] {
        return []
    }

    public func fetchServiceAvailability(completionHandler: @escaping (IapServiceAvailability?, NSError?) -> Void) {
        Task {
            do {
                let availability = try await fetchServiceAvailability()
                completionHandler(availability, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func fetchServiceAvailability() async throws -> IapServiceAvailability {
        return IapServiceAvailabilityUnavailable(throwable: nil)
    }

    public func fetchStore() -> CommonStore {
        return CommonStore.none
    }

    public func doInit(activity: CommonPlatformActivity) {
        // Implement if needed
    }

    public func isAuthorizedUser(completionHandler: @escaping (KotlinBoolean?, NSError?) -> Void) {
        Task {
            do {
                let result = try await isAuthorizedUser()
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func isAuthorizedUser() async throws -> KotlinBoolean {
        return KotlinBoolean(bool: false)
    }

    public func proceedIntent(intent: CommonPlatformIntent?, requestCode: KotlinInt?) {
        // Implement if needed
    }

    public func purchaseProduct(params: CommonIapProductPurchaseParams, completionHandler: @escaping (IapPaymentResult?, NSError?) -> Void) {
        Task {
            do {
                let result = try await purchaseProduct(params: params)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error as NSError)
            }
        }
    }

    public func purchaseProduct(params: CommonIapProductPurchaseParams) async throws -> IapPaymentResult {
        return IapPaymentResultInvalidPaymentState()
    }
}
