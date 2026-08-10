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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import ru.aleshin.studyassistant.analytics.api.AnalyticsConfig
import ru.aleshin.studyassistant.analytics.api.AnalyticsOutput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsInput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store.AnalyticsComponent
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store.AnalyticsComposeStore
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsOutput as ScreenOutput

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal abstract class AnalyticsFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: AnalyticsConfig,
    protected val outputConsumer: OutputConsumer<AnalyticsOutput>,
) : FeatureComponent<AnalyticsConfig, AnalyticsOutput>(componentContext) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class AnalyticsChild(val component: AnalyticsComponent) : Child()
    }

    class Default(
        startConfig: AnalyticsConfig,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<AnalyticsOutput>,
        private val storeFactory: AnalyticsComposeStore.Factory,
    ) : AnalyticsFeatureComponent(componentContext, startConfig, outputConsumer) {

        private val stackNavigation = StackNavigation<AnalyticsRoute>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = AnalyticsRoute.serializer(),
            initialStack = { listOf(AnalyticsRoute.Overview) },
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::createChild,
        )

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(AnalyticsOutput.NavigateToBack)
            }
        }

        private fun createChild(route: AnalyticsRoute, componentContext: ComponentContext): Child {
            val input = when (route) {
                is AnalyticsRoute.Overview -> AnalyticsInput()
                is AnalyticsRoute.Details -> AnalyticsInput(route.target, route.selection)
            }
            return Child.AnalyticsChild(
                component = AnalyticsComponent.Default(
                    storeFactory = storeFactory,
                    componentContext = componentContext,
                    inputData = input,
                    outputConsumer = screenOutputConsumer(),
                ),
            )
        }

        private fun screenOutputConsumer() = OutputConsumer<ScreenOutput> { output ->
            when (output) {
                is ScreenOutput.NavigateToBack -> navigateToBack()
                is ScreenOutput.NavigateToTarget -> stackNavigation.pushToFront(
                    AnalyticsRoute.Details(output.target, output.selection),
                )
            }
        }

        private companion object {
            const val STACK_KEY = "Analytics_ROOT_STACK"
        }
    }
}
