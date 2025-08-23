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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.navigation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.retainedInstance
import ru.aleshin.studyassistant.auth.api.presentation.BaseAuthRootComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract.ForgotOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationComposeStore
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer

/**
 * @author Stanislav Aleshin on 20.08.2025.
 */
public abstract class AuthRootComponent(
    componentContext: ComponentContext,
) : BaseAuthRootComponent(componentContext) {

    public abstract fun navigateToBack()

    internal abstract val stack: Value<ChildStack<*, Child>>

    internal sealed class Child {
        data class LoginChild(val component: LoginComponent) : Child()
        data class RegisterChild(val component: RegisterComponent) : Child()
        data class ForgotChild(val component: ForgotComponent) : Child()
        data class VerificationChild(val component: VerificationComponent) : Child()
    }

    internal class Default(
        startConfig: List<Config>?,
        componentContext: ComponentContext,
        private val featureDestroyerFactory: () -> AuthFeatureDestroyer,
        private val loginStoreFactory: LoginComposeStore.Factory,
        private val registerStoreFactory: RegisterComposeStore.Factory,
        private val verificationStoreFactory: VerificationComposeStore.Factory,
        private val forgotStoreFactory: ForgotComposeStore.Factory,
        private val outputConsumer: OutputConsumer<Output>
    ) : AuthRootComponent(
        componentContext = componentContext,
    ) {
        private companion object {
            const val STACK_KEY = "AUTH_ROOT_STACK"
        }

        private val featureDestroyer: AuthFeatureDestroyer = retainedInstance {
            featureDestroyerFactory()
        }

        private val backCallback = BackCallback { navigateToBack() }

        private val stackNavigation = StackNavigation<Config>()

        override val stack: Value<ChildStack<*, Child>> = childStack(
            source = stackNavigation,
            serializer = Config.serializer(),
            initialStack = { startConfig ?: listOf(Config.Login) },
            key = STACK_KEY,
            handleBackButton = false,
            childFactory = ::createChild,
        )

        init {
            backHandler.register(backCallback)
        }

        override fun navigateToBack() {
            stackNavigation.pop { isPop ->
                if (!isPop) outputConsumer.consume(Output.NavigateToBack)
            }
        }

        private fun createChild(config: Config, componentContext: ComponentContext): Child {
            return when (config) {
                is Config.Login -> Child.LoginChild(
                    component = LoginComponent.Default(
                        storeFactory = loginStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = loginOutputConsumer(),
                    )
                )
                is Config.Register -> Child.RegisterChild(
                    component = RegisterComponent.Default(
                        storeFactory = registerStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = registerOutputConsumer(),
                    )
                )
                is Config.Forgot -> Child.ForgotChild(
                    component = ForgotComponent.Default(
                        storeFactory = forgotStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = forgotOutputConsumer(),
                    )
                )
                is Config.Verification -> Child.VerificationChild(
                    component = VerificationComponent.Default(
                        storeFactory = verificationStoreFactory,
                        componentContext = componentContext,
                        outputConsumer = verificationOutputConsumer(),
                    )
                )
            }
        }

        private fun loginOutputConsumer() = OutputConsumer<LoginOutput> { data ->
            when (data) {
                is LoginOutput.NavigateToSignUp -> {
                    stackNavigation.pushToFront(Config.Register)
                }
                is LoginOutput.NavigateToRecovery -> {
                    stackNavigation.pushToFront(Config.Forgot)
                }
                is LoginOutput.NavigateToVerification -> {
                    stackNavigation.replaceAll(Config.Verification)
                }
                is LoginOutput.NavigateToFirstSetup -> {
                    outputConsumer.consume(Output.NavigateToFirstSetup)
                }
                is LoginOutput.NavigateToApp -> {
                    outputConsumer.consume(Output.NavigateToApp)
                }
            }
        }

        private fun registerOutputConsumer() = OutputConsumer<RegisterOutput> { data ->
            when (data) {
                is RegisterOutput.NavigateToLogin -> {
                    stackNavigation.pushToFront(Config.Login)
                }
                is RegisterOutput.NavigateToVerification -> {
                    stackNavigation.replaceAll(Config.Verification)
                }
            }
        }

        private fun verificationOutputConsumer() = OutputConsumer<VerificationOutput> { data ->
            when (data) {
                is VerificationOutput.NavigateToLogin -> {
                    stackNavigation.replaceAll(Config.Login)
                }
                is VerificationOutput.NavigateToFirstSetup -> {
                    outputConsumer.consume(Output.NavigateToFirstSetup)
                }
            }
        }

        private fun forgotOutputConsumer() = OutputConsumer<ForgotOutput> { data ->
            when (data) {
                is ForgotOutput.NavigateToLogin -> {
                    stackNavigation.pushToFront(Config.Login)
                }
            }
        }
    }
}