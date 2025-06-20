
import android.app.Activity
import android.content.Intent
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.common.ApiException
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.ConsumeOwnedPurchaseReq
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesReq
import com.huawei.hms.iap.entity.ProductInfoReq
import com.huawei.hms.iap.entity.PurchaseIntentReq
import kotlinx.coroutines.CompletableDeferred
import ru.aleshin.studyassistant.core.common.platform.PlatformActivity
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPaymentResult
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProduct
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductPurchaseParams
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapProductType
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchase
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseType
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceAvailability
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapServiceError
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.data.mappers.mapToCommon
import ru.aleshin.studyassistant.data.mappers.toHuaweiType
import ru.aleshin.studyassistant.data.mappers.toIapFailure
import java.lang.ref.WeakReference
import java.util.Currency

/**
 * @author Stanislav Aleshin on 13.04.2025.
 */
class IapServiceImpl : IapService {

    private lateinit var iapClient: IapClient
    private var activityRef: WeakReference<Activity>? = null
    private var purchaseResultContinuation: CompletableDeferred<IapPaymentResult>? = null

    override fun init(activity: PlatformActivity) {
        activityRef = WeakReference(activity)
        iapClient = Iap.getIapClient(activity)
    }

    override fun fetchStore() = Store.APP_GALLERY

    override fun proceedIntent(intent: Intent?, requestCode: Int?) {
        if (intent != null) {
            handlePurchaseResult(intent)
        }
    }

    override suspend fun isAuthorizedUser(): Boolean {
        return try {
            val request = OwnedPurchasesReq().apply {
                priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            }
            val result = Tasks.await(iapClient.obtainOwnedPurchases(request))
            result.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS
        } catch (e: Exception) {
            val type = if (e.cause is IapApiException) {
                (e.cause as IapApiException).statusCode.toIapFailure()
            } else {
                IapFailure.UnknownError
            }
            type != IapFailure.AppGalleryUserNotSignedIn && type != IapFailure.UnknownError
        }
    }

    override suspend fun fetchServiceAvailability(): IapServiceAvailability {
        return try {
            val purchasesReq = OwnedPurchasesReq().apply {
                priceType = IapClient.PriceType.IN_APP_CONSUMABLE
            }

            val isEnvReadyResult = Tasks.await(iapClient.isEnvReady)
            val purchasesResult = Tasks.await(iapClient.obtainOwnedPurchases(purchasesReq))

            if (isEnvReadyResult.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS &&
                purchasesResult.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS
            ) {
                IapServiceAvailability.Available
            } else {
                IapServiceAvailability.Unavailable(IapApiException(isEnvReadyResult.status))
            }
        } catch (e: Exception) {
            IapServiceAvailability.Unavailable(e)
        }
    }

    override suspend fun purchaseProduct(params: IapProductPurchaseParams): IapPaymentResult {
        val activity = activityRef?.get() ?: return IapPaymentResult.Failure()

        return try {
            val req = PurchaseIntentReq().apply {
                productId = params.productId
                priceType = params.productType.toHuaweiType()
                developerPayload = params.developerPayload
            }

            val intentResult = Tasks.await(iapClient.createPurchaseIntent(req))
            val status = intentResult.status
            if (status.hasResolution()) {
                purchaseResultContinuation = CompletableDeferred()
                status.startResolutionForResult(activity, REQUEST_CODE_PURCHASE)
                purchaseResultContinuation!!.await()
            } else {
                IapPaymentResult.InvalidPaymentState
            }
        } catch (e: ApiException) {
            IapPaymentResult.Failure(
                purchaseId = null,
                invoiceId = null,
                orderId = null,
                quantity = params.quantity,
                productId = params.productId,
                sandbox = false,
                errorCode = e.statusCode,
                failure = e.statusCode.toIapFailure()
            )
        }
    }

    private fun handlePurchaseResult(data: Intent) {
        val purchaseResult = iapClient.parsePurchaseResultInfoFromIntent(data)
        if (purchaseResult != null) {
            val paymentResult = when (purchaseResult.returnCode) {
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    val purchaseData = InAppPurchaseData(purchaseResult.inAppPurchaseData)
                    IapPaymentResult.Success(
                        orderId = purchaseData.orderID,
                        purchaseId = purchaseData.purchaseToken,
                        productId = purchaseData.productId,
                        invoiceId = null,
                        sandbox = purchaseData.purchaseType == 0,
                        subscriptionToken = purchaseData.subscriptionId
                    )
                }
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    IapPaymentResult.Cancelled(purchaseId = null, sandbox = false)
                }
                else -> {
                    IapPaymentResult.Failure(
                        errorCode = purchaseResult.returnCode,
                        failure = purchaseResult.returnCode.toIapFailure()
                    )
                }
            }
            purchaseResultContinuation?.complete(paymentResult)
            purchaseResultContinuation = null
        }
    }

    override suspend fun fetchProducts(ids: List<String>): List<IapProduct> {
        val request = ProductInfoReq().apply {
            priceType = IapClient.PriceType.IN_APP_SUBSCRIPTION
            productIds = ids.toMutableList()
        }

        return try {
            val response = Tasks.await(iapClient.obtainProductInfo(request))
            if (response.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                response.productInfoList.map { info -> info.mapToCommon() }
            } else {
                throw IapServiceError(response.returnCode.toIapFailure())
            }
        } catch (e: Exception) {
            val type = if (e.cause is IapApiException) {
                (e.cause as IapApiException).statusCode.toIapFailure()
            } else {
                IapFailure.UnknownError
            }
            throw IapServiceError(type, e)
        }
    }

    override suspend fun fetchPurchases(): List<IapPurchase> {
        return fetchPurchasesByType(IapClient.PriceType.IN_APP_SUBSCRIPTION)
    }

    private suspend fun fetchPurchasesByType(type: Int): List<IapPurchase> {
        return try {
            val request = OwnedPurchasesReq().apply { priceType = type }
            val result = Tasks.await(iapClient.obtainOwnedPurchases(request))

            if (result.returnCode == OrderStatusCode.ORDER_STATE_SUCCESS) {
                result.inAppPurchaseDataList?.mapNotNull { data -> parsePurchaseData(data) } ?: emptyList()
            } else {
                throw IapServiceError(result.returnCode.toIapFailure())
            }
        } catch (e: Exception) {
            val type = if (e.cause is IapApiException) {
                (e.cause as IapApiException).statusCode.toIapFailure()
            } else {
                IapFailure.UnknownError
            }
            throw IapServiceError(type, e)
        }
    }

    private fun parsePurchaseData(data: String): IapPurchase? {
        return try {
            val purchase = InAppPurchaseData(data)
            IapPurchase(
                purchaseId = purchase.purchaseToken,
                productId = purchase.productId,
                productType = when (purchase.kind) {
                    IapClient.PriceType.IN_APP_SUBSCRIPTION -> IapProductType.SUBSCRIPTION
                    IapClient.PriceType.IN_APP_NONCONSUMABLE -> IapProductType.NON_CONSUMABLE_PRODUCT
                    else -> IapProductType.CONSUMABLE_PRODUCT
                },
                invoiceId = null,
                purchaseType = IapPurchaseType.UNDEFINED,
                description = null,
                purchaseTime = purchase.purchaseTime,
                orderId = purchase.orderID,
                price = purchase.price.toInt(),
                amountLabel = buildString {
                    append(purchase.price.toString())
                    append(Currency.getInstance(purchase.currency).symbol ?: "")
                },
                currency = purchase.currency,
                quantity = purchase.quantity,
                status = when (purchase.purchaseState) {
                    0 -> if (purchase.kind == 2) IapPurchaseStatus.CONFIRMED else IapPurchaseStatus.PAID
                    1 -> IapPurchaseStatus.CANCELLED
                    2 -> IapPurchaseStatus.REFUNDED
                    else -> IapPurchaseStatus.CREATED
                },
                developerPayload = purchase.developerPayload,
                subscriptionToken = purchase.subscriptionId,
                sandbox = purchase.purchaseType == 0
            )
        } catch (e: Exception) {
            val type = if (e.cause is IapApiException) {
                (e.cause as IapApiException).statusCode.toIapFailure()
            } else {
                IapFailure.UnknownError
            }
            throw IapServiceError(type, e)
        }
    }

    override suspend fun fetchPurchaseInfo(purchaseId: String): IapPurchase? {
        return fetchPurchases().firstOrNull { it.purchaseId == purchaseId }
    }

    override suspend fun confirmPurchase(purchaseId: String, developerPayload: String?) {
        try {
            val request = ConsumeOwnedPurchaseReq().apply {
                purchaseToken = purchaseId
            }
            Tasks.await(iapClient.consumeOwnedPurchase(request))
        } catch (e: Exception) {
            val type = if (e.cause is IapApiException) {
                (e.cause as IapApiException).statusCode.toIapFailure()
            } else {
                IapFailure.UnknownError
            }
            throw IapServiceError(type, e)
        }
    }

    override suspend fun deletePurchase(purchaseId: String) {
        // Huawei IAP doesn't support direct purchase deletion
    }

    companion object {
        private const val REQUEST_CODE_PURCHASE = 1001
    }
}