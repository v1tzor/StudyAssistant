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

package ru.aleshin.studyassistant.profile.impl.di.holder

import inject.BaseFeatureDIHolder
import managers.CoroutineManager
import org.kodein.di.DI
import org.kodein.di.DirectDI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import org.kodein.di.instance
import repositories.AuthRepository
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.profile.api.di.ProfileFeatureApi
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.impl.di.ProfileFeatureDependencies
import ru.aleshin.studyassistant.profile.impl.di.modules.domainModule
import ru.aleshin.studyassistant.profile.impl.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
object ProfileFeatureDIHolder : BaseFeatureDIHolder<ProfileFeatureApi, ProfileFeatureDependencies> {

    private var directDi: DirectDI? = null

    override fun init(dependencies: ProfileFeatureDependencies) {
        if (directDi == null) {
            val di = DI {
                importAll(presentationModule, domainModule)
                bindSingleton<() -> AuthFeatureStarter> { dependencies.authFeatureStarter }
                bindSingleton<AuthRepository> { dependencies.authRepository }
                bindSingleton<CoroutineManager> { dependencies.coroutineManager }
                bindSingleton<ProfileFeatureApi> {
                    object : ProfileFeatureApi {
                        override fun fetchStarter() = instance<ProfileFeatureStarter>()
                    }
                }
            }
            directDi = di.direct
        }
    }

    override fun fetchApi(): ProfileFeatureApi {
        return fetchDI().instance<ProfileFeatureApi>()
    }

    override fun clear() {
        directDi = null
    }

    internal fun fetchDI() = checkNotNull(directDi) {
        "Profile feature DI is not initialized"
    }
}