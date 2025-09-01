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

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.billing.api.BillingFeatureApi
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponentFactory
import ru.aleshin.studyassistant.billing.impl.di.BillingFeatureDependencies
import ru.aleshin.studyassistant.billing.impl.di.modules.domainModule
import ru.aleshin.studyassistant.billing.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapService
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
public object BillingFeatureManager : BaseFeatureManager<BillingFeatureApi, BillingFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: BillingFeatureDependencies): BillingFeatureApi {
        val diGraph = directDi

        return if (diGraph != null) {
            diGraph.instance<BillingFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<SubscriptionsRepository> { dependencies.subscriptionsRepository }
                bindSingleton<ManageUserRepository> { dependencies.manageUserRepository }

                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<Konnection> { dependencies.connectionManager }

                bindSingleton<IapService> { dependencies.iapService }
                bindSingleton<AnalyticsService> { dependencies.analyticsService }

                bindSingleton<BillingFeatureApi> {
                    object : BillingFeatureApi {
                        override fun componentFactory(): BillingFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            di.direct.instance<BillingFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Billing feature DI is not initialized"
    }
}