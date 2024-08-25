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

package ru.aleshin.studyassistant.schedule.impl.di

import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
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
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface ScheduleFeatureDependencies : BaseFeatureDependencies {
    val editorFeatureStarter: () -> EditorFeatureStarter
    val usersFeatureStarter: () -> UsersFeatureStarter
    val baseScheduleRepository: BaseScheduleRepository
    val customScheduleRepository: CustomScheduleRepository
    val shareSchedulesRepository: ShareSchedulesRepository
    val organizationsRepository: OrganizationsRepository
    val subjectsRepository: SubjectsRepository
    val employeeRepository: EmployeeRepository
    val calendarSettingsRepository: CalendarSettingsRepository
    val notificationSettingsRepository: NotificationSettingsRepository
    val startClassesReminderManager: StartClassesReminderManager
    val endClassesReminderManager: EndClassesReminderManager
    val homeworkRepository: HomeworksRepository
    val todoRepository: TodoRepository
    val usersRepository: UsersRepository
    val dateManager: DateManager
    val coroutineManager: CoroutineManager
}