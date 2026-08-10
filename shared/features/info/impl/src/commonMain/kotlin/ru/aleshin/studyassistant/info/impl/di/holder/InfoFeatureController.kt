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

package ru.aleshin.studyassistant.info.impl.di.holder

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureController
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.info.api.InfoContentProviderFactory
import ru.aleshin.studyassistant.info.api.InfoFeatureApi
import ru.aleshin.studyassistant.info.impl.di.InfoFeatureDependencies
import ru.aleshin.studyassistant.info.impl.di.modules.domainModule
import ru.aleshin.studyassistant.info.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class InfoFeatureController(
    dependencies: InfoFeatureDependencies,
) : BaseFeatureController<InfoFeatureApi, InfoFeatureDependencies>(
    dependencies = dependencies,
) {

    override fun DI.MainBuilder.buildDIGraph(dependencies: InfoFeatureDependencies) {
                importAll(presentationModule, domainModule)
        
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
        
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
        
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

        bindSingleton<InfoFeatureApi> {
            object : InfoFeatureApi {
                override fun contentProviderFactory(): InfoContentProviderFactory = instance()
            }
        }
    }

    override fun fetchApi(): InfoFeatureApi = directDI.instance()

    internal fun fetchDI() = directDI
}
