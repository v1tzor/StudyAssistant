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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureManager
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroOutput
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupOutput
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComposeStore

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class InternalPreviewFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<PreviewConfig>,
    outputConsumer: OutputConsumer<PreviewOutput>,
) : PreviewFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class IntroChild(val component: IntroComponent) : Child()
        data class SetupChild(val component: SetupComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<PreviewConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<PreviewOutput>,
        private val introStoreFactory: IntroComposeStore.Factory,
        private val setupStoreFactory: SetupComposeStore.Factory,
    ) : InternalPreviewFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = PreviewContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<PreviewConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = PreviewConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(PreviewConfig.Intro) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Preview_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(PreviewOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            PreviewFeatureManager.finish()
        }

        private fun createChild(config: PreviewConfig, componentContext: ComponentContext): Child {
            return when (config) {
                PreviewConfig.Intro -> Child.IntroChild(
                    component = IntroComponent.Default(
                        storeFactory = introStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = introOutputConsumer(),
                    ),
                )
                PreviewConfig.Setup -> Child.SetupChild(
                    component = SetupComponent.Default(
                        storeFactory = setupStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = setupOutputConsumer(),
                    ),
                )
            }
        }

        private fun introOutputConsumer() = OutputConsumer<IntroOutput> { output ->
            when (output) {
                is IntroOutput.NavigateToLogin -> {
                    outputConsumer.consume(PreviewOutput.NavigateToLogin)
                }
                is IntroOutput.NavigateToRegister -> {
                    outputConsumer.consume(PreviewOutput.NavigateToRegister)
                }
            }
        }

        private fun setupOutputConsumer() = OutputConsumer<SetupOutput> { output ->
            when (output) {
                is SetupOutput.NavigateToApp -> {
                    outputConsumer.consume(PreviewOutput.NavigateToApp)
                }
                is SetupOutput.NavigateToBilling -> {
                    outputConsumer.consume(PreviewOutput.NavigateToBilling)
                }
                is SetupOutput.NavigateToWeekScheduleEditor -> {
                    outputConsumer.consume(PreviewOutput.NavigateToWeekScheduleEditor)
                }
                is SetupOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}