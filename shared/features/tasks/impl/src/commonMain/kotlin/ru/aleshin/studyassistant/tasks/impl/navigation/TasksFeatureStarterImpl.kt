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

package ru.aleshin.studyassistant.tasks.impl.navigation

import ru.aleshin.studyassistant.core.common.inject.FeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.api.presentation.TasksRootScreen
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.navigation.NavigationScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal class TasksFeatureStarterImpl(
    navScreen: NavigationScreen,
    navigatorManager: TasksNavigatorManager,
    screenProvider: TasksScreenProvider,
) : TasksFeatureStarter, FeatureStarter.WithNestedNavigation.Abstract<TasksScreen, TasksRootScreen>(
    featureNavScreen = navScreen,
    navigatorManager = navigatorManager,
    screenProvider = screenProvider,
)