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

package ru.aleshin.studyassistant.users.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.users.api.navigation.UsersScreen
import ru.aleshin.studyassistant.users.api.presentation.UsersRootScreen
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.EmployeeProfileScreen
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.FriendsScreen
import ru.aleshin.studyassistant.users.impl.presentation.ui.requests.RequestsScreen
import ru.aleshin.studyassistant.users.impl.presentation.ui.user.UserProfileScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface UsersScreenProvider : FeatureScreenProvider<UsersScreen, UsersRootScreen> {

    fun provideEditorScreen(screen: EditorScreen): Screen

    class Base(
        private val editorFeatureStarter: () -> EditorFeatureStarter,
    ) : UsersScreenProvider {

        override fun provideFeatureScreen(screen: UsersScreen) = when (screen) {
            is UsersScreen.Friends -> FriendsScreen()
            is UsersScreen.Requests -> RequestsScreen()
            is UsersScreen.EmployeeProfile -> EmployeeProfileScreen(screen.employeeId)
            is UsersScreen.UserProfile -> UserProfileScreen(screen.userId)
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorFeatureStarter().fetchRootScreenAndNavigate(screen)
        }
    }
}