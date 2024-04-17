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
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.factory
import org.kodein.di.instance
import org.kodein.di.provider
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.presentation.ui.tabs.TabsScreen
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 09.02.2024.
 */
interface MainScreenProvider {

    fun provideTabsScreen(): TabsScreen
    fun providePreviewScreen(screen: PreviewScreen): Screen
    fun provideAuthScreen(screen: AuthScreen): Screen
    fun provideScheduleScreen(): Screen
    fun provideTasksScreen(): Screen
    fun provideInfoScreen(): Screen
    fun provideProfileScreen(): Screen

    class Base(
        private val previewStarter: () -> PreviewFeatureStarter,
        private val authStarter: () -> AuthFeatureStarter,
    ) : MainScreenProvider {

        override fun provideTabsScreen() = TabsScreen()

        override fun providePreviewScreen(screen: PreviewScreen): Screen {
            return previewStarter().fetchPreviewScreen(screen)
        }

        override fun provideAuthScreen(screen: AuthScreen): Screen {
            return authStarter().fetchAuthScreen(screen)
        }

        override fun provideScheduleScreen(): Screen {
            TODO("Not yet implemented")
        }

        override fun provideTasksScreen(): Screen {
            TODO("Not yet implemented")
        }

        override fun provideInfoScreen(): Screen {
            TODO("Not yet implemented")
        }

        override fun provideProfileScreen(): Screen {
            TODO("Not yet implemented")
        }
    }
}
