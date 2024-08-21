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

package ru.aleshin.studyassistant.tasks.impl.navigation

import ru.aleshin.studyassistant.core.common.navigation.CommandBuffer
import ru.aleshin.studyassistant.core.common.navigation.NavigatorManager
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.api.presentation.TasksRootScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface TasksNavigatorManager : NavigatorManager<TasksScreen, TasksRootScreen> {
    class Base(commandBuffer: CommandBuffer) : TasksNavigatorManager,
        NavigatorManager.Abstract<TasksScreen, TasksRootScreen>(commandBuffer = commandBuffer)
}