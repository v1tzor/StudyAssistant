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
import navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.ForgotScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.LoginScreen
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.RegisterScreen
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal interface AuthScreenProvider : FeatureScreenProvider<AuthScreen> {

    fun providePreviewScreen(screen: PreviewScreen): Screen

    fun provideTabNavigationScreen(): Screen

    class Base(
        private val previewFeatureProvider: () -> PreviewFeatureStarter,
        private val navigationFeatureProvider: () -> NavigationFeatureStarter,
    ) : AuthScreenProvider {

        override fun provideFeatureScreen(screen: AuthScreen) = when (screen) {
            is AuthScreen.Login -> LoginScreen()
            is AuthScreen.Register -> RegisterScreen()
            is AuthScreen.Forgot -> ForgotScreen()
        }

        override fun providePreviewScreen(screen: PreviewScreen): Screen {
            return previewFeatureProvider().fetchRootScreenAndNavigate(screen)
        }

        override fun provideTabNavigationScreen(): Screen {
            return navigationFeatureProvider().fetchFeatureScreen()
        }
    }
}