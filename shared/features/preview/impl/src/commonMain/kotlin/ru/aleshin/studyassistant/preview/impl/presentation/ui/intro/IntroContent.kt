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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.theme.tokens.WindowSize
import ru.aleshin.studyassistant.core.ui.views.CircularStepsRow
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.AuthActionsSection
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroPage
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.NavActionsSection

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun IntroContent(
    introComponent: IntroComponent
) {
    val store = introComponent.store
    val strings = PreviewThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val windowSize = LocalWindowSize.current
    val pagerState = rememberPagerState { IntroPage.entries.size }
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            when (windowSize.heightWindowType) {
                WindowSize.WindowType.COMPACT -> BaseIntroContentCompact(
                    modifier = Modifier.padding(paddingValues),
                    pagerState = pagerState,
                    onBackClick = {
                        store.dispatchEvent(IntroEvent.SelectedPreviousPage(pagerState.currentPage))
                    },
                    onContinueClick = {
                        store.dispatchEvent(IntroEvent.SelectedNextPage(pagerState.currentPage))
                    },
                    onLoginClick = {
                        store.dispatchEvent(IntroEvent.ClickLogin)
                    },
                    onRegisterClick = {
                        store.dispatchEvent(IntroEvent.ClickRegister)
                    },
                )
                else -> BaseIntroContent(
                    modifier = Modifier.padding(paddingValues),
                    pagerState = pagerState,
                    onBackClick = {
                        store.dispatchEvent(IntroEvent.SelectedPreviousPage(pagerState.currentPage))
                    },
                    onContinueClick = {
                        store.dispatchEvent(IntroEvent.SelectedNextPage(pagerState.currentPage))
                    },
                    onLoginClick = {
                        store.dispatchEvent(IntroEvent.ClickLogin)
                    },
                    onRegisterClick = {
                        store.dispatchEvent(IntroEvent.ClickRegister)
                    },
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

    store.handleEffects { effect ->
        when (effect) {
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

@Composable
@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
private fun BaseIntroContent(
    modifier: Modifier,
    pagerState: PagerState,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { pageIndex ->
            val page = IntroPage.fetchByIndex(pageIndex) ?: IntroPage.entries.first()
            InfoSection(
                illustration = painterResource(page.illustration),
                headline = page.headline,
                body = page.body,
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularStepsRow(
                stepsCount = IntroPage.entries.size,
                currentStep = pagerState.currentPage,
            )
            AnimatedContent(
                targetState = pagerState.currentPage == IntroPage.entries.lastIndex,
            ) { isLastPage ->
                if (isLastPage) {
                    AuthActionsSection(
                        onBackClick = onBackClick,
                        onLoginClick = onLoginClick,
                        onRegisterClick = onRegisterClick,
                    )
                } else {
                    NavActionsSection(
                        canBackMove = pagerState.currentPage != 0,
                        onBackClick = onBackClick,
                        onContinueClick = onContinueClick,
                    )
                }
            }
        }
    }
}

@Composable
@ExperimentalResourceApi
internal fun InfoSection(
    modifier: Modifier = Modifier,
    illustration: Painter,
    headline: String,
    body: String,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.padding(horizontal = 32.dp).weight(1f),
            painter = illustration,
            contentDescription = headline,
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
        )
        Column(
            modifier = Modifier.padding(horizontal = 24.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = headline,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                )
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}