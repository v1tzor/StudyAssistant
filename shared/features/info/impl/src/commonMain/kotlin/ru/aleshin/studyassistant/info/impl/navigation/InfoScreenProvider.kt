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

package ru.aleshin.studyassistant.info.impl.navigation

import cafe.adriel.voyager.core.screen.Screen
import navigation.FeatureScreenProvider
import architecture.screens.EmptyScreen
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.info.api.navigation.InfoScreen

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface InfoScreenProvider : FeatureScreenProvider<InfoScreen> {

    fun provideEditorScreen(screen: EditorScreen): Screen

    class Base(
        private val editorFeatureStarter: () -> EditorFeatureStarter,
    ) : InfoScreenProvider {

        override fun provideFeatureScreen(screen: InfoScreen) = when (screen) {
            is InfoScreen.Organizations -> EmptyScreen
            is InfoScreen.Subjects -> EmptyScreen
            is InfoScreen.Employee -> EmptyScreen
        }

        override fun provideEditorScreen(screen: EditorScreen): Screen {
            return editorFeatureStarter().fetchFeatureScreen(screen)
        }
    }
}