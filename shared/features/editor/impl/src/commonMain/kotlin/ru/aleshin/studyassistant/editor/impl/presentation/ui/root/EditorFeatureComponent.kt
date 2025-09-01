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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureManager
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.store.DailyScheduleComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.store.DailyScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.store.OrganizationComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store.WeekScheduleComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectComposeStore
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract.TodoOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.store.TodoComposeStore

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
internal abstract class InternalEditorFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<EditorConfig>,
    outputConsumer: OutputConsumer<EditorOutput>,
) : EditorFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class EmployeeChild(val component: EmployeeComponent) : Child()
        data class WeekScheduleChild(val component: WeekScheduleComponent) : Child()
        data class DailyScheduleChild(val component: DailyScheduleComponent) : Child()
        data class ClassChild(val component: ClassComponent) : Child()
        data class SubjectChild(val component: SubjectComponent) : Child()
        data class HomeworkChild(val component: HomeworkComponent) : Child()
        data class TodoChild(val component: TodoComponent) : Child()
        data class OrganizationChild(val component: OrganizationComponent) : Child()
        data class ProfileChild(val component: ProfileComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<EditorConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<EditorOutput>,
        private val classStoreFactory: ClassComposeStore.Factory,
        private val employeeStoreFactory: EmployeeComposeStore.Factory,
        private val weekScheduleStoreFactory: WeekScheduleComposeStore.Factory,
        private val dailyScheduleStoreFactory: DailyScheduleComposeStore.Factory,
        private val subjectStoreFactory: SubjectComposeStore.Factory,
        private val homeworkStoreFactory: HomeworkComposeStore.Factory,
        private val todoStoreFactory: TodoComposeStore.Factory,
        private val organizationStoreFactory: OrganizationComposeStore.Factory,
        private val profileStoreFactory: ProfileComposeStore.Factory
    ) : InternalEditorFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = EditorContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<EditorConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = EditorConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf() },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Editor_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(EditorOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            EditorFeatureManager.finish()
        }

        private fun createChild(config: EditorConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is EditorConfig.Class -> Child.ClassChild(
                    component = ClassComponent.Default(
                        storeFactory = classStoreFactory,
                        componentContext = componentContext,
                        inputData = ClassInput(
                            classId = config.classId,
                            scheduleId = config.scheduleId,
                            organizationId = config.organizationId,
                            customSchedule = config.isCustomSchedule,
                            weekDay = config.weekDay,
                        ),
                        outputConsumer = classOutputConsumer(),
                    )
                )
                is EditorConfig.DailySchedule -> Child.DailyScheduleChild(
                    component = DailyScheduleComponent.Default(
                        storeFactory = dailyScheduleStoreFactory,
                        componentContext = componentContext,
                        inputData = DailyScheduleInput(
                            date = config.date,
                            baseScheduleId = config.baseScheduleId,
                            customScheduleId = config.customScheduleId,
                        ),
                        outputConsumer = dailyScheduleOutputConsumer(),
                    )
                )
                is EditorConfig.Employee -> Child.EmployeeChild(
                    component = EmployeeComponent.Default(
                        storeFactory = employeeStoreFactory,
                        componentContext = componentContext,
                        inputData = EmployeeInput(
                            employeeId = config.employeeId,
                            organizationId = config.organizationId,
                        ),
                        outputConsumer = employeeOutputConsumer(),
                    )
                )
                is EditorConfig.Homework -> Child.HomeworkChild(
                    component = HomeworkComponent.Default(
                        storeFactory = homeworkStoreFactory,
                        componentContext = componentContext,
                        inputData = HomeworkInput(
                            homeworkId = config.homeworkId,
                            date = config.date,
                            subjectId = config.subjectId,
                            organizationId = config.organizationId,
                        ),
                        outputConsumer = homeworkOutputConsumer(),
                    )
                )
                is EditorConfig.Organization -> Child.OrganizationChild(
                    component = OrganizationComponent.Default(
                        storeFactory = organizationStoreFactory,
                        componentContext = componentContext,
                        inputData = OrganizationInput(config.organizationId),
                        outputConsumer = organizationOutputConsumer(),
                    )
                )
                is EditorConfig.Profile -> Child.ProfileChild(
                    component = ProfileComponent.Default(
                        storeFactory = profileStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = profileOutputConsumer(),
                    )
                )
                is EditorConfig.Subject -> Child.SubjectChild(
                    component = SubjectComponent.Default(
                        storeFactory = subjectStoreFactory,
                        componentContext = componentContext,
                        inputData = SubjectInput(
                            subjectId = config.subjectId,
                            organizationId = config.organizationId,
                        ),
                        outputConsumer = subjectOutputConsumer(),
                    )
                )
                is EditorConfig.Todo -> Child.TodoChild(
                    component = TodoComponent.Default(
                        storeFactory = todoStoreFactory,
                        componentContext = componentContext,
                        inputData = TodoInput(config.todoId),
                        outputConsumer = todoOutputConsumer(),
                    )
                )
                is EditorConfig.WeekSchedule -> Child.WeekScheduleChild(
                    component = WeekScheduleComponent.Default(
                        storeFactory = weekScheduleStoreFactory,
                        componentContext = componentContext,
                        inputData = WeekScheduleInput(config.week),
                        outputConsumer = weekScheduleOutputConsumer(),
                    )
                )
            }
        }

        private fun classOutputConsumer() = OutputConsumer<ClassOutput> { output ->
            when (output) {
                is ClassOutput.NavigateToEmployeeEditor -> {
                    val outputData = EditorConfig.Employee(
                        employeeId = output.config.employeeId,
                        organizationId = output.config.organizationId,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is ClassOutput.NavigateToOrganizationEditor -> {
                    val outputData = EditorConfig.Organization(output.config.organizationId)
                    stackNavigation.pushToFront(outputData)
                }
                is ClassOutput.NavigateToSubjectEditor -> {
                    val outputData = EditorConfig.Subject(
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is ClassOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun dailyScheduleOutputConsumer() = OutputConsumer<DailyScheduleOutput> { output ->
            when (output) {
                is DailyScheduleOutput.NavigateToClassEditor -> {
                    val outputData = EditorConfig.Class(
                        classId = output.config.classId,
                        scheduleId = output.config.scheduleId,
                        organizationId = output.config.organizationId,
                        isCustomSchedule = output.config.isCustomSchedule,
                        weekDay = output.config.weekDay,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is DailyScheduleOutput.NavigateToOrganizationEditor -> {
                    val outputData = EditorConfig.Organization(output.config.organizationId)
                    stackNavigation.pushToFront(outputData)
                }
                is DailyScheduleOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun employeeOutputConsumer() = OutputConsumer<EmployeeOutput> { output ->
            when (output) {
                is EmployeeOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun homeworkOutputConsumer() = OutputConsumer<HomeworkOutput> { output ->
            when (output) {
                is HomeworkOutput.NavigateToOrganizationEditor -> {
                    val outputData = EditorConfig.Organization(output.config.organizationId)
                    stackNavigation.pushToFront(outputData)
                }
                is HomeworkOutput.NavigateToSubjectEditor -> {
                    val outputData = EditorConfig.Subject(
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is HomeworkOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun organizationOutputConsumer() = OutputConsumer<OrganizationOutput> { output ->
            when (output) {
                is OrganizationOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun profileOutputConsumer() = OutputConsumer<ProfileOutput> { output ->
            when (output) {
                is ProfileOutput.NavigateToBilling -> {
                    outputConsumer.consume(EditorOutput.NavigateToBilling)
                }
                is ProfileOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun subjectOutputConsumer() = OutputConsumer<SubjectOutput> { output ->
            when (output) {
                is SubjectOutput.NavigateToEmployeeEditor -> {
                    val outputData = EditorConfig.Employee(
                        employeeId = output.config.employeeId,
                        organizationId = output.config.organizationId,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is SubjectOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun todoOutputConsumer() = OutputConsumer<TodoOutput> { output ->
            when (output) {
                is TodoOutput.NavigateToBilling -> {
                    outputConsumer.consume(EditorOutput.NavigateToBilling)
                }
                is TodoOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun weekScheduleOutputConsumer() = OutputConsumer<WeekScheduleOutput> { output ->
            when (output) {
                is WeekScheduleOutput.NavigateToClassEditor -> {
                    val outputData = EditorConfig.Class(
                        classId = output.config.classId,
                        scheduleId = output.config.scheduleId,
                        organizationId = output.config.organizationId,
                        isCustomSchedule = output.config.isCustomSchedule,
                        weekDay = output.config.weekDay,
                    )
                    stackNavigation.pushToFront(outputData)
                }
                is WeekScheduleOutput.NavigateToOrganizationEditor -> {
                    val outputData = EditorConfig.Organization(output.config.organizationId)
                    stackNavigation.pushToFront(outputData)
                }
                is WeekScheduleOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}