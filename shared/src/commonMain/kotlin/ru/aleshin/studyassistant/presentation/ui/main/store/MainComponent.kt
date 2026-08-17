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
import ru.aleshin.studyassistant.analytics.api.AnalyticsConfig
import ru.aleshin.studyassistant.analytics.api.AnalyticsDecomposeFeatureFactory
import ru.aleshin.studyassistant.analytics.api.AnalyticsOutput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.core.common.navigation.WidgetDeepLinkDestination
import ru.aleshin.studyassistant.editor.api.EditorConfig
import ru.aleshin.studyassistant.editor.api.EditorDecomposeFeatureFactory
import ru.aleshin.studyassistant.editor.api.EditorOutput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainInput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainOutput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainState
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.AnalyticsChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.EditorChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.PreviewChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.ScheduleChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.SettingsChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.TabNavigationChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Child.UsersChild
import ru.aleshin.studyassistant.presentation.ui.main.store.MainComponent.Config.Analytics
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
import ru.aleshin.studyassistant.preview.api.PreviewConfig
import ru.aleshin.studyassistant.preview.api.PreviewDecomposeFeatureFactory
import ru.aleshin.studyassistant.preview.api.PreviewOutput
import ru.aleshin.studyassistant.schedule.api.ScheduleConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleDecomposeFeatureFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput.NavigateToEditor
import ru.aleshin.studyassistant.settings.api.SettingsConfig
import ru.aleshin.studyassistant.settings.api.SettingsDecomposeFeatureFactory
import ru.aleshin.studyassistant.settings.api.SettingsOutput
import ru.aleshin.studyassistant.tasks.api.TasksConfig
import ru.aleshin.studyassistant.users.api.UsersConfig
import ru.aleshin.studyassistant.users.api.UsersDecomposeFeatureFactory
import ru.aleshin.studyassistant.users.api.UsersOutput

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
    abstract fun handleDeepLink(deepLinkUrl: DeepLinkUrl)

    @Serializable
    sealed class Config {
        @Serializable
        data object Splash : Config()

        @Serializable
        data class Preview(val startConfig: PreviewConfig = PreviewConfig.Intro) : Config()

        @Serializable
        data class TabNavigation(val startConfig: TabsConfig = TabsConfig.Schedule()) : Config()

        @Serializable
        data class Schedule(val startConfig: ScheduleConfig = ScheduleConfig.Overview) : Config()

        @Serializable
        data class Editor(val startConfig: EditorConfig) : Config()

        @Serializable
        data class Users(val startConfig: UsersConfig) : Config()

        @Serializable
        data class Settings(val startConfig: SettingsConfig = SettingsConfig.General) : Config()

        @Serializable
        data class Analytics(val startConfig: AnalyticsConfig = AnalyticsConfig.Overview) : Config()
    }

    sealed class Child {
        object SplashChild : Child()
        data class PreviewChild(val contentProvider: FeatureContentProvider) : Child()
        data class ScheduleChild(val contentProvider: FeatureContentProvider) : Child()
        data class EditorChild(val contentProvider: FeatureContentProvider) : Child()
        data class SettingsChild(val contentProvider: FeatureContentProvider) : Child()
        data class AnalyticsChild(val contentProvider: FeatureContentProvider) : Child()
        data class UsersChild(val contentProvider: FeatureContentProvider) : Child()
        data class TabNavigationChild(val component: TabsComponent) : Child()
    }

    class Default(
        storeFactory: MainComposeStore.Factory,
        componentContext: ComponentContext,
        deepLink: DeepLinkUrl?,
        private val previewFeatureFactory: PreviewDecomposeFeatureFactory,
        private val scheduleFeatureFactory: ScheduleDecomposeFeatureFactory,
        private val editorFeatureFactory: EditorDecomposeFeatureFactory,
        private val settingsFeatureFactory: SettingsDecomposeFeatureFactory,
        private val analyticsFeatureFactory: AnalyticsDecomposeFeatureFactory,
        private val usersFeatureFactory: UsersDecomposeFeatureFactory,
        private val tabsComponentFactory: TabsComponentFactory,
    ) : MainComponent(componentContext) {

        private companion object {
            const val STORE_KEY = "MAIN_COMPONENT_KEY"
            const val STACK_KEY = "LOCAL_FIRST_MAIN_STACK_KEY"
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

        override fun handleDeepLink(deepLinkUrl: DeepLinkUrl) {
            stackNavigation.replaceAll(*resolveDeepLink(deepLinkUrl).toTypedArray())
        }

        private fun resolveDeepLink(deepLinkUrl: DeepLinkUrl): List<Config> {
            val widgetDestination = WidgetDeepLinkDestination.fromDeepLinkUrl(deepLinkUrl)
            val widgetConfigs = when (widgetDestination) {
                WidgetDeepLinkDestination.Schedule -> listOf(
                    TabNavigation(TabsConfig.Schedule(ScheduleConfig.Overview)),
                )
                WidgetDeepLinkDestination.Homeworks -> listOf(
                    TabNavigation(TabsConfig.Tasks(TasksConfig.Homeworks())),
                )
                WidgetDeepLinkDestination.Todos -> listOf(
                    TabNavigation(TabsConfig.Tasks(TasksConfig.Todos)),
                )
                WidgetDeepLinkDestination.Goals -> listOf(
                    TabNavigation(TabsConfig.Tasks(TasksConfig.Overview)),
                )
                is WidgetDeepLinkDestination.ScheduleEditor -> listOf(
                    TabNavigation(TabsConfig.Schedule(ScheduleConfig.Overview)),
                    Editor(
                        EditorConfig.DailySchedule(
                            date = widgetDestination.date,
                            customScheduleId = widgetDestination.customScheduleId,
                            baseScheduleId = widgetDestination.baseScheduleId,
                        ),
                    ),
                )
                is WidgetDeepLinkDestination.HomeworkEditor -> listOf(
                    TabNavigation(TabsConfig.Tasks(TasksConfig.Homeworks())),
                    Editor(
                        EditorConfig.Homework(
                            homeworkId = widgetDestination.homeworkId,
                            date = widgetDestination.date,
                            subjectId = widgetDestination.subjectId,
                            organizationId = widgetDestination.organizationId,
                        ),
                    ),
                )
                is WidgetDeepLinkDestination.TodoEditor -> listOf(
                    TabNavigation(TabsConfig.Tasks(TasksConfig.Todos)),
                    Editor(EditorConfig.Todo(widgetDestination.todoId)),
                )
                null -> null
            }
            if (widgetConfigs != null) return widgetConfigs

            val code = deepLinkUrl.params["code"]
            val destination = deepLinkUrl.pathSegments.takeIf {
                it.firstOrNull() == "share" && !code.isNullOrBlank()
            }?.getOrNull(1)
            val tabConfig = when (destination) {
                "schedule" -> TabsConfig.Schedule(ScheduleConfig.Share(code))
                "homework" -> TabsConfig.Tasks(TasksConfig.Share(code))
                else -> TabsConfig.Schedule()
            }
            return listOf(TabNavigation(tabConfig))
        }

        private fun createChild(config: Config, componentContext: ComponentContext): Child {
            return when (config) {
                is Splash -> Child.SplashChild
                is Preview -> {
                    val api = previewFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = previewOutputConsumer(),
                        componentContext = componentContext,
                    )
                    PreviewChild(contentProvider = provider)
                }
                is Editor -> {
                    val api = editorFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = editorOutputConsumer(),
                        componentContext = componentContext,
                    )
                    EditorChild(contentProvider = provider)
                }
                is Schedule -> {
                    val api = scheduleFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = scheduleOutputConsumer(),
                        componentContext = componentContext,
                    )
                    ScheduleChild(contentProvider = provider)
                }
                is Settings -> {
                    val api = settingsFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = settingsOutputConsumer(),
                        componentContext = componentContext,
                    )
                    SettingsChild(contentProvider = provider)
                }
                is Analytics -> {
                    val api = analyticsFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = analyticsOutputConsumer(),
                        componentContext = componentContext,
                    )
                    AnalyticsChild(contentProvider = provider)
                }
                is Users -> {
                    val api = usersFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig,
                        outputConsumer = usersOutputConsumer(),
                        componentContext = componentContext,
                    )
                    UsersChild(contentProvider = provider)
                }
                is TabNavigation -> {
                    val component = tabsComponentFactory.createComponent(
                        startConfig = config.startConfig,
                        outputConsumer = tabNavOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabNavigationChild(component = component)
                }
            }
        }

        private fun mainOutputConsumer() = OutputConsumer<MainOutput> { output ->
            when (output) {
                is MainOutput.NavigateToApp -> {
                    stackNavigation.replaceAll(TabNavigation())
                }
                is MainOutput.NavigateToPreview -> {
                    stackNavigation.replaceAll(Preview(output.startConfig))
                }
                is MainOutput.NavigateToDeepLink -> {
                    handleDeepLink(output.deepLinkUrl)
                }
            }
        }

        private fun previewOutputConsumer() = OutputConsumer<PreviewOutput> { output ->
            when (output) {
                is PreviewOutput.NavigateToApp -> {
                    stackNavigation.replaceAll(TabNavigation())
                }
                is PreviewOutput.NavigateToWeekScheduleEditor -> {
                    stackNavigation.replaceAll(TabNavigation(), Editor(EditorConfig.WeekSchedule()))
                }
                is PreviewOutput.NavigateToScheduleImport -> {
                    stackNavigation.replaceAll(
                        TabNavigation(),
                        Schedule(ScheduleConfig.Import),
                    )
                }
            }
        }

        private fun editorOutputConsumer() = OutputConsumer<EditorOutput> { output ->
            when (output) {
                is EditorOutput.NavigateToBack -> navigateToBack()
                is EditorOutput.NavigateToImport -> {
                    val config = Schedule(ScheduleConfig.Import)
                    stackNavigation.pushToFront(config)
                }
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
                        is NavigateToEditor.Organization -> EditorConfig.Organization(
                            organizationId = output.organizationId,
                        )
                    }
                    stackNavigation.pushToFront(Editor(screenConfig))
                }
                is ScheduleOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun settingsOutputConsumer() = OutputConsumer<SettingsOutput> { output ->
            when (output) {
                is SettingsOutput.NavigateToBack -> navigateToBack()
                is SettingsOutput.NavigateToOnboarding -> {
                    stackNavigation.replaceAll(Preview(PreviewConfig.Setup))
                }
            }
        }

        private fun analyticsOutputConsumer() = OutputConsumer<AnalyticsOutput> { output ->
            when (output) {
                is AnalyticsOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun usersOutputConsumer() = OutputConsumer<UsersOutput> { output ->
            when (output) {
                is UsersOutput.NavigateToEmployeeEditor -> {
                    val screenConfig = EditorConfig.Employee(
                        employeeId = output.employeeId,
                        organizationId = output.organizationId,
                    )
                    stackNavigation.pushToFront(Editor(screenConfig))
                }
                is UsersOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun tabNavOutputConsumer() = OutputConsumer<TabsOutput> { output ->
            when (output) {
                is TabsOutput.NavigateToEditor -> {
                    stackNavigation.pushToFront(Editor(output.config))
                }
                is TabsOutput.NavigateToSettings -> {
                    stackNavigation.pushToFront(Settings(output.config))
                }
                is TabsOutput.NavigateToAnalytics -> {
                    stackNavigation.pushToFront(Analytics())
                }
                is TabsOutput.NavigateToScheduleSharing -> {
                    stackNavigation.pushToFront(Schedule(ScheduleConfig.Share()))
                }
                is TabsOutput.NavigateToUsers -> {
                    if (output.config is UsersConfig.EmployeeProfile) {
                        stackNavigation.pushToFront(Users(output.config))
                    }
                }
                is TabsOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}
