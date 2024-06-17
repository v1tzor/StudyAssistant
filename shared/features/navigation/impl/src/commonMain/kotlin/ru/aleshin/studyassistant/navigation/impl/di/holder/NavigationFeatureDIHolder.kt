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

package ru.aleshin.studyassistant.navigation.impl.di.holder

import inject.BaseFeatureDIHolder
import managers.CoroutineManager
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.navigation.api.di.NavigationFeatureApi
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.navigation.impl.di.NavigationFeatureDependencies
import ru.aleshin.studyassistant.navigation.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
object NavigationFeatureDIHolder : BaseFeatureDIHolder<NavigationFeatureApi, NavigationFeatureDependencies> {

    private var directDI: DirectDI? = null

    override fun init(dependencies: NavigationFeatureDependencies) {
        if (directDI == null) {
            val di = DI {
                importAll(presentationModule)
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<() -> ScheduleFeatureStarter> { dependencies.scheduleFeatureStarter }
                bindSingleton<() -> InfoFeatureStarter> { dependencies.infoFeatureStarter }
                bindSingleton<() -> ProfileFeatureStarter> { dependencies.profileFeatureStarter }
                bindSingleton<NavigationFeatureApi> {
                    object : NavigationFeatureApi {
                        override fun fetchStarter() = instance<NavigationFeatureStarter>()
                    }
                }
            }
            directDI = di.direct
        }
    }

    override fun fetchApi(): NavigationFeatureApi {
        return fetchDI().instance<NavigationFeatureApi>()
    }

    override fun clear() {
        directDI = null
    }

    internal fun fetchDI() = checkNotNull(directDI) {
        "Navigation feature DI is not initialized"
    }
}