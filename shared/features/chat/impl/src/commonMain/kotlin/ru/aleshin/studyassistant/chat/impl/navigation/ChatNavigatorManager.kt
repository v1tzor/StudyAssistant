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

package ru.aleshin.studyassistant.chat.impl.navigation

import ru.aleshin.studyassistant.chat.api.navigation.ChatScreen
import ru.aleshin.studyassistant.chat.api.presentation.ChatRootScreen
import ru.aleshin.studyassistant.core.common.navigation.CommandBuffer
import ru.aleshin.studyassistant.core.common.navigation.NavigatorManager

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
internal interface ChatNavigatorManager : NavigatorManager<ChatScreen, ChatRootScreen> {
    class Base(commandBuffer: CommandBuffer) : ChatNavigatorManager,
        NavigatorManager.Abstract<ChatScreen, ChatRootScreen>(commandBuffer = commandBuffer)
}