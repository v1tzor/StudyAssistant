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

package ru.aleshin.studyassistant.users.impl.presentation.ui.root

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
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent
import ru.aleshin.studyassistant.users.impl.di.holder.UsersFeatureManager
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileInput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract.FriendsOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store.FriendsComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.store.FriendsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract.RequestsOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store.RequestsComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.store.RequestsComposeStore
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileInput
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract.UserProfileOutput
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.store.UserProfileComposeStore

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
internal abstract class InternalUsersFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<UsersConfig>,
    outputConsumer: OutputConsumer<UsersOutput>,
) : UsersFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val stack: Value<ChildStack<*, Child>>

    sealed class Child {
        data class FriendsChild(val component: FriendsComponent) : Child()
        data class RequestsChild(val component: RequestsComponent) : Child()
        data class UserProfileChild(val component: UserProfileComponent) : Child()
        data class EmployeeProfileChild(val component: EmployeeProfileComponent) : Child()
    }

    class Default(
        startConfig: StartFeatureConfig<UsersConfig>,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<UsersOutput>,
        private val friendsStoreFactory: FriendsComposeStore.Factory,
        private val requestsStoreFactory: RequestsComposeStore.Factory,
        private val userProfileStoreFactory: UserProfileComposeStore.Factory,
        private val employeeProfileStoreFactory: EmployeeProfileComposeStore.Factory,
    ) : InternalUsersFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {
        override val contentProvider = UsersContentProvider(this)

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<UsersConfig>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = UsersConfig.serializer(),
            initialStack = { startConfig.backstack ?: listOf(UsersConfig.Friends) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        private companion object {
            const val STACK_KEY = "Users_ROOT_STACK"
        }

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(UsersOutput.NavigateToBack)
            }
        }

        override fun onDestroyInstance() {
            UsersFeatureManager.finish()
        }

        private fun createChild(config: UsersConfig, componentContext: ComponentContext): Child {
            return when (config) {
                is UsersConfig.Friends -> Child.FriendsChild(
                    component = FriendsComponent.Default(
                        storeFactory = friendsStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = friendsOutputConsumer(),
                    )
                )
                is UsersConfig.Requests -> Child.RequestsChild(
                    component = RequestsComponent.Default(
                        storeFactory = requestsStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = requestsOutputConsumer(),
                    )
                )
                is UsersConfig.UserProfile -> Child.UserProfileChild(
                    component = UserProfileComponent.Default(
                        storeFactory = userProfileStoreFactory,
                        componentContext = componentContext,
                        inputData = UserProfileInput(config.userId),
                        outputConsumer = userProfileOutputConsumer(),
                    )
                )
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

        private fun friendsOutputConsumer() = OutputConsumer<FriendsOutput> { output ->
            when (output) {
                is FriendsOutput.NavigateToRequests -> {
                    stackNavigation.pushToFront(UsersConfig.Requests)
                }
                is FriendsOutput.NavigateToUserProfile -> {
                    val config = UsersConfig.UserProfile(output.config.userId)
                    stackNavigation.pushToFront(config)
                }
                is FriendsOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun requestsOutputConsumer() = OutputConsumer<RequestsOutput> { output ->
            when (output) {
                is RequestsOutput.NavigateToUserProfile -> {
                    val config = UsersConfig.UserProfile(output.config.userId)
                    stackNavigation.pushToFront(config)
                }
                is RequestsOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun userProfileOutputConsumer() = OutputConsumer<UserProfileOutput> { output ->
            when (output) {
                is UserProfileOutput.NavigateToBack -> navigateToBack()
            }
        }

        private fun employeeProfileOutputConsumer() = OutputConsumer<EmployeeProfileOutput> { output ->
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