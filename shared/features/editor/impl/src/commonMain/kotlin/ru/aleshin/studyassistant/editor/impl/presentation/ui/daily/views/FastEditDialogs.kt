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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.NumberedDurationsList
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.StartOfClassesField

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun StartOfDayEditorDialog(
    modifier: Modifier = Modifier,
    startOfDay: Instant?,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit,
) {
    var editableTime by remember { mutableStateOf(startOfDay) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = EditorThemeRes.strings.startOfDayDialogHeader,
                    title = EditorThemeRes.strings.startOfDayDialogTitle,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StartOfClassesField(
                        modifier = Modifier.padding(top = 12.dp),
                        startOfClassTime = editableTime,
                        onChangeTime = { editableTime = it },
                    )
                }
                DialogButtons(
                    enabledConfirm = editableTime != null,
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = { editableTime?.let { onConfirm(it) } },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ClassesDurationEditorDialog(
    modifier: Modifier = Modifier,
    classesDurations: List<Pair<Int, Millis>>,
    onDismiss: () -> Unit,
    onConfirm: (Millis, List<NumberedDurationUi>) -> Unit,
) {
    val classesDurationsMap = remember(classesDurations) {
        classesDurations.groupBy { it.second }
    }
    val baseDuration = remember(classesDurationsMap) {
        classesDurationsMap.maxBy { it.value.size }.key
    }
    val specificDurations = remember(classesDurations, baseDuration) {
        classesDurations.filter { it.second != baseDuration }.map {
            NumberedDurationUi(it.first, it.second)
        }
    }

    var editableBaseDuration by remember { mutableStateOf<Millis?>(baseDuration) }
    val editableSpecificDurations = remember { mutableStateOf(specificDurations) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = EditorThemeRes.strings.classesDurationDialogHeader,
                    title = EditorThemeRes.strings.classesDurationDialogTitle,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NumberedDurationsList(
                        modifier = Modifier.padding(top = 12.dp),
                        baseDuration = editableBaseDuration,
                        specificDurations = editableSpecificDurations,
                        onChangeBaseDuration = { editableBaseDuration = it },
                        onChangeSpecificDurations = { editableSpecificDurations.value = it }
                    )
                }
                DialogButtons(
                    enabledConfirm = editableBaseDuration != null,
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        editableBaseDuration?.let { onConfirm(it, editableSpecificDurations.value) }
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun BreaksDurationEditorDialog(
    modifier: Modifier = Modifier,
    breaksDurations: List<Pair<Int, Millis>>,
    onDismiss: () -> Unit,
    onConfirm: (Millis, List<NumberedDurationUi>) -> Unit,
) {
    val breaksDurationsMap = remember(breaksDurations) {
        breaksDurations.groupBy { it.second }
    }
    val baseDuration = remember(breaksDurationsMap) {
        breaksDurationsMap.maxBy { it.value.size }.key
    }
    val specificDurations = remember(breaksDurations, baseDuration) {
        breaksDurations.filter { it.second != baseDuration }.map {
            NumberedDurationUi(it.first, it.second)
        }
    }

    var editableBaseDuration by remember { mutableStateOf<Millis?>(baseDuration) }
    val editableSpecificDurations = remember { mutableStateOf(specificDurations) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = EditorThemeRes.strings.breaksDurationDialogHeader,
                    title = EditorThemeRes.strings.breaksDurationDialogTitle,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NumberedDurationsList(
                        modifier = Modifier.padding(top = 12.dp),
                        baseDuration = editableBaseDuration,
                        specificDurations = editableSpecificDurations,
                        onChangeBaseDuration = { editableBaseDuration = it },
                        onChangeSpecificDurations = { editableSpecificDurations.value = it },
                        numberedContainerColor = MaterialTheme.colorScheme.tertiary,
                        durationContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    )
                }
                DialogButtons(
                    enabledConfirm = editableBaseDuration != null,
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        editableBaseDuration?.let { onConfirm(it, editableSpecificDurations.value) }
                    },
                )
            }
        }
    }
}