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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.settings.api.SettingsConfig
import ru.aleshin.studyassistant.settings.api.SettingsOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store.TabNavigationComposeStore
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComposeStore

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
internal abstract class SettingsFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: SettingsConfig,
    protected val outputConsumer: OutputConsumer<SettingsOutput>,
) : FeatureComponent<SettingsConfig, SettingsOutput>(componentContext) {

    abstract val stack: Value<ChildStack<*, Child>>

    abstract val store: TabNavigationComposeStore

    sealed class Child {
        data class GeneralChild(val component: GeneralComponent) : Child()
        data class NotificationChild(val component: NotificationComponent) : Child()
        data class CalendarChild(val component: CalendarComponent) : Child()
        data class AiSettingsChild(val component: AiSettingsComponent) : Child()
        data class AboutAppChild(val component: AboutAppComponent) : Child()
    }

    class Default(
        startConfig: SettingsConfig,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<SettingsOutput>,
        private val tabNavigationStoreFactory: TabNavigationComposeStore.Factory,
        private val generalStoreFactory: GeneralComposeStore.Factory,
        private val notificationStoreFactory: NotificationComposeStore.Factory,
        private val calendarStoreFactory: CalendarComposeStore.Factory,
        private val aiSettingsStoreFactory: AiSettingsComposeStore.Factory,
        private val aboutAppStoreFactory: AboutAppComposeStore.Factory,
    ) : SettingsFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        override val store by saveableStore(
            storeFactory = tabNavigationStoreFactory,
            defaultState = TabNavigationState,
            stateSerializer = TabNavigationState.serializer(),
            storeKey = STORE_KEY,
            outputConsumer = rootOutputConsumer(),
        )

        private val stackNavigation = StackNavigation<SettingsConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = SettingsConfig.serializer(),
            initialStack = { listOf(startConfig) },
            key = STACK_KEY,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Settings_ROOT_STACK"
            const val STORE_KEY = "Settings_ROOT_STORE"
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(SettingsOutput.NavigateToBack)
            }
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
                SettingsConfig.Ai -> Child.AiSettingsChild(
                    component = AiSettingsComponent.Default(
                        storeFactory = aiSettingsStoreFactory,
                        componentContext = componentContext,
                    )
                )
                SettingsConfig.General -> Child.GeneralChild(
                    component = GeneralComponent.Default(
                        storeFactory = generalStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = generalOutputConsumer(),
                    )
                )
                SettingsConfig.Notification -> Child.NotificationChild(
                    component = NotificationComponent.Default(
                        storeFactory = notificationStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = notificationOutputConsumer(),
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
                is TabNavigationOutput.NavigateToAi -> {
                    stackNavigation.replaceCurrent(SettingsConfig.Ai)
                }
                is TabNavigationOutput.NavigateToGeneral -> {
                    stackNavigation.replaceCurrent(SettingsConfig.General)
                }
                is TabNavigationOutput.NavigateToNotification -> {
                    stackNavigation.replaceCurrent(SettingsConfig.Notification)
                }
                is TabNavigationOutput.NavigateToBack -> {
                    navigateToBack()
                }
            }
        }

        private fun notificationOutputConsumer() = OutputConsumer<NotificationOutput> {}

        private fun generalOutputConsumer() = OutputConsumer<GeneralOutput> { output ->
            when (output) {
                is GeneralOutput.NavigateToOnboarding -> {
                    outputConsumer.consume(SettingsOutput.NavigateToOnboarding)
                }
            }
        }
    }
}
