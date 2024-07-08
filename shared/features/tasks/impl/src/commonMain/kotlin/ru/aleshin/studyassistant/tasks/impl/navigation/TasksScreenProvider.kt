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

import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.core.common.navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.HomeworksScreen
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.OverviewScreen
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.TodosScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface TasksScreenProvider : FeatureScreenProvider<TasksScreen> {

    fun provideEditorScreen(screen: EditorScreen): Screen

    class Base(
        private val editorFeatureStarter: () -> EditorFeatureStarter,
    ) : TasksScreenProvider {

        override fun provideFeatureScreen(screen: TasksScreen) = when (screen) {
            is TasksScreen.Overview -> OverviewScreen()
            is TasksScreen.Homeworks -> HomeworksScreen(screen.targetDate)
            is TasksScreen.Todos -> TodosScreen()
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorFeatureStarter().fetchRootScreenAndNavigate(screen)
        }
    }
}