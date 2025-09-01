/*
 * Copyright 2025 Stanislav Aleshin
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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.SettingsContentProvider
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.contract.SubscriptionOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.store.SubscriptionComposeStore

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
internal abstract class InternalSettingsFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<SettingsConfig>,
    outputConsumer: OutputConsumer<SettingsOutput>,
) : SettingsFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    abstract val store: TabNavigationComposeStore

    sealed class Child {
        data class GeneralChild(val component: GeneralComponent) : Child()
        data class NotificationChild(val component: NotificationComponent) : Child()
        data class CalendarChild(val component: CalendarComponent) : Child()
        data class SubscriptionChild(val component: SubscriptionComponent) : Child()
        data class AboutAppChild(val component: AboutAppComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<SettingsConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<SettingsOutput>,
        private val tabNavigationStoreFactory: TabNavigationComposeStore.Factory,
        private val generalStoreFactory: GeneralComposeStore.Factory,
        private val notificationStoreFactory: NotificationComposeStore.Factory,
        private val calendarStoreFactory: CalendarComposeStore.Factory,
        private val subscriptionStoreFactory: SubscriptionComposeStore.Factory,
        private val aboutAppStoreFactory: AboutAppComposeStore.Factory,
    ) : InternalSettingsFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        override val contentProvider = SettingsContentProvider(this)

        override val store by saveableStore(
            storeFactory = tabNavigationStoreFactory,
            defaultState = TabNavigationState,
            stateSerializer = TabNavigationState.serializer(),
            storeKey = STORE_KEY,
            outputConsumer = rootOutputConsumer(),
        )

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<SettingsConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = SettingsConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(SettingsConfig.General) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Settings_ROOT_STACK"
            const val STORE_KEY = "Settings_ROOT_STORE"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(SettingsOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            SettingsFeatureManager.finish()
        }

        private fun createChild(config: SettingsConfig, componentContext: ComponentContext): Child {
            return when (config) {
                SettingsConfig.AboutApp -> Child.AboutAppChild(
                    component = AboutAppComponent.Default(
                        storeFactory = aboutAppStoreFactory,
                        componentContext = componentContext,
                    )
                )
                SettingsConfig.Calendar -> Child.CalendarChild(
                    component = CalendarComponent.Default(
                        storeFactory = calendarStoreFactory,
                        componentContext = componentContext,
                    )
                )
                SettingsConfig.General -> Child.GeneralChild(
                    component = GeneralComponent.Default(
                        storeFactory = generalStoreFactory,
                        componentContext = componentContext,
                    )
                )
                SettingsConfig.Notification -> Child.NotificationChild(
                    component = NotificationComponent.Default(
                        storeFactory = notificationStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = notificationOutputConsumer(),
                    )
                )
                SettingsConfig.Subscription -> Child.SubscriptionChild(
                    component = SubscriptionComponent.Default(
                        storeFactory = subscriptionStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = subscriptionOutputConsumer(),
                    )
                )
            }
        }

        private fun rootOutputConsumer() = OutputConsumer<TabNavigationOutput> { output ->
            when (output) {
                is TabNavigationOutput.NavigateToAboutApp -> {
                    stackNavigation.replaceCurrent(SettingsConfig.AboutApp)
                }
                is TabNavigationOutput.NavigateToCalendar -> {
                    stackNavigation.replaceCurrent(SettingsConfig.Calendar)
                }
                is TabNavigationOutput.NavigateToGeneral -> {
                    stackNavigation.replaceCurrent(SettingsConfig.General)
                }
                is TabNavigationOutput.NavigateToNotification -> {
                    stackNavigation.replaceCurrent(SettingsConfig.Notification)
                }
                is TabNavigationOutput.NavigateToSubscription -> {
                    stackNavigation.replaceCurrent(SettingsConfig.Subscription)
                }
                is TabNavigationOutput.NavigateToBack -> {
                    navigateToBack()
                }
            }
        }

        private fun notificationOutputConsumer() = OutputConsumer<NotificationOutput> { output ->
            when (output) {
                is NotificationOutput.NavigateToBilling -> {
                    outputConsumer.consume(SettingsOutput.NavigateToBilling)
                }
            }
        }

        private fun subscriptionOutputConsumer() = OutputConsumer<SubscriptionOutput> { output ->
            when (output) {
                is SubscriptionOutput.NavigateToBilling -> {
                    outputConsumer.consume(SettingsOutput.NavigateToBilling)
                }
            }
        }
    }
}