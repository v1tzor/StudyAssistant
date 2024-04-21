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
import ru.aleshin.core.common.navigation.screens.EmptyScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface ScreenProvider {

    fun provideScheduleScreen(): Screen

    fun provideTasksScreen(): Screen

    fun provideInfoScreen(): Screen

    fun provideProfileScreen(): Screen

    class Base() : ScreenProvider {

        override fun provideScheduleScreen(): Screen {
            // TODO: Not yet implemented
            return EmptyScreen
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
            // TODO: Not yet implemented
            return EmptyScreen
        }
    }
}