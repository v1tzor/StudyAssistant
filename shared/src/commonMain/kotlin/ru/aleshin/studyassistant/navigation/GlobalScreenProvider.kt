/*
 * Copyright 2023 Stanislav Aleshin
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

package ru.aleshin.studyassistant.navigation

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen

/**
 * @author Stanislav Aleshin on 09.02.2024.
 */
interface GlobalScreenProvider {

    fun providePreviewScreen(screen: PreviewScreen): Screen
    fun provideAuthScreen(screen: AuthScreen): Screen
    fun provideScheduleScreen(screen: ScheduleScreen): Screen
    fun provideEditorScreen(screen: EditorScreen): Screen
    fun provideTabNavigationScreen(): Screen

    class Base(
        private val navigationStarter: () -> NavigationFeatureStarter,
        private val previewStarter: () -> PreviewFeatureStarter,
        private val authStarter: () -> AuthFeatureStarter,
        private val scheduleStarter: () -> ScheduleFeatureStarter,
        private val editorStarter: () -> EditorFeatureStarter,
    ) : GlobalScreenProvider {

        override fun providePreviewScreen(screen: PreviewScreen): Screen {
            return previewStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideAuthScreen(screen: AuthScreen): Screen {
            return authStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideScheduleScreen(screen: ScheduleScreen): Screen {
            return scheduleStarter().fetchRootScreenAndNavigate(screen)
        }

        override fun provideTabNavigationScreen(): Screen {
            return navigationStarter().fetchFeatureScreen()
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorStarter().fetchRootScreenAndNavigate(screen)
        }
    }
}