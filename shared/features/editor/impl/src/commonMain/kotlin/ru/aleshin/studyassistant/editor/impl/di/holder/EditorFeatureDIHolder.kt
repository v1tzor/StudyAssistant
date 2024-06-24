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

package ru.aleshin.studyassistant.editor.impl.di.holder

import inject.BaseFeatureDIHolder
import managers.CoroutineManager
import managers.DateManager
import managers.TimeOverlayManager
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.EmployeeRepository
import repositories.HomeworksRepository
import repositories.OrganizationsRepository
import repositories.SubjectsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.di.EditorFeatureApi
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.impl.di.EditorFeatureDependencies
import ru.aleshin.studyassistant.editor.impl.di.modules.domainModule
import ru.aleshin.studyassistant.editor.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.editor.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
object EditorFeatureDIHolder : BaseFeatureDIHolder<EditorFeatureApi, EditorFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: EditorFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<EmployeeRepository> { dependencies.employeeRepository }
                bindSingleton<SubjectsRepository> { dependencies.subjectsRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworksRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<TimeOverlayManager> { dependencies.overlayManager }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<EditorFeatureApi> {
                    object : EditorFeatureApi {
                        override fun fetchStarter() = instance<EditorFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): EditorFeatureApi {
        return fetchDI().instance<EditorFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Editor feature DI is not initialized"
    }
}