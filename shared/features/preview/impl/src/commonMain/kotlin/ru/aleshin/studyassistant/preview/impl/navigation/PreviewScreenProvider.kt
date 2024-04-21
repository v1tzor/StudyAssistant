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

package ru.aleshin.studyassistant.preview.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import inject.MainScreen
import navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.IntroScreen
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.SetupScreen

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
internal interface PreviewScreenProvider : FeatureScreenProvider<PreviewScreen> {

    fun provideAuthScreen(screen: AuthScreen): Screen

    fun provideTabNavigationScreen(): Screen

    class Base(
        private val navigationFeatureStarter: () -> NavigationFeatureStarter,
        private val authFeatureStarter: () -> AuthFeatureStarter,
    ) : PreviewScreenProvider {

        override fun provideFeatureScreen(screen: PreviewScreen) = when (screen) {
            is PreviewScreen.Intro -> IntroScreen()
            is PreviewScreen.Setup -> SetupScreen()
        }

        override fun provideAuthScreen(screen: AuthScreen): Screen {
            return authFeatureStarter().fetchFeatureScreen(screen)
        }

        override fun provideTabNavigationScreen(): Screen {
            return navigationFeatureStarter().fetchFeatureScreen(MainScreen)
        }
    }
}
