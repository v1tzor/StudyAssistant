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
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.info.api.navigation.InfoScreen
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface TabScreenProvider {

    fun provideScheduleScreen(screen: ScheduleScreen): Screen

    fun provideTasksScreen(screen: TasksScreen): Screen

    fun provideInfoScreen(screen: InfoScreen): Screen

    fun provideProfileScreen(): Screen

    class Base(
        private val scheduleFeatureStarter: () -> ScheduleFeatureStarter,
        private val tasksFeatureStarter: () -> TasksFeatureStarter,
        private val infoFeatureStarter: () -> InfoFeatureStarter,
        private val profileFeatureStarter: () -> ProfileFeatureStarter,
    ) : TabScreenProvider {

        override fun provideScheduleScreen(screen: ScheduleScreen): Screen {
            return scheduleFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideTasksScreen(screen: TasksScreen): Screen {
            return tasksFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideInfoScreen(screen: InfoScreen): Screen {
            return infoFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideProfileScreen(): Screen {
            return profileFeatureStarter().fetchFeatureScreen()
        }
    }
}