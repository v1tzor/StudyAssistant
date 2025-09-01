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
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.preview.api.PreviewFeatureApi
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponentFactory
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.modules.domainModule
import ru.aleshin.studyassistant.preview.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
public object PreviewFeatureManager : BaseFeatureManager<PreviewFeatureApi, PreviewFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: PreviewFeatureDependencies): PreviewFeatureApi {
        val diGraph = directDi

        return if (diGraph != null) {
            diGraph.instance<PreviewFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<GeneralSettingsRepository> { dependencies.generalSettingsRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }

                bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<DateManager> { dependencies.dateManager }

                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

                bindSingleton<PreviewFeatureApi> {
                    object : PreviewFeatureApi {
                        override fun componentFactory(): PreviewFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            di.direct.instance<PreviewFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Preview feature DI is not initialized"
    }
}