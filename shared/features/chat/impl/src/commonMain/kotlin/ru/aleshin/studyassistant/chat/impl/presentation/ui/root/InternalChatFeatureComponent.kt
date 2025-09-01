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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureManager
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.contract.AssistantOutput
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store.AssistantComponent
import ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.store.AssistantComposeStore
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class InternalChatFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<ChatConfig>,
    outputConsumer: OutputConsumer<ChatOutput>,
) : ChatFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class AssistantChild(val component: AssistantComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<ChatConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<ChatOutput>,
        private val assistantStoreFactory: AssistantComposeStore.Factory,
    ) : InternalChatFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = ChatContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<ChatConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = ChatConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(ChatConfig.Assistant) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Chat_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(ChatOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            ChatFeatureManager.finish()
        }

        private fun createChild(config: ChatConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is ChatConfig.Assistant -> Child.AssistantChild(
                    component = AssistantComponent.Default(
                        storeFactory = assistantStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = assistantOutputConsumer(),
                    )
                )
            }
        }

        private fun assistantOutputConsumer() = OutputConsumer<AssistantOutput> { output ->
            when (output) {
                is AssistantOutput.NavigateToBilling -> {
                    outputConsumer.consume(ChatOutput.NavigateToBilling)
                }
            }
        }
    }
}