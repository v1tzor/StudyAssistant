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

package ru.aleshin.studyassistant.info.impl.di.holder

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
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.info.api.di.InfoFeatureApi
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.info.impl.di.InfoFeatureDependencies
import ru.aleshin.studyassistant.info.impl.di.modules.domainModule
import ru.aleshin.studyassistant.info.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.info.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
object InfoFeatureDIHolder : BaseFeatureDIHolder<InfoFeatureApi, InfoFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: InfoFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindInstance<() -> EditorFeatureStarter> { dependencies.editorFeatureStarter }
                bindSingleton<BaseScheduleRepository> { dependencies.baseScheduleRepository }
                bindSingleton<OrganizationsRepository> { dependencies.organizationsRepository }
                bindSingleton<UsersRepository> { dependencies.usersRepository }
                bindSingleton<CalendarSettingsRepository> { dependencies.calendarSettingsRepository }
                bindSingleton<DateManager> { dependencies.dateManager }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<InfoFeatureApi> {
                    object : InfoFeatureApi {
                        override fun fetchStarter() = instance<InfoFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): InfoFeatureApi {
        return fetchDI().instance<InfoFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Info feature DI is not initialized"
    }
}