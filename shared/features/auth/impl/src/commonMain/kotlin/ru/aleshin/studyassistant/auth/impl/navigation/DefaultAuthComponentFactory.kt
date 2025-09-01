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

package ru.aleshin.studyassistant.auth.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthConfig
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthOutput
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponentFactory
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.store.ForgotComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store.LoginComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.store.RegisterComposeStore
import ru.aleshin.studyassistant.auth.impl.presentation.ui.root.InternalAuthFeatureComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationComposeStore
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
internal class DefaultAuthComponentFactory(
    private val loginStoreFactory: LoginComposeStore.Factory,
    private val registerStoreFactory: RegisterComposeStore.Factory,
    private val verificationStoreFactory: VerificationComposeStore.Factory,
    private val forgotStoreFactory: ForgotComposeStore.Factory,
) : AuthFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<AuthConfig>,
        outputConsumer: OutputConsumer<AuthOutput>
    ): AuthFeatureComponent {
        return InternalAuthFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            loginStoreFactory = loginStoreFactory,
            registerStoreFactory = registerStoreFactory,
            verificationStoreFactory = verificationStoreFactory,
            forgotStoreFactory = forgotStoreFactory,
        )
    }
}