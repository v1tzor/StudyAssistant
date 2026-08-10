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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.tasks.api.TasksConfig
import ru.aleshin.studyassistant.tasks.api.TasksOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksInput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store.HomeworksComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.OverviewComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.store.OverviewComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareInput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store.ShareComposeStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoComposeStore

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class TasksFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: TasksConfig,
    protected val outputConsumer: OutputConsumer<TasksOutput>,
) : FeatureComponent<TasksConfig, TasksOutput>(componentContext) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class OverviewChild(val component: OverviewComponent) : Child()
        data class HomeworksChild(val component: HomeworksComponent) : Child()
        data class TodoChild(val component: TodoComponent) : Child()
        data class ShareChild(val component: ShareComponent) : Child()
    }

    class Default(
        startConfig: TasksConfig,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<TasksOutput>,
        private val overviewStoreFactory: OverviewComposeStore.Factory,
        private val todoStoreFactory: TodoComposeStore.Factory,
        private val homeworksStoreFactory: HomeworksComposeStore.Factory,
        private val shareStoreFactory: ShareComposeStore.Factory,
    ) : TasksFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        private val stackNavigation = StackNavigation<TasksConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = TasksConfig.serializer(),
            initialStack = { listOf(startConfig) },
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Tasks_ROOT_STACK"
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(TasksOutput.NavigateToBack)
            }
        }

        private fun createChild(config: TasksConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is TasksConfig.Overview -> Child.OverviewChild(
                    component = OverviewComponent.Default(
                        storeFactory = overviewStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = overviewOutputConsumer(),
                    )
                )

                is TasksConfig.Homeworks -> Child.HomeworksChild(
                    component = HomeworksComponent.Default(
                        storeFactory = homeworksStoreFactory,
                        componentContext = componentContext,
                        inputData = HomeworksInput(config.targetDate),
                        outputConsumer = homeworksOutputConsumer(),
                    )
                )

                is TasksConfig.Todos -> Child.TodoChild(
                    component = TodoComponent.Default(
                        storeFactory = todoStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = todoOutputConsumer(),
                    )
                )

                is TasksConfig.Share -> Child.ShareChild(
                    component = ShareComponent.Default(
                        storeFactory = shareStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = shareOutputConsumer(),
                        inputData = ShareInput(config.code),
                    )
                )
            }
        }

        private fun overviewOutputConsumer() = OutputConsumer<OverviewOutput> {
            when (it) {
                is OverviewOutput.NavigateToHomeworks -> {
                    stackNavigation.pushToFront(TasksConfig.Homeworks(it.targetDate))
                }

                is OverviewOutput.NavigateToTodo -> {
                    stackNavigation.pushToFront(TasksConfig.Todos)
                }

                is OverviewOutput.NavigateToShareHomeworks -> {
                    stackNavigation.pushToFront(TasksConfig.Share())
                }

                is OverviewOutput.NavigateToAnalytics -> {
                    outputConsumer.consume(TasksOutput.NavigateToAnalytics)
                }

                is OverviewOutput.NavigateToHomeworkEditor -> {
                    val outputData = TasksOutput.NavigateToEditor.Homework(
                        homeworkId = it.config.homeworkId,
                        date = it.config.date,
                        subjectId = it.config.subjectId,
                        organizationId = it.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }

                is OverviewOutput.NavigateToTodoEditor -> {
                    val outputData = TasksOutput.NavigateToEditor.Todo(
                        todoId = it.config.todoId,
                    )
                    outputConsumer.consume(outputData)
                }
            }
        }

        private fun homeworksOutputConsumer() = OutputConsumer<HomeworksOutput> {
            when (it) {
                is HomeworksOutput.NavigateToHomeworkEditor -> {
                    val outputData = TasksOutput.NavigateToEditor.Homework(
                        homeworkId = it.config.homeworkId,
                        date = it.config.date,
                        subjectId = it.config.subjectId,
                        organizationId = it.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }

                is HomeworksOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun todoOutputConsumer() = OutputConsumer<TodoOutput> {
            when (it) {
                is TodoOutput.NavigateToTodoEditor -> {
                    val outputData = TasksOutput.NavigateToEditor.Todo(
                        todoId = it.config.todoId,
                    )
                    outputConsumer.consume(outputData)
                }

                is TodoOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun shareOutputConsumer() = OutputConsumer<ShareOutput> {
            when (it) {
                is ShareOutput.NavigateToSubjectEditor -> {
                    val outputData = TasksOutput.NavigateToEditor.Subject(
                        subjectId = it.config.subjectId,
                        organizationId = it.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }

                is ShareOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}
