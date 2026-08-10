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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.ic_list_edit
import ru.aleshin.studyassistant.schedule.impl.resources.ic_open_table
import ru.aleshin.studyassistant.schedule.impl.resources.overview_header
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_calendar_today as core_ic_calendar_today

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OverviewTopBar(
    modifier: Modifier = Modifier,
    enabledEdit: Boolean,
    onEditClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onCurrentDay: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                header = stringResource(Res.string.overview_header),
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            TopAppBarButton(
                enabled = enabledEdit,
                imagePainter = painterResource(Res.drawable.ic_list_edit),
                imageDescription = null,
                onButtonClick = onEditClick,
            )
        },
        actions = {
            Row {
                TopAppBarButton(
                    imagePainter = painterResource(CoreRes.drawable.core_ic_calendar_today),
                    imageDescription = null,
                    onButtonClick = onCurrentDay,
                )
                TopAppBarButton(
                    imagePainter = painterResource(Res.drawable.ic_open_table),
                    imageDescription = null,
                    onButtonClick = onDetailsClick,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
}
