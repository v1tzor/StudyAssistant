/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.application

import ReviewServiceImpl
import ru.aleshin.studyassistant.PlatformSDK
import ru.aleshin.studyassistant.core.common.platform.BaseApplication
import ru.aleshin.studyassistant.data.AnalyticsServiceImpl
import ru.aleshin.studyassistant.data.CrashlyticsServiceImpl
import ru.aleshin.studyassistant.di.PlatformConfiguration
import ru.ok.tracer.HasTracerConfiguration

/**
 * @author Stanislav Aleshin on 14.04.2025.
 */
abstract class BaseFlavorApplication : BaseApplication(), HasTracerConfiguration {

    override val tracerConfiguration = CrashlyticsServiceImpl.tracerConfiguration

    override fun initPlatformServices() {
        PlatformSDK.doInit(
            configuration = PlatformConfiguration(
                analyticsService = AnalyticsServiceImpl(
                    context = applicationContext
                ),
                reviewService = ReviewServiceImpl(),
                crashlyticsService = CrashlyticsServiceImpl(),
                applicationContext = applicationContext,
            )
        )
    }
}
