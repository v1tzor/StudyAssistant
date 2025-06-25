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

import ru.aleshin.studyassistant.chat.api.navigation.ChatFeatureStarter
import ru.aleshin.studyassistant.chat.api.navigation.ChatScreen
import ru.aleshin.studyassistant.chat.api.presentation.ChatRootScreen
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.info.api.navigation.InfoScreen
import ru.aleshin.studyassistant.info.api.presentation.InfoRootScreen
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.profile.api.presentation.ProfileRootScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.api.presentation.ScheduleRootScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.api.presentation.TasksRootScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface TabScreenProvider {

    fun provideScheduleScreen(screen: ScheduleScreen): ScheduleRootScreen
    fun provideTasksScreen(screen: TasksScreen): TasksRootScreen
    fun provideChatScreen(screen: ChatScreen): ChatRootScreen
    fun provideInfoScreen(screen: InfoScreen): InfoRootScreen
    fun provideProfileScreen(): ProfileRootScreen

    class Base(
        private val scheduleFeatureStarter: () -> ScheduleFeatureStarter,
        private val tasksFeatureStarter: () -> TasksFeatureStarter,
        private val chatFeatureStarter: () -> ChatFeatureStarter,
        private val infoFeatureStarter: () -> InfoFeatureStarter,
        private val profileFeatureStarter: () -> ProfileFeatureStarter,
    ) : TabScreenProvider {

        override fun provideScheduleScreen(screen: ScheduleScreen): ScheduleRootScreen {
            return scheduleFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideTasksScreen(screen: TasksScreen): TasksRootScreen {
            return tasksFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideChatScreen(screen: ChatScreen): ChatRootScreen {
            return chatFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideInfoScreen(screen: InfoScreen): InfoRootScreen {
            return infoFeatureStarter().fetchRootScreenAndNavigate(screen, isNavigate = false)
        }

        override fun provideProfileScreen(): ProfileRootScreen {
            return profileFeatureStarter().fetchFeatureScreen()
        }
    }
}