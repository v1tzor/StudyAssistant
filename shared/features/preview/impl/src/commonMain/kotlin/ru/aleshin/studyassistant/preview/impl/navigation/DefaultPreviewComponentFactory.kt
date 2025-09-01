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

package ru.aleshin.studyassistant.preview.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewOutput
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponentFactory
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.root.InternalPreviewFeatureComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultPreviewComponentFactory(
    private val introStoreFactory: IntroComposeStore.Factory,
    private val setupStoreFactory: SetupComposeStore.Factory,
) : PreviewFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<PreviewConfig>,
        outputConsumer: OutputConsumer<PreviewOutput>
    ): PreviewFeatureComponent {
        return InternalPreviewFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            introStoreFactory = introStoreFactory,
            setupStoreFactory = setupStoreFactory,
        )
    }
}