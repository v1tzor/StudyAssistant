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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.chat.api.ChatConfig
import ru.aleshin.studyassistant.chat.api.ChatOutput
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantOutput
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantState
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore

/**
 * @author Stanislav Aleshin on 25.08.2025
 */
internal abstract class ChatFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: ChatConfig,
    protected val outputConsumer: OutputConsumer<ChatOutput>,
) : FeatureComponent<ChatConfig, ChatOutput>(componentContext) {

    abstract val store: AssistantComposeStore

    class Default(
        storeFactory: AssistantComposeStore.Factory,
        componentContext: ComponentContext,
        startConfig: ChatConfig,
        outputConsumer: OutputConsumer<ChatOutput>,
    ) : ChatFeatureComponent(componentContext, startConfig, outputConsumer) {

        private companion object Companion {
            const val COMPONENT_KEY = "CHAT_ASSISTANT"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = AssistantState(),
            stateSerializer = AssistantState.serializer(),
            outputConsumer = assistantOutputConsumer(),
            storeKey = COMPONENT_KEY,
        )

        private fun assistantOutputConsumer() = OutputConsumer<AssistantOutput> { output ->
            when (output) {
                AssistantOutput.NavigateToAiSettings -> outputConsumer.consume(ChatOutput.NavigateToAiSettings)
            }
        }

        override fun navigateToBack() {
            outputConsumer.consume(ChatOutput.NavigateToBack)
        }
    }
}