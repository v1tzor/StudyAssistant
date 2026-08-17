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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.resources.ic_document_scanner
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.schedule_import_open_description
import ru.aleshin.studyassistant.editor.impl.resources.week_schedule_editor_header
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun WeekScheduleTopBar(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onImportClick: () -> Unit,
) {
    val actions: @Composable () -> Unit = {
        TopAppBarButton(
            imagePainter = painterResource(CoreRes.drawable.ic_document_scanner),
            imageDescription = stringResource(Res.string.schedule_import_open_description),
            onButtonClick = onImportClick,
        )
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
    )
    if (isExpanded) {
        TopAppBar(
            modifier = modifier,
            title = {
                TopAppBarTitle(
                    header = stringResource(Res.string.week_schedule_editor_header),
                    textAlign = TextAlign.Start,
                )
            },
            actions = { actions() },
            colors = colors,
        )
    } else {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = { Text(text = stringResource(Res.string.week_schedule_editor_header)) },
            colors = colors,
            actions = { actions() },
        )
    }
}