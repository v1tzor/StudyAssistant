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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.theme.tokens.WindowSize
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.screenmodel.rememberIntroScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroPage

/**
 * @author Stanislav Aleshin on 14.04.2024
 */
internal class IntroScreen : Screen {

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    override fun Content() = ScreenContent(
        screenModel = rememberIntroScreenModel(),
        initialState = IntroViewState,
    ) {
        val strings = PreviewThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val windowSize = LocalWindowSize.current
        val rootNavigator = LocalNavigator.currentOrThrow.root()
        val pagerState = rememberPagerState { IntroPage.entries.size }
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                when (windowSize.heightWindowType) {
                    WindowSize.WindowType.COMPACT -> IntroContentCompact(
                        modifier = Modifier.padding(paddingValues),
                        pagerState = pagerState,
                        onBackClick = { dispatchEvent(IntroEvent.PreviousPage(pagerState.currentPage)) },
                        onContinueClick = { dispatchEvent(IntroEvent.NextPage(pagerState.currentPage)) },
                        onLoginClick = { dispatchEvent(IntroEvent.NavigateToLogin) },
                        onRegisterClick = { dispatchEvent(IntroEvent.NavigateToRegister) },
                    )
                    else -> IntroContent(
                        modifier = Modifier.padding(paddingValues),
                        pagerState = pagerState,
                        onBackClick = { dispatchEvent(IntroEvent.PreviousPage(pagerState.currentPage)) },
                        onContinueClick = { dispatchEvent(IntroEvent.NextPage(pagerState.currentPage)) },
                        onLoginClick = { dispatchEvent(IntroEvent.NavigateToLogin) },
                        onRegisterClick = { dispatchEvent(IntroEvent.NavigateToRegister) },
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is IntroEffect.ReplaceGlobalScreen -> rootNavigator.replaceAll(effect.screen)
                is IntroEffect.ScrollToPage -> pagerState.animateScrollToPage(effect.pageIndex)
                is IntroEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}