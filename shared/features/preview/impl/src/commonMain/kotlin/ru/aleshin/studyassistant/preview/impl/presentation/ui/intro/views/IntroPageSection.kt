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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun IntroPageSection(
    page: IntroPage,
    useHorizontalLayout: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp? = null,
) {
    val resolvedHorizontalPadding = horizontalPadding ?: if (useHorizontalLayout) 48.dp else 24.dp
    if (useHorizontalLayout) {
        Row(
            modifier = modifier.fillMaxSize().padding(horizontal = resolvedHorizontalPadding, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IntroIllustration(
                page = page,
                modifier = Modifier.weight(1f),
            )
            IntroPageText(
                page = page,
                modifier = Modifier.weight(1f),
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize().padding(horizontal = resolvedHorizontalPadding, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IntroIllustration(
                page = page,
                modifier = Modifier.weight(1f),
            )
            IntroPageText(
                page = page,
                modifier = Modifier.fillMaxWidth().weight(1f),
            )
        }
    }
}

@Composable
private fun IntroIllustration(
    page: IntroPage,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier.fillMaxSize().sizeIn(maxWidth = 520.dp, maxHeight = 420.dp),
        painter = painterResource(page.illustration),
        contentDescription = null,
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun IntroPageText(
    page: IntroPage,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(page.headline),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Black,
            ),
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(page.body),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
