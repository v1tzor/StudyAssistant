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

package ru.aleshin.studyassistant.tasks.impl.di.holder

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureManager
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.api.TasksFeatureApi
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponentFactory
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.modules.domainModule
import ru.aleshin.studyassistant.tasks.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
public object TasksFeatureManager : BaseFeatureManager<TasksFeatureApi, TasksFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun createOrGetFeature(dependencies: TasksFeatureDependencies): TasksFeatureApi {
        val diGraph = directDi

        return if (diGraph != null) {
            diGraph.instance<TasksFeatureApi>()
        } else {
            val di = DI {
                importAll(presentationModule, domainModule)

                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<DailyGoalsRepository> { dependencies.goalsRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
                bindSingleton<ShareHomeworksRepository> { dependencies.shareHomeworksRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }
                bindSingleton<TodoRepository> { dependencies.todoRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }

                bindSingleton<TodoReminderManager> { dependencies.todoReminderManager }
                bindSingleton<Konnection> { dependencies.connectionManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }

                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }

                bindSingleton<TasksFeatureApi> {
                    object : TasksFeatureApi {
                        override fun componentFactory(): TasksFeatureComponentFactory = instance()
                    }
                }
            }
            directDi = di.direct

            di.direct.instance<TasksFeatureApi>()
        }
    }

    override fun finish() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Tasks feature DI is not initialized"
    }
}