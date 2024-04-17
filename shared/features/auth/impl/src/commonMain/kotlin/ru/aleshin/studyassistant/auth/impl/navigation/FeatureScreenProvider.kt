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

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.ForgotScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.LoginScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.RegisterScreen
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal interface FeatureScreenProvider {

    fun providePreviewScreen(screen: PreviewScreen): Screen

    fun provideLoginScreen(): LoginScreen

    fun provideRegisterScreen(): RegisterScreen

    fun provideForgotScreen(): ForgotScreen

    class Base(
        private val previewFeatureProvider: () -> PreviewFeatureStarter,
    ) : FeatureScreenProvider {

        override fun providePreviewScreen(screen: PreviewScreen): Screen {
            return previewFeatureProvider().fetchPreviewScreen(screen)
        }

        override fun provideLoginScreen(): LoginScreen {
            return LoginScreen()
        }

        override fun provideRegisterScreen(): RegisterScreen {
            return RegisterScreen()
        }

        override fun provideForgotScreen(): ForgotScreen {
            return ForgotScreen()
        }
    }
}