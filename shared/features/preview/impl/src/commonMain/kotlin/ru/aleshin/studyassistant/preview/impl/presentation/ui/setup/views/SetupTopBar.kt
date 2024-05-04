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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.annotation.FloatRange
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import theme.StudyAssistantRes
import views.TopAppBarButton
import views.TopAppBarEmptyButton

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
internal fun SetupTopBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    onBackPressed: () -> Unit,
    @FloatRange(0.0, 1.0) stepProgress: Float,
) {
    val progress by animateFloatAsState(
        targetValue = stepProgress,
        animationSpec = tween(320)
    )
    TopAppBar(
        modifier = modifier,
        title = {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                strokeCap = StrokeCap.Round,
            )
        },
        navigationIcon = {
            TopAppBarButton(
                enabled = enabled,
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                imageDescription = StudyAssistantRes.strings.backIconDesc,
                onButtonClick = onBackPressed,
            )
        },
        actions = {
            TopAppBarEmptyButton()
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 0.dp,
    )
}