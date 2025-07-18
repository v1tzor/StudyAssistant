/*
 * Copyright 2024 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.billing.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.di.BillingFeatureApi
import ru.aleshin.studyassistant.billing.api.navigation.BillingFeatureStarter
import ru.aleshin.studyassistant.billing.impl.di.BillingFeatureDependencies
import ru.aleshin.studyassistant.billing.impl.di.modules.domainModule
import ru.aleshin.studyassistant.billing.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.billing.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
public object BillingFeatureDIHolder : BaseFeatureDIHolder<BillingFeatureApi, BillingFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: BillingFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<ProductsRepository> { dependencies.productsRepository }
                bindSingleton<ManageUserRepository> { dependencies.manageUserRepository }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<IapService> { dependencies.iapService }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<AnalyticsService> { dependencies.analyticsService }
                bindSingleton<BillingFeatureApi> {
                    object : BillingFeatureApi {
                        override fun fetchStarter() = instance<BillingFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): BillingFeatureApi {
        return fetchDI().instance<BillingFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Billing feature DI is not initialized"
    }
}