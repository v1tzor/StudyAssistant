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

package ru.aleshin.studyassistant.schedule.impl.navigation

import inject.FeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.navigation.NavigationScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal class ScheduleFeatureStarterImpl(
    navScreen: NavigationScreen,
    navigatorManager: ScheduleNavigatorManager,
    screenProvider: ScheduleScreenProvider,
) : ScheduleFeatureStarter, FeatureStarter.Navigation<ScheduleScreen>(
    featureNavScreen = navScreen,
    navigatorManager = navigatorManager,
    screenProvider = screenProvider,
)