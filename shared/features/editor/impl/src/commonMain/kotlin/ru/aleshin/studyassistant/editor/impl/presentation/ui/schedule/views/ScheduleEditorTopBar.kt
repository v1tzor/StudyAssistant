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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views

import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import views.TopAppBarEmptyButton
import views.TopAppBarTitle

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun ScheduleEditorTopBar(
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                header = EditorThemeRes.strings.scheduleEditorHeader,
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            TopAppBarEmptyButton()
        },
        actions = {
            TopAppBarEmptyButton()
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 0.dp,
    )
}