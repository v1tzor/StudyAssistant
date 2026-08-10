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

package ru.aleshin.studyassistant.schedule.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.schedule.api.ScheduleConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleContentProviderFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.root.ScheduleContentProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.root.ScheduleFeatureComponent

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
internal class DefaultScheduleContentProviderFactory(
    private val di: DI,
) : ScheduleContentProviderFactory {

    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: ScheduleConfig,
        outputConsumer: OutputConsumer<ScheduleOutput>,
    ): FeatureContentProvider {
        val deps = ScheduleComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<ScheduleComponentDeps, ScheduleFeatureComponent>(arg = deps)
        return ScheduleContentProvider(di, component)
    }
}

internal typealias ScheduleComponentDeps = FeatureComponentDeps<ScheduleConfig, ScheduleOutput>
