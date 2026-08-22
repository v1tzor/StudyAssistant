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

package ru.aleshin.studyassistant.tasks.impl.di.holder

import dev.tmapps.konnection.Konnection
import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureController
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.platform.services.ReviewService
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworkShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.tasks.api.TasksContentProviderFactory
import ru.aleshin.studyassistant.tasks.api.TasksFeatureApi
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.modules.domainModule
import ru.aleshin.studyassistant.tasks.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
public class TasksFeatureController(
    dependencies: TasksFeatureDependencies,
) : BaseFeatureController<TasksFeatureApi, TasksFeatureDependencies>(
    dependencies = dependencies,
) {

    override fun DI.MainBuilder.buildDIGraph(dependencies: TasksFeatureDependencies) {
        importAll(presentationModule, domainModule)

        bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
        bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
        bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
        bindSingleton<ProfileRepository> { dependencies.profileRepository }
        bindSingleton<DailyGoalsRepository> { dependencies.goalsRepository }
        bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
        bindSingleton<HomeworkShareRepository> { dependencies.homeworkShareRepository }
        bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
        bindSingleton<TodoRepository> { dependencies.todoRepository }
        bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
        bindSingleton<GeneralSettingsRepository> { dependencies.generalSettingsRepository }

        bindSingleton<TodoReminderManager> { dependencies.todoReminderManager }
        bindSingleton<Konnection> { dependencies.connectionManager }
        bindSingleton<DateManager> { dependencies.dateManager }
        bindSingleton<CoroutineManager> { dependencies.coroutineManager }

        bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
        bindSingleton<AnalyticsService> { dependencies.analyticsService }
        bindSingleton<ReviewService> { dependencies.reviewService }

        bindSingleton<TasksFeatureApi> {
            object : TasksFeatureApi {
                override fun contentProviderFactory(): TasksContentProviderFactory = instance()
            }
        }
    }

    override fun fetchApi(): TasksFeatureApi = directDI.instance()

    internal fun fetchDI() = directDI
}
