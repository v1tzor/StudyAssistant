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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import di.withDirectDI
import navigation.NestedFeatureNavigator
import navigation.rememberNavigatorManager
import navigation.rememberScreenProvider
import org.kodein.di.compose.withDI
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.navigation.AuthNavigatorManager
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthTheme

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal class NavigationScreen : Screen {

    @Composable
    override fun Content() = withDirectDI(directDI = { AuthFeatureDIHolder.fetchDI() }) {
        val screenModel = rememberScreenModel { NavScreenModel() }
        val screenProvider = rememberScreenProvider<AuthScreenProvider, AuthScreen>()
        val navigatorManager = rememberNavigatorManager<AuthNavigatorManager, AuthScreen>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            AuthTheme(content = { CurrentScreen() })
        }
    }
}