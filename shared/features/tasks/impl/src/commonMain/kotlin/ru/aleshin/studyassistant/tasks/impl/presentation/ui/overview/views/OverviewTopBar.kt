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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.open_analytics_desc
import ru.aleshin.studyassistant.tasks.impl.resources.overview_header
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_analytics_details as core_ic_analytics_details

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OverviewTopBar(
    modifier: Modifier = Modifier,
    onAnalyticsClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(Res.string.overview_header)) },
        actions = {
            IconButton(onClick = onAnalyticsClick) {
                Icon(
                    painter = painterResource(CoreRes.drawable.core_ic_analytics_details),
                    contentDescription = stringResource(Res.string.open_analytics_desc),
                    modifier = Modifier.size(24.dp),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OverviewExpandedTopBar(
    modifier: Modifier = Modifier,
    onAnalyticsClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                header = stringResource(Res.string.overview_header),
                textAlign = TextAlign.Start,
            )
        },
        actions = {
            TopAppBarButton(
                imagePainter = painterResource(CoreRes.drawable.core_ic_analytics_details),
                imageDescription = stringResource(Res.string.open_analytics_desc),
                onButtonClick = onAnalyticsClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
