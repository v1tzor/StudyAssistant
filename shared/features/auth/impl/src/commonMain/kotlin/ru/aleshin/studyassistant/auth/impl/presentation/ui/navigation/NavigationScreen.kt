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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.auth.api.presentation.AuthRootScreen
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.auth.impl.navigation.AuthNavigatorManager
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthTheme
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.NestedFeatureNavigator
import ru.aleshin.studyassistant.core.common.navigation.rememberNavigatorManager
import ru.aleshin.studyassistant.core.common.navigation.rememberScreenProvider

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal class NavigationScreen : AuthRootScreen() {

    @Composable
    override fun Content() = withDirectDI(directDI = { AuthFeatureDIHolder.fetchDI() }) {
        val screenModel = rememberScreenModel { NavScreenModel() }
        val screenProvider = rememberScreenProvider<AuthScreenProvider, AuthScreen, AuthRootScreen>()
        val navigatorManager = rememberNavigatorManager<AuthNavigatorManager, AuthScreen, AuthRootScreen>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            AuthTheme(content = { CurrentScreen() })
        }
    }
}