/*
 * Copyright 2026 Stanislav Aleshin
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
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureController
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.preview.api.PreviewContentProviderFactory
import ru.aleshin.studyassistant.preview.api.PreviewFeatureApi
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.modules.domainModule
import ru.aleshin.studyassistant.preview.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class PreviewFeatureController(
    dependencies: PreviewFeatureDependencies,
) : BaseFeatureController<PreviewFeatureApi, PreviewFeatureDependencies>(
    dependencies = dependencies,
) {

    override fun DI.MainBuilder.buildDIGraph(dependencies: PreviewFeatureDependencies) {
        importAll(presentationModule, domainModule)

        bindSingleton<ProfileRepository> { dependencies.profileRepository }
        bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
        bindSingleton<GeneralSettingsRepository> { dependencies.generalSettingsRepository }
        bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }

        bindSingleton<CoroutineManager> { dependencies.coroutineManager }
        bindSingleton<DateManager> { dependencies.dateManager }
        bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
        bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

        bindSingleton<PreviewFeatureApi> {
            object : PreviewFeatureApi {
                override fun contentProviderFactory(): PreviewContentProviderFactory = instance()
            }
        }
    }

    override fun fetchApi(): PreviewFeatureApi = directDI.instance()

    internal fun fetchDI() = directDI
}
