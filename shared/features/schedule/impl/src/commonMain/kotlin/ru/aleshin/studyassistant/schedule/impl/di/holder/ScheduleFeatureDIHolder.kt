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

import inject.BaseFeatureDIHolder
import managers.CoroutineManager
import managers.DateManager
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import repositories.BaseScheduleRepository
import repositories.CalendarSettingsRepository
import repositories.CustomScheduleRepository
import repositories.HomeworksRepository
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.schedule.api.di.ScheduleFeatureApi
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.modules.domainModule
import ru.aleshin.studyassistant.schedule.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.schedule.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
object ScheduleFeatureDIHolder : BaseFeatureDIHolder<ScheduleFeatureApi, ScheduleFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: ScheduleFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<CustomScheduleRepository> { dependencies.customScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<HomeworksRepository> { dependencies.homeworkRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<ScheduleFeatureApi> {
                    object : ScheduleFeatureApi {
                        override fun fetchStarter() = instance<ScheduleFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): ScheduleFeatureApi {
        return fetchDI().instance<ScheduleFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Schedule feature DI is not initialized"
    }
}