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

package ru.aleshin.studyassistant.navigation.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import inject.MainScreen
import ru.aleshin.core.common.navigation.screens.EmptyScreen
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface NavigationScreenProvider {

    fun provideScheduleScreen(screen: ScheduleScreen): Screen

    fun provideTasksScreen(): Screen

    fun provideInfoScreen(): Screen

    fun provideProfileScreen(): Screen

    class Base(
        private val scheduleFeatureStarter: () -> ScheduleFeatureStarter,
        private val profileFeatureStarter: () -> ProfileFeatureStarter,
    ) : NavigationScreenProvider {

        override fun provideScheduleScreen(screen: ScheduleScreen): Screen {
            return scheduleFeatureStarter().fetchFeatureScreen(screen)
        }

        override fun provideTasksScreen(): Screen {
            // TODO: Not yet implemented
            return EmptyScreen
        }

        override fun provideInfoScreen(): Screen {
            // TODO: Not yet implemented
            return EmptyScreen
        }

        override fun provideProfileScreen(): Screen {
            return profileFeatureStarter().fetchFeatureScreen(MainScreen)
        }
    }
}