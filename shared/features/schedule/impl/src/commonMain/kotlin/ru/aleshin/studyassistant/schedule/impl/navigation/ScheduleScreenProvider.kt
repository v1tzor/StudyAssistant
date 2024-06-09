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

package ru.aleshin.studyassistant.schedule.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import navigation.FeatureScreenProvider
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.DetailsScreen
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.OverviewScreen

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface ScheduleScreenProvider : FeatureScreenProvider<ScheduleScreen> {

    fun provideEditorScreen(screen: EditorScreen): Screen

    class Base(
        private val editorFeatureStarter: () -> EditorFeatureStarter,
    ) : ScheduleScreenProvider {

        override fun provideFeatureScreen(screen: ScheduleScreen) = when (screen) {
            is ScheduleScreen.Overview -> OverviewScreen()
            is ScheduleScreen.Details -> DetailsScreen()
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorFeatureStarter().fetchFeatureScreen(screen)
        }
    }
}
