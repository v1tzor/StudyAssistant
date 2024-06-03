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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.TopAppBarButton
import views.TopAppBarTextButton
import views.TopAppBarTitle

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
internal fun ClassEditorTopBar(
    modifier: Modifier = Modifier,
    enabledSave: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column {
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Box(modifier = Modifier.fillMaxWidth().statusBarsPadding())
        }
        TopAppBar(
            modifier = modifier,
            title = {
                TopAppBarTitle(
                    header = EditorThemeRes.strings.classEditorHeader,
                    headerStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Normal,
                    )
                )
            },
            navigationIcon = {
                TopAppBarButton(
                    imageVector = Icons.Default.Close,
                    imageDescription = StudyAssistantRes.strings.backIconDesc,
                    onButtonClick = onBackClick,
                )
            },
            actions = {
                TopAppBarTextButton(
                    enabled = enabledSave,
                    text = EditorThemeRes.strings.saveButtonTitle,
                    onClick = onSaveClick,
                )
            },
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            elevation = 0.dp,
        )
    }
}