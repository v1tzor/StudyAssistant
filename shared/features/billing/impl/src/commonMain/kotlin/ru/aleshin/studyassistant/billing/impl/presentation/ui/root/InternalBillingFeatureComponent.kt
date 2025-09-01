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

package ru.aleshin.studyassistant.billing.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent
import ru.aleshin.studyassistant.billing.impl.di.holder.BillingFeatureManager
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionOutput
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.store.SubscriptionComponent
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.store.SubscriptionComposeStore
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
internal abstract class InternalBillingFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<BillingConfig>,
    outputConsumer: OutputConsumer<BillingOutput>,
) : BillingFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class SubscriptionChild(val component: SubscriptionComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<BillingConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<BillingOutput>,
        private val subscriptionStoreFactory: SubscriptionComposeStore.Factory,
    ) : InternalBillingFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = BillingContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<BillingConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = BillingConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(BillingConfig.Subscription) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Billing_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(BillingOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            BillingFeatureManager.finish()
        }

        private fun createChild(config: BillingConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is BillingConfig.Subscription -> Child.SubscriptionChild(
                    component = SubscriptionComponent.Default(
                        storeFactory = subscriptionStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = subscriptionOutputConsumer(),
                    )
                )
            }
        }

        private fun subscriptionOutputConsumer() = OutputConsumer<SubscriptionOutput> { output ->
            when (output) {
                is SubscriptionOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}