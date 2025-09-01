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

package ru.aleshin.studyassistant.presentation.ui.main.store

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthConfig
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthOutput
import ru.aleshin.studyassistant.auth.api.AuthFeatureStarter
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent.BillingConfig
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent.BillingOutput
import ru.aleshin.studyassistant.billing.api.BillingFeatureStarter
import ru.aleshin.studyassistant.core.common.architecture.component.BaseComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorOutput
import ru.aleshin.studyassistant.editor.api.EditorFeatureStarter
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainInput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainOutput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainState
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.AuthChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.BillingChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.EditorChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.PreviewChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.ScheduleChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.SettingsChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.TabNavigationChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.UsersChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Auth
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Billing
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Editor
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Preview
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Schedule
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Settings
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Splash
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.TabNavigation
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Users
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsConfig
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponent.TabsOutput
import ru.aleshin.studyassistant.presentation.ui.tabnavigation.component.TabsComponentFactory
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewOutput
import ru.aleshin.studyassistant.preview.api.PreviewFeatureStarter
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent.ScheduleConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent.ScheduleOutput
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent.ScheduleOutput.NavigateToEditor
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent.SettingsConfig
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent.SettingsOutput
import ru.aleshin.studyassistant.settings.api.SettingsFeatureStarter
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersOutput
import ru.aleshin.studyassistant.users.api.UsersFeatureStarter

/**
 * @author Stanislav Aleshin on 21.08.2025.
 */
abstract class MainComponent(
    componentContext: ComponentContext,
) : BaseComponent(
    componentContext = componentContext,
) {

    abstract val store: MainComposeStore

    abstract val stack: Value<ChildStack<*, Child>>

    abstract fun navigateToBack()

    @Serializable
    sealed class Config {
        @Serializable
        data object Splash : Config()

        @Serializable
        data class TabNavigation(
            val startConfig: StartFeatureConfig<TabsConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Preview(
            val startConfig: StartFeatureConfig<PreviewConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Auth(
            val startConfig: StartFeatureConfig<AuthConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Schedule(
            val startConfig: StartFeatureConfig<ScheduleConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Billing(
            val startConfig: StartFeatureConfig<BillingConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Editor(
            val startConfig: StartFeatureConfig<EditorConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Users(
            val startConfig: StartFeatureConfig<UsersConfig> = StartFeatureConfig(null)
        ) : Config()

        @Serializable
        data class Settings(
            val startConfig: StartFeatureConfig<SettingsConfig> = StartFeatureConfig(null)
        ) : Config()
    }

    sealed class Child {
        object SplashChild : Child()
        data class PreviewChild(val component: PreviewFeatureComponent) : Child()
        data class AuthChild(val component: AuthFeatureComponent) : Child()
        data class ScheduleChild(val component: ScheduleFeatureComponent) : Child()
        data class EditorChild(val component: EditorFeatureComponent) : Child()
        data class BillingChild(val component: BillingFeatureComponent) : Child()
        data class SettingsChild(val component: SettingsFeatureComponent) : Child()
        data class UsersChild(val component: UsersFeatureComponent) : Child()
        data class TabNavigationChild(val component: TabsComponent) : Child()
    }

    class Default(
        storeFactory: MainComposeStore.Factory,
        componentContext: ComponentContext,
        deepLink: DeepLinkUrl?,
        private val previewFeatureStarter: PreviewFeatureStarter,
        private val authFeatureStarter: AuthFeatureStarter,
        private val scheduleFeatureStarter: ScheduleFeatureStarter,
        private val editorFeatureStarter: EditorFeatureStarter,
        private val billingFeatureStarter: BillingFeatureStarter,
        private val settingsFeatureStarter: SettingsFeatureStarter,
        private val usersFeatureStarter: UsersFeatureStarter,
        private val tabsComponentFactory: TabsComponentFactory,
    ) : MainComponent(componentContext) {

        private companion object {
            const val STORE_KEY = "MAIN_COMPONENT_KEY"
            const val STACK_KEY = "MAIN_STACK_KEY"
        }

        private val stackNavigation = StackNavigation<Config>()

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = MainState(),
            input = MainInput(deepLink),
            outputConsumer = mainOutputConsumer(),
            stateSerializer = MainState.serializer(),
            storeKey = STORE_KEY,
        )

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = Config.serializer(),
            initialConfiguration = Splash,
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::createChild,
        )

        override fun navigateToBack() {
            stackNavigation.pop()
        }

        private fun createChild(config: Config, componentContext: ComponentContext): Child {
            return when (config) {
                is Splash -> Child.SplashChild

                is Preview -> PreviewChild(
                    component = previewFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = config.startConfig,
                        outputConsumer = previewOutputConsumer(),
                    )
                )

                is Auth -> AuthChild(
                    component = authFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = config.startConfig,
                        outputConsumer = authOutputConsumer(),
                    )
                )

                is Editor -> EditorChild(
                    component = editorFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = editorOutputConsumer(),
                        componentContext = componentContext,
                    )
                )

                is Schedule -> ScheduleChild(
                    component = scheduleFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = scheduleOutputConsumer(),
                        componentContext = componentContext,
                    )
                )

                is Billing -> BillingChild(
                    component = billingFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = billingOutputConsumer(),
                        componentContext = componentContext,
                    )
                )

                is Settings -> SettingsChild(
                    component = settingsFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = settingsOutputConsumer(),
                        componentContext = componentContext,
                    )
                )

                is Users -> UsersChild(
                    component = usersFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = usersOutputConsumer(),
                        componentContext = componentContext,
                    )
                )

                is TabNavigation -> TabNavigationChild(
                    component = tabsComponentFactory.createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = tabNavOutputConsumer(),
                        componentContext = componentContext,
                    )
                )
            }
        }

        private fun mainOutputConsumer() = OutputConsumer<MainOutput> { output ->
            when (output) {
                is MainOutput.NavigateToApp -> {
                    stackNavigation.replaceAll(TabNavigation())
                }
                is MainOutput.NavigateToAuth -> {
                    val config = StartFeatureConfig<AuthConfig>(listOf(AuthConfig.Login))
                    stackNavigation.replaceAll(Auth(config))
                }
                is MainOutput.NavigateToIntro -> {
                    val config = StartFeatureConfig<PreviewConfig>(listOf(PreviewConfig.Intro))
                    stackNavigation.replaceAll(Preview(config))
                }
                is MainOutput.NavigateToSetup -> {
                    val config = StartFeatureConfig<PreviewConfig>(listOf(PreviewConfig.Setup))
                    stackNavigation.replaceAll(Preview(config))
                }
                is MainOutput.NavigateToVerification -> {
                    val config = StartFeatureConfig<AuthConfig>(listOf(AuthConfig.Verification))
                    stackNavigation.replaceAll(Auth(config))
                }
            }
        }

        private fun authOutputConsumer() = OutputConsumer<AuthOutput> { output ->
            when (output) {
                is AuthOutput.NavigateToApp -> {
                    stackNavigation.replaceAll(TabNavigation())
                }
                is AuthOutput.NavigateToFirstSetup -> {
                    val config = StartFeatureConfig<PreviewConfig>(listOf(PreviewConfig.Setup))
                    stackNavigation.replaceAll(Preview(config))
                }
                is AuthOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun previewOutputConsumer() = OutputConsumer<PreviewOutput> { output ->
            when (output) {
                is PreviewOutput.NavigateToApp -> {
                    stackNavigation.replaceAll(TabNavigation())
                }
                is PreviewOutput.NavigateToBilling -> {
                    stackNavigation.pushToFront(Billing())
                }
                is PreviewOutput.NavigateToLogin -> {
                    val config = StartFeatureConfig<AuthConfig>(listOf(AuthConfig.Login))
                    stackNavigation.replaceAll(Auth(config))
                }
                is PreviewOutput.NavigateToRegister -> {
                    val config = StartFeatureConfig<AuthConfig>(listOf(AuthConfig.Register))
                    stackNavigation.replaceAll(Auth(config))
                }
                is PreviewOutput.NavigateToWeekScheduleEditor -> {
                    val screenConfig = EditorConfig.WeekSchedule()
                    val startConfig = StartFeatureConfig<EditorConfig>(listOf(screenConfig))
                    stackNavigation.pushToFront(Editor(startConfig))
                }
                is PreviewOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun editorOutputConsumer() = OutputConsumer<EditorOutput> { output ->
            when (output) {
                is EditorOutput.NavigateToBilling -> {
                    stackNavigation.pushToFront(Billing())
                }
                is EditorOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun scheduleOutputConsumer() = OutputConsumer<ScheduleOutput> { output ->
            when (output) {
                is NavigateToEditor -> {
                    val screenConfig = when (output) {
                        is NavigateToEditor.DailySchedule -> EditorConfig.DailySchedule(
                            date = output.date,
                            customScheduleId = output.customScheduleId,
                            baseScheduleId = output.baseScheduleId,
                        )
                        is NavigateToEditor.Homework -> EditorConfig.Homework(
                            homeworkId = output.homeworkId,
                            date = output.date,
                            subjectId = output.subjectId,
                            organizationId = output.organizationId,
                        )
                        is NavigateToEditor.WeekSchedule -> EditorConfig.WeekSchedule(
                            week = output.week,
                        )
                    }
                    val startConfig = StartFeatureConfig<EditorConfig>(listOf(screenConfig))
                    stackNavigation.pushToFront(Editor(startConfig))
                }
                is ScheduleOutput.NavigateToUserProfile -> {
                    val screenConfig = UsersConfig.UserProfile(output.userId)
                    val startConfig = StartFeatureConfig<UsersConfig>(listOf(screenConfig))
                    stackNavigation.pushToFront(Users(startConfig))
                }
                is ScheduleOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun billingOutputConsumer() = OutputConsumer<BillingOutput> { output ->
            when (output) {
                is BillingOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun settingsOutputConsumer() = OutputConsumer<SettingsOutput> { output ->
            when (output) {
                is SettingsOutput.NavigateToBilling -> {
                    stackNavigation.pushToFront(Billing())
                }
                is SettingsOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun usersOutputConsumer() = OutputConsumer<UsersOutput> { output ->
            when (output) {
                is UsersOutput.NavigateToEmployeeEditor -> {
                    val screenConfig = EditorConfig.Employee(
                        employeeId = output.employeeId,
                        organizationId = output.organizationId,
                    )
                    val startConfig = StartFeatureConfig<EditorConfig>(listOf(screenConfig))
                    stackNavigation.pushToFront(Editor(startConfig))
                }
                is UsersOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun tabNavOutputConsumer() = OutputConsumer<TabsOutput> { output ->
            when (output) {
                is TabsOutput.NavigateToAuth -> {
                    val config = StartFeatureConfig<AuthConfig>(listOf(AuthConfig.Login))
                    stackNavigation.replaceAll(Auth(config))
                }
                is TabsOutput.NavigateToEditor -> {
                    val config = StartFeatureConfig<EditorConfig>(listOf(output.config))
                    stackNavigation.pushToFront(Editor(config))
                }
                is TabsOutput.NavigateToSettings -> {
                    val config = StartFeatureConfig<SettingsConfig>(listOf(output.config))
                    stackNavigation.pushToFront(Settings(config))
                }
                is TabsOutput.NavigateToSharedSchedule -> {
                    val screenConfig = ScheduleConfig.Share(
                        receivedShareId = output.receivedShareId,
                    )
                    val startConfig = StartFeatureConfig<ScheduleConfig>(listOf(screenConfig))
                    stackNavigation.pushToFront(Schedule(startConfig))
                }
                is TabsOutput.NavigateToUsers -> {
                    val config = StartFeatureConfig<UsersConfig>(listOf(output.config))
                    stackNavigation.pushToFront(Users(config))
                }
                is TabsOutput.NavigateToBilling -> {
                    stackNavigation.pushToFront(Billing())
                }
                is TabsOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}