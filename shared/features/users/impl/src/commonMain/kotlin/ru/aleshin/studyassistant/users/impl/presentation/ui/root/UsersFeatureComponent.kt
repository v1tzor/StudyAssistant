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

package ru.aleshin.studyassistant.users.impl.presentation.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.users.api.UsersConfig
import ru.aleshin.studyassistant.users.api.UsersOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileInput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComposeStore

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class UsersFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: UsersConfig,
    protected val outputConsumer: OutputConsumer<UsersOutput>,
) : FeatureComponent<UsersConfig, UsersOutput>(componentContext) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class EmployeeProfileChild(val component: EmployeeProfileComponent) : Child()
    }

    class Default(
        startConfig: UsersConfig,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<UsersOutput>,
        private val employeeProfileStoreFactory: EmployeeProfileComposeStore.Factory,
    ) : UsersFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        private val stackNavigation = StackNavigation<UsersConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = UsersConfig.serializer(),
            initialStack = { listOf(startConfig) },
            key = STACK_KEY,
            handleBackButton = true,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Users_ROOT_STACK"
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(UsersOutput.NavigateToBack)
            }
        }

        private fun createChild(config: UsersConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is UsersConfig.EmployeeProfile -> Child.EmployeeProfileChild(
                    component = EmployeeProfileComponent.Default(
                        storeFactory = employeeProfileStoreFactory,
                        componentContext = componentContext,
                        inputData = EmployeeProfileInput(config.employeeId),
                        outputConsumer = employeeProfileOutputConsumer(),
                    )
                )
            }
        }

        private fun employeeProfileOutputConsumer() =
            OutputConsumer<EmployeeProfileOutput> { output ->
                when (output) {
                    is EmployeeProfileOutput.NavigateToEmployeeEditor -> {
                        val outputData = UsersOutput.NavigateToEmployeeEditor(
                            employeeId = output.config.employeeId,
                            organizationId = output.config.organizationId,
                        )
                        outputConsumer.consume(outputData)
                    }

                    is EmployeeProfileOutput.NavigateToBack -> navigateToBack()
                }
            }
    }
}
