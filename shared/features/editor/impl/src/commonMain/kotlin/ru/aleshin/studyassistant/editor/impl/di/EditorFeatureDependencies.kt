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

package ru.aleshin.studyassistant.editor.impl.di

import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.managers.TimeOverlayManager
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
public interface EditorFeatureDependencies : BaseFeatureDependencies {
    public val baseScheduleRepository: BaseScheduleRepository
    public val customScheduleRepository: CustomScheduleRepository
    public val employeeRepository: EmployeeRepository
    public val subjectsRepository: SubjectsRepository
    public val organizationsRepository: OrganizationsRepository
    public val homeworksRepository: HomeworksRepository
    public val todoRepository: TodoRepository
    public val calendarSettingsRepository: CalendarSettingsRepository
    public val notificationSettingsRepository: NotificationSettingsRepository
    public val startClassesReminderManager: StartClassesReminderManager
    public val endClassesReminderManager: EndClassesReminderManager
    public val todoReminderManager: TodoReminderManager
    public val usersRepository: UsersRepository
    public val manageUserRepository: ManageUserRepository
    public val dateManager: DateManager
    public val overlayManager: TimeOverlayManager
    public val coroutineManager: CoroutineManager
}