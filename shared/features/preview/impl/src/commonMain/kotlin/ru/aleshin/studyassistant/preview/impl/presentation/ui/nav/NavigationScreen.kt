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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import di.withDirectDI
import navigation.NavigatorManager
import navigation.NestedFeatureNavigator
import navigation.rememberNavigatorManager
import navigation.rememberScreenProvider
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewNavigatorManager
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewScreenProvider
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewTheme

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
internal class NavigationScreen : Screen {

    @Composable
    override fun Content() = withDirectDI(directDI = { PreviewFeatureDIHolder.fetchDI() }) {
        val screenModel = rememberScreenModel { NavScreenModel() }
        val screenProvider = rememberScreenProvider<PreviewScreenProvider, PreviewScreen>()
        val navigatorManager = rememberNavigatorManager<PreviewNavigatorManager, PreviewScreen>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            PreviewTheme(content = { CurrentScreen() })
        }
    }
}
