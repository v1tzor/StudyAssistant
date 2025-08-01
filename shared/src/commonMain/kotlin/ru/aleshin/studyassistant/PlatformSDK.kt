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

package ru.aleshin.studyassistant

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.direct
import ru.aleshin.studyassistant.core.common.di.MainDependenciesGraph
import ru.aleshin.studyassistant.core.common.di.coreCommonModule
import ru.aleshin.studyassistant.core.data.di.coreDataModule
import ru.aleshin.studyassistant.di.PlatformConfiguration
import ru.aleshin.studyassistant.di.modules.domainModule
import ru.aleshin.studyassistant.di.modules.featureModule
import ru.aleshin.studyassistant.di.modules.platformModule
import ru.aleshin.studyassistant.di.modules.presentationModule

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
object PlatformSDK {

    fun doInit(configuration: PlatformConfiguration) {
        configuration.appService.initializeApp()
        configuration.analyticsService.initializeService()
        configuration.crashlyticsService.initializeService()

        MainDependenciesGraph.initialize(
            di = DI {
                bindSingleton<PlatformConfiguration> { configuration }
                importAll(
                    platformModule,
                    coreCommonModule,
                    coreDataModule,
                    featureModule,
                    presentationModule,
                    domainModule,
                )
            }.direct
        )
    }
}