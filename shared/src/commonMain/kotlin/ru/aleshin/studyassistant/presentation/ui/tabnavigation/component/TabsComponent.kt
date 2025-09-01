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
import com.arkivanov.essenty.backhandler.BackCallback
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthConfig
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent.BillingConfig
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent
import ru.aleshin.studyassistant.chat.api.ChatFeatureComponent.ChatOutput
import ru.aleshin.studyassistant.chat.api.ChatFeatureStarter
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent.InfoOutput
import ru.aleshin.studyassistant.info.api.InfoFeatureStarter
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent.ProfileOutput
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent.ProfileOutput.NavigateToSettings
import ru.aleshin.studyassistant.profile.api.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent.ScheduleOutput
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponent.ScheduleOutput.NavigateToEditor
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureStarter
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent.SettingsConfig
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput.NavigateToEditor.Homework
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput.NavigateToEditor.Subject
import ru.aleshin.studyassistant.tasks.api.TasksFeatureComponent.TasksOutput.NavigateToEditor.Todo
import ru.aleshin.studyassistant.tasks.api.TasksFeatureStarter
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig

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
        data class ScheduleChild(val component: ScheduleFeatureComponent) : TabsChild()
        data class TasksChild(val component: TasksFeatureComponent) : TabsChild()
        data class ChatChild(val component: ChatFeatureComponent) : TabsChild()
        data class InfoChild(val component: InfoFeatureComponent) : TabsChild()
        data class ProfileChild(val component: ProfileFeatureComponent) : TabsChild()
    }

    @Serializable
    sealed class TabsConfig {

        @Serializable
        data object Schedule : TabsConfig()

        @Serializable
        data object Tasks : TabsConfig()

        @Serializable
        data object Chat : TabsConfig()

        @Serializable
        data object Info : TabsConfig()

        @Serializable
        data object Profile : TabsConfig()
    }

    sealed class TabsOutput : BaseOutput {
        data class NavigateToEditor(val config: EditorConfig) : TabsOutput()
        data class NavigateToBilling(val config: BillingConfig) : TabsOutput()
        data class NavigateToUsers(val config: UsersConfig) : TabsOutput()
        data class NavigateToSettings(val config: SettingsConfig) : TabsOutput()
        data class NavigateToAuth(val config: AuthConfig) : TabsOutput()
        data class NavigateToSharedSchedule(val receivedShareId: UID) : TabsOutput()
        data object NavigateToBack : TabsOutput()
    }

    class Default(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<TabsConfig>,
        private val outputConsumer: OutputConsumer<TabsOutput>,
        private val scheduleFeatureStarter: ScheduleFeatureStarter,
        private val tasksFeatureStarter: TasksFeatureStarter,
        private val chatFeatureStarter: ChatFeatureStarter,
        private val infoFeatureStarter: InfoFeatureStarter,
        private val profileFeatureStarter: ProfileFeatureStarter,
    ) : TabsComponent(
        componentContext = componentContext,
    ) {

        private val stackNavigation = StackNavigation<TabsConfig>()

        override val stack = childStack(
            source = stackNavigation,
            serializer = TabsConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(TabsConfig.Schedule) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::childFactory
        )

        private val backCallback = BackCallback {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(TabsOutput.NavigateToBack)
            }
        }

        private companion object {
            const val STACK_KEY = "TABS_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun clickScheduleTab() {
            stackNavigation.bringToFront(TabsConfig.Schedule)
        }

        override fun clickTasksTab() {
            stackNavigation.bringToFront(TabsConfig.Tasks)
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
                is TabsConfig.Schedule -> TabsChild.ScheduleChild(
                    component = scheduleFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = StartFeatureConfig(null),
                        outputConsumer = scheduleOutputConsumer()
                    )
                )
                is TabsConfig.Tasks -> TabsChild.TasksChild(
                    component = tasksFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = StartFeatureConfig(null),
                        outputConsumer = tasksOutputConsumer()
                    )
                )
                is TabsConfig.Chat -> TabsChild.ChatChild(
                    component = chatFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = StartFeatureConfig(null),
                        outputConsumer = chatOutputConsumer()
                    )
                )
                is TabsConfig.Info -> TabsChild.InfoChild(
                    component = infoFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = StartFeatureConfig(null),
                        outputConsumer = infoOutputConsumer()
                    )
                )
                is TabsConfig.Profile -> TabsChild.ProfileChild(
                    component = profileFeatureStarter.createOrGetFeature().componentFactory().createComponent(
                        componentContext = componentContext,
                        startConfig = StartFeatureConfig(null),
                        outputConsumer = profileOutputConsumer()
                    )
                )
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
                is ScheduleOutput.NavigateToUserProfile -> {
                    val config = UsersConfig.UserProfile(userId = output.userId)
                    outputConsumer.consume(TabsOutput.NavigateToUsers(config))
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
                is TasksOutput.NavigateToBilling -> {
                    outputConsumer.consume(TabsOutput.NavigateToBilling(BillingConfig.Subscription))
                }
                is TasksOutput.NavigateToUserProfile -> {
                    val config = UsersConfig.UserProfile(userId = output.userId)
                    outputConsumer.consume(TabsOutput.NavigateToUsers(config))
                }
                is TasksOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }

        private fun chatOutputConsumer() = OutputConsumer<ChatOutput> { output ->
            when (output) {
                is ChatOutput.NavigateToBilling -> {
                    outputConsumer.consume(TabsOutput.NavigateToBilling(BillingConfig.Subscription))
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
                is InfoOutput.NavigateToBilling -> {
                    outputConsumer.consume(TabsOutput.NavigateToBilling(BillingConfig.Subscription))
                }
                is InfoOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }

        private fun profileOutputConsumer() = OutputConsumer<ProfileOutput> { output ->
            when (output) {
                is ProfileOutput.NavigateToAuth -> {
                    val config = AuthConfig.Login
                    outputConsumer.consume(TabsOutput.NavigateToAuth(config))
                }
                is ProfileOutput.NavigateToFriends -> {
                    val config = UsersConfig.Friends
                    outputConsumer.consume(TabsOutput.NavigateToUsers(config))
                }
                is ProfileOutput.NavigateToProfileEditor -> {
                    val config = EditorConfig.Profile
                    outputConsumer.consume(TabsOutput.NavigateToEditor(config))
                }
                is NavigateToSettings -> {
                    val config = when (output) {
                        NavigateToSettings.AboutApp -> SettingsConfig.AboutApp
                        NavigateToSettings.Calendar -> SettingsConfig.Calendar
                        NavigateToSettings.General -> SettingsConfig.General
                        NavigateToSettings.Notification -> SettingsConfig.Notification
                        NavigateToSettings.Subscription -> SettingsConfig.Subscription
                    }
                    outputConsumer.consume(TabsOutput.NavigateToSettings(config))
                }
                is ProfileOutput.NavigateToSharedSchedule -> {
                    val outputData = TabsOutput.NavigateToSharedSchedule(output.receivedShareId)
                    outputConsumer.consume(outputData)
                }
                is ProfileOutput.NavigateToBack -> {
                    stackNavigation.pop()
                }
            }
        }
    }
}