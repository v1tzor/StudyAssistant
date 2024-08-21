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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.NestedFeatureNavigator
import ru.aleshin.studyassistant.core.common.navigation.rememberNavigatorManager
import ru.aleshin.studyassistant.core.common.navigation.rememberScreenProvider
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.api.presentation.TasksRootScreen
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksNavigatorManager
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksTheme

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
internal class NavigationScreen : TasksRootScreen() {

    @Composable
    override fun Content() = withDirectDI(directDI = { TasksFeatureDIHolder.fetchDI() }) {
        val screenModel = rememberScreenModel { NavigationScreenModel() }
        val screenProvider = rememberScreenProvider<TasksScreenProvider, TasksScreen, TasksRootScreen>()
        val navigatorManager = rememberNavigatorManager<TasksNavigatorManager, TasksScreen, TasksRootScreen>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            TasksTheme(content = { CurrentScreen() })
        }
    }
}