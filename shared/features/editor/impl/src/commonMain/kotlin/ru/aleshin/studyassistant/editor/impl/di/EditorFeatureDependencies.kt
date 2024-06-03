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

import inject.BaseFeatureDependencies
import managers.CoroutineManager
import managers.DateManager
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.EmployeeRepository
import repositories.OrganizationsRepository
import repositories.SubjectsRepository
import repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
interface EditorFeatureDependencies : BaseFeatureDependencies {
    val baseScheduleRepository: BaseScheduleRepository
    val customScheduleRepository: CustomScheduleRepository
    val employeeRepository: EmployeeRepository
    val subjectsRepository: SubjectsRepository
    val organizationsRepository: OrganizationsRepository
    val calendarSettingsRepository: CalendarSettingsRepository
    val usersRepository: UsersRepository
    val dateManager: DateManager
    val coroutineManager: CoroutineManager
}