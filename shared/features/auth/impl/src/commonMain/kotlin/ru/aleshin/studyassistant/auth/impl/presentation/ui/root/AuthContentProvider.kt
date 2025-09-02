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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.stack.ChildStack
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureManager
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthTheme
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.ForgotContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.LoginContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.RegisterContent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.root.InternalAuthFeatureComponent.Child
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.VerificationContent
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.core.common.navigation.backAnimation

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
public class AuthContentProvider internal constructor(
    private val component: InternalAuthFeatureComponent,
) : FeatureContentProvider {

    @Composable
    @OptIn(ExperimentalDecomposeApi::class)
    override fun invoke(modifier: Modifier) {
        withDirectDI(directDI = { AuthFeatureManager.fetchDI() }) {
            AuthTheme {
                ChildStack(
                    modifier = modifier,
                    stack = component.stack,
                    animation = backAnimation(
                        backHandler = component.backHandler,
                        onBack = component::navigateToBack
                    )
                ) { child ->
                    when (val instance = child.instance) {
                        is Child.LoginChild -> {
                            LoginContent(instance.component)
                        }
                        is Child.RegisterChild -> {
                            RegisterContent(instance.component)
                        }
                        is Child.VerificationChild -> {
                            VerificationContent(instance.component)
                        }
                        is Child.ForgotChild -> {
                            ForgotContent(instance.component)
                        }
                    }
                }
            }
        }
    }
}