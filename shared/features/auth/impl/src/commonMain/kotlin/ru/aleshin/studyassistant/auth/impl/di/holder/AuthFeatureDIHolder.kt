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

package ru.aleshin.studyassistant.auth.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.di.AuthFeatureApi
import ru.aleshin.studyassistant.auth.api.navigation.AuthComponentProvider
import ru.aleshin.studyassistant.auth.impl.di.AuthFeatureDependencies
import ru.aleshin.studyassistant.auth.impl.di.modules.domainModule
import ru.aleshin.studyassistant.auth.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.core.api.auth.AccountService
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AppService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubscriptionsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
public object AuthFeatureDIHolder : BaseFeatureDIHolder<AuthFeatureApi, AuthFeatureDependencies> {

    private var directDI: DirectDI? = null

    override fun init(dependencies: AuthFeatureDependencies) {
        if (directDI == null) {
            val di = DI {
                importAll(presentationModule, domainModule)
                bindSingleton<AuthRepository> { dependencies.authRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<ManageUserRepository> { dependencies.manageUserRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }
                bindSingleton<GeneralSettingsRepository> { dependencies.generalSettingsRepository }
                bindSingleton<SubscriptionsRepository> { dependencies.subscriptionsRepository }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<AccountService> { dependencies.accountService }
                bindSingleton<AppService> { dependencies.appService }
                bindSingleton<SourceSyncFacade> { dependencies.sourceSyncFacade }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<AuthFeatureApi> {
                    object : AuthFeatureApi {
                        override fun fetchComponentProvider() = instance<AuthComponentProvider>()
                    }
                }
            }
            directDI = di.direct
        }
    }

    override fun fetchApi(): AuthFeatureApi {
        return fetchDI().instance<AuthFeatureApi>()
    }

    override fun clear() {
        directDI = null
    }

    internal fun fetchDI() = checkNotNull(directDI) {
        "Auth feature DI is not initialized"
    }
}