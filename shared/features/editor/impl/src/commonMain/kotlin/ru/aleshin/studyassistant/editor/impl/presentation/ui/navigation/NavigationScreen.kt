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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.NestedFeatureNavigator
import ru.aleshin.studyassistant.core.common.navigation.rememberNavigatorManager
import ru.aleshin.studyassistant.core.common.navigation.rememberScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.api.presentation.EditorRootScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorNavigatorManager
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorTheme

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class NavigationScreen : EditorRootScreen() {

    @Composable
    override fun Content() = withDirectDI(directDI = { EditorFeatureDIHolder.fetchDI() }) {
        val screenProvider = rememberScreenProvider<EditorScreenProvider, EditorScreen, EditorRootScreen>()
        val navigatorManager = rememberNavigatorManager<EditorNavigatorManager, EditorScreen, EditorRootScreen>()
        val screenModel = rememberScreenModel<NavigationScreenModel>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            EditorTheme(content = { CurrentScreen() })
        }
    }
}