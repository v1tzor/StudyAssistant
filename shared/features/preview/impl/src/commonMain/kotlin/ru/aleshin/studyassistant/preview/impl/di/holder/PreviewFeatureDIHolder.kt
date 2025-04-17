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

package ru.aleshin.studyassistant.preview.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.api.PreviewFeatureApi
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.modules.domainModule
import ru.aleshin.studyassistant.preview.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.preview.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
public object PreviewFeatureDIHolder : BaseFeatureDIHolder<PreviewFeatureApi, PreviewFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: PreviewFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindInstance<() -> AuthFeatureStarter> { dependencies.authFeatureStarter }
                bindInstance<() -> NavigationFeatureStarter> { dependencies.navigationFeatureStarter }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<PreviewFeatureApi> {
                    object : PreviewFeatureApi {
                        override fun fetchStarter() = instance<PreviewFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): PreviewFeatureApi {
        return fetchDI().instance<PreviewFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Preview feature DI is not initialized"
    }
}
