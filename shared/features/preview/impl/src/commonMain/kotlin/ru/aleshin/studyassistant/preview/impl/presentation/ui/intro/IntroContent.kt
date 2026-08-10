/*
 * Copyright 2026 Stanislav Aleshin
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.ui.utils.isCompactHeight
import ru.aleshin.studyassistant.core.ui.utils.useExpandedLayout
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroNavigationSection
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroPage
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroPageSection
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroStepsSection

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun IntroContent(
    introComponent: IntroComponent,
    modifier: Modifier = Modifier,
) {
    val store = introComponent.store
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val pagerState = rememberPagerState { IntroPage.entries.size }
    val snackbarState = remember { SnackbarHostState() }

    IntroScaffold(
        modifier = modifier,
        pagerState = pagerState,
        useHorizontalPageLayout = adaptiveInfo.useExpandedLayout || adaptiveInfo.isCompactHeight,
        snackbarState = snackbarState,
        onBackClick = {
            store.dispatchEvent(IntroEvent.SelectedPreviousPage(pagerState.currentPage))
        },
        onContinueClick = {
            store.dispatchEvent(IntroEvent.SelectedNextPage(pagerState.currentPage))
        },
        onSetupClick = {
            store.dispatchEvent(IntroEvent.ClickSetup)
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is IntroEffect.ScrollToPage -> pagerState.animateScrollToPage(effect.pageIndex)
            is IntroEffect.ShowError -> snackbarState.showSnackbar(
                message = effect.failures.mapToMessage(),
                withDismissAction = true,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun IntroScaffold(
    pagerState: PagerState,
    useHorizontalPageLayout: Boolean,
    snackbarState: SnackbarHostState,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    ) { paddingValues ->
        IntroLayout(
            modifier = Modifier.padding(paddingValues),
            pagerState = pagerState,
            useHorizontalPageLayout = useHorizontalPageLayout,
            onBackClick = onBackClick,
            onContinueClick = onContinueClick,
            onSetupClick = onSetupClick,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun IntroLayout(
    pagerState: PagerState,
    useHorizontalPageLayout: Boolean,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().widthIn(max = 1200.dp),
        ) {
            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = pagerState,
                key = { pageIndex -> IntroPage.entries[pageIndex].name },
            ) { pageIndex ->
                IntroPageSection(
                    page = IntroPage.entries[pageIndex],
                    useHorizontalLayout = useHorizontalPageLayout,
                )
            }
            IntroStepsSection(
                stepsCount = IntroPage.entries.size,
                currentStep = pagerState.currentPage,
            )
            IntroNavigationSection(
                isFirstPage = pagerState.currentPage == 0,
                isLastPage = pagerState.currentPage == IntroPage.entries.lastIndex,
                onBackClick = onBackClick,
                onContinueClick = onContinueClick,
                onSetupClick = onSetupClick,
            )
        }
    }
}
