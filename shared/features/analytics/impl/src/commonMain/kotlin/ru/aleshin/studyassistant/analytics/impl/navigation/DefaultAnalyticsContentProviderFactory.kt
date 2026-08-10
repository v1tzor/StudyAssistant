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

package ru.aleshin.studyassistant.analytics.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.analytics.api.AnalyticsConfig
import ru.aleshin.studyassistant.analytics.api.AnalyticsContentProviderFactory
import ru.aleshin.studyassistant.analytics.api.AnalyticsOutput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.root.AnalyticsContentProvider
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.root.AnalyticsFeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal class DefaultAnalyticsContentProviderFactory(
    private val di: DI,
) : AnalyticsContentProviderFactory {

    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: AnalyticsConfig,
        outputConsumer: OutputConsumer<AnalyticsOutput>,
    ): FeatureContentProvider {
        val deps = AnalyticsComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<AnalyticsComponentDeps, AnalyticsFeatureComponent>(
            arg = deps,
        )
        return AnalyticsContentProvider(di, component)
    }
}

internal typealias AnalyticsComponentDeps = FeatureComponentDeps<AnalyticsConfig, AnalyticsOutput>
