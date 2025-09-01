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

package ru.aleshin.studyassistant.info.impl.presentation.ui.root

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
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent
import ru.aleshin.studyassistant.info.impl.di.holder.InfoFeatureManager
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeInput
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsInput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsOutput
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComposeStore

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class InternalInfoFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<InfoConfig>,
    outputConsumer: OutputConsumer<InfoOutput>,
) : InfoFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class OrganizationsChild(val component: OrganizationsComponent) : Child()
        data class EmployeeChild(val component: EmployeeComponent) : Child()
        data class SubjectsChild(val component: SubjectsComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<InfoConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<InfoOutput>,
        private val organizationsStoreFactory: OrganizationsComposeStore.Factory,
        private val employeeStoreFactory: EmployeeComposeStore.Factory,
        private val subjectsStoreFactory: SubjectsComposeStore.Factory,
    ) : InternalInfoFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = InfoContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<InfoConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = InfoConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(InfoConfig.Organizations) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Info_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(InfoOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            InfoFeatureManager.finish()
        }

        private fun createChild(config: InfoConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is InfoConfig.Organizations -> Child.OrganizationsChild(
                    component = OrganizationsComponent.Default(
                        storeFactory = organizationsStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = organizationsOutputConsumer(),
                    )
                )
                is InfoConfig.Employee -> Child.EmployeeChild(
                    component = EmployeeComponent.Default(
                        storeFactory = employeeStoreFactory,
                        componentContext = componentContext,
                        inputData = EmployeeInput(config.organizationId),
                        outputConsumer = employeeOutputConsumer(),
                    )
                )
                is InfoConfig.Subjects -> Child.SubjectsChild(
                    component = SubjectsComponent.Default(
                        storeFactory = subjectsStoreFactory,
                        componentContext = componentContext,
                        inputData = SubjectsInput(config.organizationId),
                        outputConsumer = subjectsOutputConsumer(),
                    )
                )
            }
        }

        private fun organizationsOutputConsumer() = OutputConsumer<OrganizationsOutput> { output ->
            when (output) {
                is OrganizationsOutput.NavigateToSubjects -> {
                    stackNavigation.pushToFront(InfoConfig.Subjects(output.config.organizationId))
                }
                is OrganizationsOutput.NavigateToEmployees -> {
                    stackNavigation.pushToFront(InfoConfig.Employee(output.config.organizationId))
                }
                is OrganizationsOutput.NavigateToBilling -> {
                    outputConsumer.consume(InfoOutput.NavigateToBilling)
                }
                is OrganizationsOutput.NavigateToEmployeeProfile -> {
                    val outputData = InfoOutput.NavigateToEmployeeProfile(output.config.employeeId)
                    outputConsumer.consume(outputData)
                }
                is OrganizationsOutput.NavigateToOrganizationEditor -> {
                    val outputData = InfoOutput.NavigateToEditor.Organization(
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
                is OrganizationsOutput.NavigateToSubjectEditor -> {
                    val outputData = InfoOutput.NavigateToEditor.Subject(
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
            }
        }

        private fun employeeOutputConsumer() = OutputConsumer<EmployeeOutput> { output ->
            when (output) {
                is EmployeeOutput.NavigateToEmployeeEditor -> {
                    val outputData = InfoOutput.NavigateToEditor.Employee(
                        employeeId = output.config.employeeId,
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
                is EmployeeOutput.NavigateToEmployeeProfile -> {
                    val outputData = InfoOutput.NavigateToEmployeeProfile(output.config.employeeId)
                    outputConsumer.consume(outputData)
                }
                is EmployeeOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun subjectsOutputConsumer() = OutputConsumer<SubjectsOutput> { output ->
            when (output) {
                is SubjectsOutput.NavigateToSubjectEditor -> {
                    val outputData = InfoOutput.NavigateToEditor.Subject(
                        subjectId = output.config.subjectId,
                        organizationId = output.config.organizationId,
                    )
                    outputConsumer.consume(outputData)
                }
                is SubjectsOutput.NavigateToBack -> navigateToBack()
            }
        }
    }
}