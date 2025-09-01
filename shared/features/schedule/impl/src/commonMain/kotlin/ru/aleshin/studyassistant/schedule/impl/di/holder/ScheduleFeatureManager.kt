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

package ru.aleshin.studyassistant.schedule.impl.di.holder

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureApi
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponentFactory
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.modules.domainModule
import ru.aleshin.studyassistant.schedule.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public object ScheduleFeatureManager : BaseFeatureManager<ScheduleFeatureApi, ScheduleFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: ScheduleFeatureDependencies): ScheduleFeatureApi {
        val diGraph = directDi

        return if (diGraph != null) {
            diGraph.instance<ScheduleFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<ShareSchedulesRepository> { dependencies.shareSchedulesRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<TodoRepository> { dependencies.todoRepository }

                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<NotificationSettingsRepository> { dependencies.notificationSettingsRepository }
                bindSingleton<StartClassesReminderManager> { dependencies.startClassesReminderManager }
                bindSingleton<EndClassesReminderManager> { dependencies.endClassesReminderManager }
                bindSingleton<Konnection> { dependencies.connectionManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }

                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

                bindProvider<ScheduleFeatureApi> {
                    object : ScheduleFeatureApi {
                        override fun componentFactory(): ScheduleFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            di.direct.instance<ScheduleFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Schedule feature DI is not initialized"
    }
}