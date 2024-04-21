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

package ru.aleshin.studyassistant.auth.impl.di.holder

import inject.BaseFeatureDIHolder
import managers.CoroutineManager
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindInstance
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.auth.api.di.AuthFeatureApi
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.impl.di.AuthFeatureDependencies
import ru.aleshin.studyassistant.auth.impl.di.modules.domainModule
import ru.aleshin.studyassistant.auth.impl.di.modules.navigationModule
import ru.aleshin.studyassistant.auth.impl.di.modules.presentationModule
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
object AuthFeatureDIHolder : BaseFeatureDIHolder<AuthFeatureApi, AuthFeatureDependencies> {

    private var directDI: DirectDI? = null

    override fun init(dependencies: AuthFeatureDependencies) {
        if (directDI == null) {
            val di = DI {
                importAll(navigationModule, presentationModule, domainModule)
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindInstance<() -> NavigationFeatureStarter> { dependencies.navigationFeatureStarter }
                bindInstance<() -> PreviewFeatureStarter> { dependencies.previewFeatureStarter }
                bindSingleton<AuthFeatureApi> {
                    object : AuthFeatureApi {
                        override fun fetchStarter() = instance<AuthFeatureStarter>()
                    }
                }
            }
            directDI = di.direct
        }
    }

    override fun fetchApi(): AuthFeatureApi {
        return fetchDI().instance<AuthFeatureApi>()
    }

    override fun clear() {
        directDI = null
    }

    internal fun fetchDI() = checkNotNull(directDI) {
        "Auth feature DI is not initialized"
    }
}