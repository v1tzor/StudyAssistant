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

package ru.aleshin.studyassistant.info.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.info.api.InfoConfig
import ru.aleshin.studyassistant.info.api.InfoContentProviderFactory
import ru.aleshin.studyassistant.info.api.InfoOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.root.InfoContentProvider
import ru.aleshin.studyassistant.info.impl.presentation.ui.root.InfoFeatureComponent

/**
 * @author Stanislav Aleshin on 05.07.2026.
 */
internal class DefaultInfoContentProviderFactory(
    private val di: DI,
) : InfoContentProviderFactory {

    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: InfoConfig,
        outputConsumer: OutputConsumer<InfoOutput>,
    ): FeatureContentProvider {
        val deps = InfoComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<InfoComponentDeps, InfoFeatureComponent>(arg = deps)

        return InfoContentProvider(di, component)
    }
}

internal typealias InfoComponentDeps = FeatureComponentDeps<InfoConfig, InfoOutput>
