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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.navigation

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.kodein.rememberScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import ru.aleshin.studyassistant.chat.api.navigation.ChatScreen
import ru.aleshin.studyassistant.chat.api.presentation.ChatRootScreen
import ru.aleshin.studyassistant.chat.impl.di.holder.ChatFeatureDIHolder
import ru.aleshin.studyassistant.chat.impl.navigation.ChatNavigatorManager
import ru.aleshin.studyassistant.chat.impl.navigation.ChatScreenProvider
import ru.aleshin.studyassistant.chat.impl.presentation.theme.ChatTheme
import ru.aleshin.studyassistant.core.common.di.withDirectDI
import ru.aleshin.studyassistant.core.common.navigation.NestedFeatureNavigator
import ru.aleshin.studyassistant.core.common.navigation.rememberNavigatorManager
import ru.aleshin.studyassistant.core.common.navigation.rememberScreenProvider

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class NavigationScreen : ChatRootScreen() {

    @Composable
    override fun Content() = withDirectDI(directDI = { ChatFeatureDIHolder.fetchDI() }) {
        val screenProvider = rememberScreenProvider<ChatScreenProvider, ChatScreen, ChatRootScreen>()
        val navigatorManager = rememberNavigatorManager<ChatNavigatorManager, ChatScreen, ChatRootScreen>()
        val screenModel = rememberScreenModel<NavigationScreenModel>()

        NestedFeatureNavigator(
            screenProvider = screenProvider,
            navigatorManager = navigatorManager,
        ) {
            ChatTheme(content = { CurrentScreen() })
        }
    }
}