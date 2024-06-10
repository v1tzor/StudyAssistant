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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import di.withDirectDI
import navigation.NestedFeatureNavigator
import navigation.rememberNavigatorManager
import navigation.rememberScreenProvider
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleNavigatorManager
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleTheme

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal class NavigationScreen : Screen {

    @Composable
    override fun Content() = withDirectDI(directDI = { ScheduleFeatureDIHolder.fetchDI() }) {
        val screenModel = rememberScreenModel { NavigationScreenModel() }
        val screenProvider = rememberScreenProvider<ScheduleScreenProvider, ScheduleScreen>()
        val navigatorManager = rememberNavigatorManager<ScheduleNavigatorManager, ScheduleScreen>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            ScheduleTheme(content = { CurrentScreen() })
        }
    }
}