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

import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDIHolder
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.managers.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ShareHomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.tasks.api.di.TasksFeatureApi
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.impl.di.TasksFeatureDependencies
import ru.aleshin.studyassistant.tasks.impl.di.modules.domainModule
import ru.aleshin.studyassistant.tasks.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.tasks.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
public object TasksFeatureDIHolder : BaseFeatureDIHolder<TasksFeatureApi, TasksFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: TasksFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindInstance<() -> UsersFeatureStarter> { dependencies.usersFeatureStarter }
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
                bindSingleton<ShareHomeworksRepository> { dependencies.shareHomeworksRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<MessageRepository> { dependencies.messageRepository }
                bindSingleton<TodoRepository> { dependencies.todoRepository }
                bindSingleton<TodoReminderManager> { dependencies.todoReminderManager }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<CrashlyticsService> { dependencies.crashlyticsService }
                bindSingleton<TasksFeatureApi> {
                    object : TasksFeatureApi {
                        override fun fetchStarter() = instance<TasksFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): TasksFeatureApi {
        return fetchDI().instance<TasksFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Tasks feature DI is not initialized"
    }
}