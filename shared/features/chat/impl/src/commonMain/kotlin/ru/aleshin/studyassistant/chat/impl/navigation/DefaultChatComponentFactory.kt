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

package ru.aleshin.studyassistant.chat.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent.ChatConfig
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent.ChatOutput
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponentFactory
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store.AssistantComposeStore
import ru.aleshin.studyassistant.chat.impl.presentation.ui.root.InternalChatFeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultChatComponentFactory(
    private val assistantStoreFactory: AssistantComposeStore.Factory,
) : ChatFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<ChatConfig>,
        outputConsumer: OutputConsumer<ChatOutput>
    ): ChatFeatureComponent {
        return InternalChatFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            assistantStoreFactory = assistantStoreFactory,
        )
    }
}