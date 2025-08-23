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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthTheme
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.ForgotContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.LoginContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.RegisterContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.VerificationContent
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.backAnimation

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
@OptIn(ExperimentalDecomposeApi::class)
public fun RootAuthContent(
    modifier: Modifier = Modifier,
    rootComponent: AuthRootComponent,
) {
    AuthTheme {
        withDirectDI(
            directDI = { AuthFeatureDIHolder.fetchDI() }
        ) {
            Children(
                modifier = modifier,
                stack = rootComponent.stack,
                animation = backAnimation(
                    backHandler = rootComponent.backHandler,
                    onBack = rootComponent::navigateToBack
                )
            ) { child ->
                when (val instance = child.instance) {
                    is AuthRootComponent.Child.LoginChild -> {
                        LoginContent(instance.component)
                    }
                    is AuthRootComponent.Child.RegisterChild -> {
                        RegisterContent(instance.component)
                    }
                    is AuthRootComponent.Child.VerificationChild -> {
                        VerificationContent(instance.component)
                    }
                    is AuthRootComponent.Child.ForgotChild -> {
                        ForgotContent(instance.component)
                    }
                }
            }
        }
    }
}