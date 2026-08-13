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

package ru.aleshin.studyassistant.schedule.impl.di.holder

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureController
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.schedule.api.ScheduleContentProviderFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureApi
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.modules.domainModule
import ru.aleshin.studyassistant.schedule.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class ScheduleFeatureController(
    dependencies: ScheduleFeatureDependencies,
) : BaseFeatureController<ScheduleFeatureApi, ScheduleFeatureDependencies>(
    dependencies = dependencies,
) {

    override fun DI.MainBuilder.buildDIGraph(dependencies: ScheduleFeatureDependencies) {
        importAll(presentationModule, domainModule)

        bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
        bindSingleton<ScheduleShareRepository> { dependencies.scheduleShareRepository }
        bindSingleton<ScheduleImportRepository> { dependencies.scheduleImportRepository }
        bindSingleton<AdRewardRepository> { dependencies.adRewardRepository }
        bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
        bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
        bindSingleton<ProfileRepository> { dependencies.profileRepository }
        bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
        bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
        bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
        bindSingleton<TodoRepository> { dependencies.todoRepository }

        bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
        bindSingleton<NotificationSettingsRepository> { dependencies.notificationSettingsRepository }
        bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
        bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
        bindSingleton<Konnection> { dependencies.connectionManager }
        bindSingleton<DeviceInfoProvider> { dependencies.deviceInfoProvider }
        bindSingleton<DateManager> { dependencies.dateManager }
        bindSingleton<CoroutineManager> { dependencies.coroutineManager }
        bindSingleton<AppDispatchers> { dependencies.appDispatchers }

        bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

        bindSingleton<ScheduleFeatureApi> {
            object : ScheduleFeatureApi {
                override fun contentProviderFactory(): ScheduleContentProviderFactory = instance()
            }
        }
    }

    override fun fetchApi(): ScheduleFeatureApi {
        return directDI.instance<ScheduleFeatureApi>()
    }

    internal fun fetchDI() = directDI
}
