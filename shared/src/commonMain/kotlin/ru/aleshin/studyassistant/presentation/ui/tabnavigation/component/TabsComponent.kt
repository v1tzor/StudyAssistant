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

package ru.aleshin.studyassistant.presentation.ui.tabnavigation.component

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.chat.api.ChatConfig
import ru.aleshin.studyassistant.chat.api.ChatDecomposeFeatureFactory
import ru.aleshin.studyassistant.chat.api.ChatOutput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.editor.api.EditorConfig
import ru.aleshin.studyassistant.info.api.InfoConfig
import ru.aleshin.studyassistant.info.api.InfoDecomposeFeatureFactory
import ru.aleshin.studyassistant.info.api.InfoOutput
import ru.aleshin.studyassistant.profile.api.ProfileConfig
import ru.aleshin.studyassistant.profile.api.ProfileDecomposeFeatureFactory
import ru.aleshin.studyassistant.profile.api.ProfileOutput
import ru.aleshin.studyassistant.profile.api.ProfileOutput.NavigateToSettings
import ru.aleshin.studyassistant.schedule.api.ScheduleConfig
import ru.aleshin.studyassistant.schedule.api.ScheduleDecomposeFeatureFactory
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput
import ru.aleshin.studyassistant.schedule.api.ScheduleOutput.NavigateToEditor
import ru.aleshin.studyassistant.settings.api.SettingsConfig
import ru.aleshin.studyassistant.tasks.api.TasksConfig
import ru.aleshin.studyassistant.tasks.api.TasksDecomposeFeatureFactory
import ru.aleshin.studyassistant.tasks.api.TasksOutput
import ru.aleshin.studyassistant.tasks.api.TasksOutput.NavigateToEditor.Homework
import ru.aleshin.studyassistant.tasks.api.TasksOutput.NavigateToEditor.Subject
import ru.aleshin.studyassistant.tasks.api.TasksOutput.NavigateToEditor.Todo
import ru.aleshin.studyassistant.users.api.UsersConfig

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
abstract class TabsComponent(
    componentContext: ComponentContext
) : ChildComponent(
    componentContext = componentContext
) {

    abstract val stack: Value<ChildStack<*, TabsChild>>

    abstract fun clickScheduleTab()
    abstract fun clickTasksTab()
    abstract fun clickChatTab()
    abstract fun clickInfoTab()
    abstract fun clickProfileTab()

    sealed class TabsChild {
        data class ScheduleChild(val contentProvider: FeatureContentProvider) : TabsChild()
        data class TasksChild(val contentProvider: FeatureContentProvider) : TabsChild()
        data class ChatChild(val contentProvider: FeatureContentProvider) : TabsChild()
        data class InfoChild(val contentProvider: FeatureContentProvider) : TabsChild()
        data class ProfileChild(val contentProvider: FeatureContentProvider) : TabsChild()
    }

    @Serializable
    sealed class TabsConfig {

        @Serializable
        data class Schedule(val startConfig: ScheduleConfig? = null) : TabsConfig()

        @Serializable
        data class Tasks(val startConfig: TasksConfig? = null) : TabsConfig()

        @Serializable
        data object Chat : TabsConfig()

        @Serializable
        data object Info : TabsConfig()

        @Serializable
        data object Profile : TabsConfig()
    }

    sealed class TabsOutput : BaseOutput {
        data class NavigateToEditor(val config: EditorConfig) : TabsOutput()
        data class NavigateToUsers(val config: UsersConfig) : TabsOutput()
        data class NavigateToSettings(val config: SettingsConfig) : TabsOutput()
        data object NavigateToAnalytics : TabsOutput()
        data object NavigateToScheduleSharing : TabsOutput()
        data object NavigateToBack : TabsOutput()
    }

    class Default(
        componentContext: ComponentContext,
        startConfig: TabsConfig,
        private val outputConsumer: OutputConsumer<TabsOutput>,
        private val scheduleFeatureFactory: ScheduleDecomposeFeatureFactory,
        private val tasksFeatureFactory: TasksDecomposeFeatureFactory,
        private val chatFeatureFactory: ChatDecomposeFeatureFactory,
        private val infoFeatureFactory: InfoDecomposeFeatureFactory,
        private val profileFeatureFactory: ProfileDecomposeFeatureFactory,
    ) : TabsComponent(
        componentContext = componentContext,
    ) {

        private val stackNavigation = StackNavigation<TabsConfig>()

        override val stack = childStack(
            source = stackNavigation,
            serializer = TabsConfig.serializer(),
            initialConfiguration = startConfig,
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::childFactory
        )

        private companion object {
            const val STACK_KEY = "TABS_STACK"
        }

        override fun clickScheduleTab() {
            stackNavigation.bringToFront(TabsConfig.Schedule())
        }

        override fun clickTasksTab() {
            stackNavigation.bringToFront(TabsConfig.Tasks())
        }

        override fun clickChatTab() {
            stackNavigation.bringToFront(TabsConfig.Chat)
        }

        override fun clickInfoTab() {
            stackNavigation.bringToFront(TabsConfig.Info)
        }

        override fun clickProfileTab() {
            stackNavigation.bringToFront(TabsConfig.Profile)
        }

        private fun childFactory(config: TabsConfig, componentContext: ComponentContext): TabsChild {
            return when (config) {
                is TabsConfig.Schedule -> {
                    val api = scheduleFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig ?: ScheduleConfig.Overview,
                        outputConsumer = scheduleOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabsChild.ScheduleChild(contentProvider = provider)
                }
                is TabsConfig.Tasks -> {
                    val api = tasksFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = config.startConfig ?: TasksConfig.Overview,
                        outputConsumer = tasksOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabsChild.TasksChild(contentProvider = provider)
                }
                is TabsConfig.Chat -> {
                    val api = chatFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = ChatConfig.Assistant,
                        outputConsumer = chatOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabsChild.ChatChild(contentProvider = provider)
                }
                is TabsConfig.Info -> {
                    val api = infoFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = InfoConfig.Organizations,
                        outputConsumer = infoOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabsChild.InfoChild(contentProvider = provider)
                }
                is TabsConfig.Profile -> {
                    val api = profileFeatureFactory.createOrGetFeature(componentContext)
                    val provider = api.contentProviderFactory().createProvider(
                        startConfig = ProfileConfig.Profile,
                        outputConsumer = profileOutputConsumer(),
                        componentContext = componentContext,
                    )
                    TabsChild.ProfileChild(contentProvider = provider)
                }
            }
        }

        private fun scheduleOutputConsumer() = OutputConsumer<ScheduleOutput> { output ->
            when (output) {
                is NavigateToEditor -> {
                    val config = when (output) {
                        is NavigateToEditor.Homework -> EditorConfig.Homework(
                            homeworkId = output.homeworkId,
                            date = output.date,
                            subjectId = output.subjectId,
                            organizationId = output.organizationId,
                        )
                        is NavigateToEditor.DailySchedule -> EditorConfig.DailySchedule(
                            date = output.date,
                            customScheduleId = output.customScheduleId,
                            baseScheduleId = output.baseScheduleId,
                        )
                        is NavigateToEditor.WeekSchedule -> EditorConfig.WeekSchedule(
                            week = output.week,
                        )
                    }
                    outputConsumer.consume(TabsOutput.NavigateToEditor(config))
                }
                is ScheduleOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }

        private fun tasksOutputConsumer() = OutputConsumer<TasksOutput> { output ->
            when (output) {
                is TasksOutput.NavigateToEditor -> {
                    val config = when (output) {
                        is Homework -> EditorConfig.Homework(
                            homeworkId = output.homeworkId,
                            date = output.date,
                            subjectId = output.subjectId,
                            organizationId = output.organizationId,
                        )
                        is Subject -> EditorConfig.Subject(
                            subjectId = output.subjectId,
                            organizationId = output.organizationId,
                        )
                        is Todo -> EditorConfig.Todo(
                            todoId = output.todoId,
                        )
                    }
                    outputConsumer.consume(TabsOutput.NavigateToEditor(config))
                }
                is TasksOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
                is TasksOutput.NavigateToAnalytics -> {
                    outputConsumer.consume(TabsOutput.NavigateToAnalytics)
                }
            }
        }

        private fun chatOutputConsumer() = OutputConsumer<ChatOutput> { output ->
            when (output) {
                is ChatOutput.NavigateToAiSettings -> {
                    outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.Ai))
                }
                is ChatOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }

        private fun infoOutputConsumer() = OutputConsumer<InfoOutput> { output ->
            when (output) {
                is InfoOutput.NavigateToEditor -> {
                    val config = when (output) {
                        is InfoOutput.NavigateToEditor.Organization -> EditorConfig.Organization(
                            organizationId = output.organizationId,
                        )
                        is InfoOutput.NavigateToEditor.Employee -> EditorConfig.Employee(
                            employeeId = output.employeeId,
                            organizationId = output.organizationId,
                        )
                        is InfoOutput.NavigateToEditor.Subject -> EditorConfig.Subject(
                            subjectId = output.subjectId,
                            organizationId = output.organizationId,
                        )
                    }
                    outputConsumer.consume(TabsOutput.NavigateToEditor(config))
                }
                is InfoOutput.NavigateToEmployeeProfile -> {
                    val config = UsersConfig.EmployeeProfile(employeeId = output.employeeId)
                    outputConsumer.consume(TabsOutput.NavigateToUsers(config))
                }
                is InfoOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }

        private fun profileOutputConsumer() = OutputConsumer<ProfileOutput> { output ->
            when (output) {
                is ProfileOutput.NavigateToProfileEditor -> {
                    val config = EditorConfig.Profile
                    outputConsumer.consume(TabsOutput.NavigateToEditor(config))
                }
                is ProfileOutput.NavigateToScheduleSharing -> {
                    outputConsumer.consume(TabsOutput.NavigateToScheduleSharing)
                }
                is NavigateToSettings -> {
                    when (output) {
                        NavigateToSettings.AboutApp -> {
                            outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.AboutApp))
                        }
                        NavigateToSettings.Calendar -> {
                            outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.Calendar))
                        }
                        NavigateToSettings.General -> {
                            outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.General))
                        }
                        NavigateToSettings.Notification -> {
                            outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.Notification))
                        }
                        NavigateToSettings.Ai -> {
                            outputConsumer.consume(TabsOutput.NavigateToSettings(SettingsConfig.Ai))
                        }
                    }
                }
                is ProfileOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }
    }
}
