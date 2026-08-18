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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.CircularStepsRow
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.back_label
import ru.aleshin.studyassistant.preview.impl.resources.continue_label
import ru.aleshin.studyassistant.preview.impl.resources.setup_label

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
@Composable
internal fun IntroStepsSection(
    modifier: Modifier = Modifier,
    stepsCount: Int,
    currentStep: Int,
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularStepsRow(
            stepsCount = stepsCount,
            currentStep = currentStep,
        )
    }
}

@Composable
internal fun IntroNavigationSection(
    isFirstPage: Boolean,
    isLastPage: Boolean,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(
            modifier = Modifier.weight(1f),
            onClick = onBackClick,
            enabled = !isFirstPage,
        ) {
            Text(text = stringResource(Res.string.back_label))
        }
        Button(
            modifier = Modifier.weight(1f),
            onClick = if (isLastPage) onSetupClick else onContinueClick,
        ) {
            Text(
                text = stringResource(
                    if (isLastPage) Res.string.setup_label else Res.string.continue_label,
                ),
            )
        }
    }
}
