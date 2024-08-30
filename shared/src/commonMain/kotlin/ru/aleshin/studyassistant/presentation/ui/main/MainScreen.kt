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

package ru.aleshin.studyassistant.presentation.ui.main

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.NavigatorDisposeBehavior
import cafe.adriel.voyager.transitions.CrossfadeTransition
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantTheme
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainViewState
import ru.aleshin.studyassistant.presentation.ui.main.screenmodel.rememberMainScreenModel
import ru.aleshin.studyassistant.presentation.ui.splash.SplashScreen

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
fun MainScreen() = ScreenContent(
    screenModel = rememberMainScreenModel(),
    initialState = MainViewState(),
) { state ->
    StudyAssistantTheme(
        themeType = state.generalSettings.themeType,
        languageType = state.generalSettings.languageType,
    ) {
        Navigator(
            screen = SplashScreen(),
            disposeBehavior = NavigatorDisposeBehavior(
                disposeNestedNavigators = false,
                disposeSteps = true,
            ),
        ) { navigator ->
            CrossfadeTransition(navigator = navigator)

            handleEffect { effect ->
                when (effect) {
                    is MainEffect.ReplaceGlobalScreen -> navigator.replaceAll(effect.screen)
                    is MainEffect.ShowError -> error(effect.failures)
                }
            }
        }
    }
}