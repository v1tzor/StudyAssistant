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

package ru.aleshin.studyassistant.profile.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.profile.api.presentation.ProfileRootScreen
import ru.aleshin.studyassistant.profile.impl.presentation.ui.ProfileScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.settings.api.navigation.SettingsFeatureStarter
import ru.aleshin.studyassistant.settings.api.navigation.SettingsScreen
import ru.aleshin.studyassistant.users.api.navigation.UsersFeatureStarter
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface ProfileScreenProvider : FeatureScreenProvider<ProfileRootScreen, ProfileRootScreen> {

    fun provideAuthScreen(screen: AuthScreen): Screen
    fun provideUsersScreen(screen: UsersScreen): Screen
    fun provideSettingsScreen(screen: SettingsScreen): Screen
    fun provideEditorScreen(screen: EditorScreen): Screen
    fun provideScheduleScreen(screen: ScheduleScreen): Screen

    class Base(
        private val authFeatureStarter: () -> AuthFeatureStarter,
        private val usersFeatureStarter: () -> UsersFeatureStarter,
        private val settingsFeatureStarter: () -> SettingsFeatureStarter,
        private val editorFeatureStarter: () -> EditorFeatureStarter,
        private val scheduleFeatureStarter: () -> ScheduleFeatureStarter,
    ) : ProfileScreenProvider {

        override fun provideFeatureScreen(screen: ProfileRootScreen): Screen {
            return ProfileScreen()
        }

        override fun provideAuthScreen(screen: AuthScreen): Screen {
            return authFeatureStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideUsersScreen(screen: UsersScreen): Screen {
            return usersFeatureStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideSettingsScreen(screen: SettingsScreen): Screen {
            return settingsFeatureStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorFeatureStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideScheduleScreen(screen: ScheduleScreen): Screen {
            return scheduleFeatureStarter().fetchRootScreenAndNavigate(screen)
        }
    }
}