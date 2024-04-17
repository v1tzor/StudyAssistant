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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views.IntroPage
import views.CircularStepsRow

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
internal fun IntroContent(
    state: IntroViewState,
    modifier: Modifier,
    pagerState: PagerState,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(state = pagerState) { pageIndex ->
                val page = IntroPage.fetchByIndex(pageIndex) ?: IntroPage.entries.first()
                InfoSection(
                    illustration = painterResource(page.illustration),
                    headline = page.headline,
                    body = page.body,
                )
            }

        }
        Column(
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularStepsRow(
                countSteps = IntroPage.entries.size,
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
        modifier = modifier.widthIn(max = 600.dp),
        verticalArrangement = Arrangement.spacedBy(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            painter = illustration,
            contentDescription = headline,
            contentScale = ContentScale.FillWidth,
        )
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
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

@Composable
internal fun NavActionsSection(
    modifier: Modifier = Modifier,
    canBackMove: Boolean,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TextButton(
                onClick = onBackClick,
                enabled = canBackMove,
            ) {
                Text(
                    text = PreviewThemeRes.strings.backLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = onContinueClick,
        ) {
            Text(
                text = PreviewThemeRes.strings.continueLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
internal fun AuthActionsSection(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLoginClick,
        ) {
            Text(
                text = PreviewThemeRes.strings.loginLabel,
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        FilledTonalButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRegisterClick,
        ) {
            Text(
                text = PreviewThemeRes.strings.registerLabel,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        TextButton(onClick = onBackClick) {
            Text(
                text = PreviewThemeRes.strings.backLabel,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}