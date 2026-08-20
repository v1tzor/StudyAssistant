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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.presentation.models.organizations.NumberedDurationUi
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ImportFastEditDurationMath
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_breaks_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_breaks_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_classes_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_classes_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start_field
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_start_title
import kotlin.time.Instant
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline
import ru.aleshin.studyassistant.core.ui.resources.save_confirm_title as core_save_confirm_title

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportStartOfDayEditorDialog(
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
                    header = stringResource(Res.string.schedule_import_fast_edit_start_header),
                    title = stringResource(Res.string.schedule_import_fast_edit_start_title),
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ImportStartOfClassesField(
                        modifier = Modifier.padding(top = 12.dp),
                        startOfClassTime = editableTime,
                        onChangeTime = { editableTime = it },
                    )
                }
                DialogButtons(
                    enabledConfirm = editableTime != null,
                    confirmTitle = stringResource(CoreRes.string.core_save_confirm_title),
                    onCancelClick = onDismiss,
                    onConfirmClick = { editableTime?.let(onConfirm) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportClassesDurationEditorDialog(
    modifier: Modifier = Modifier,
    classesDurations: List<Pair<Int, Millis>>,
    onDismiss: () -> Unit,
    onConfirm: (Millis, List<NumberedDurationUi>) -> Unit,
) {
    val groupedDurations = remember(classesDurations) {
        ImportFastEditDurationMath.groupedDurations(classesDurations)
    }
    var editableBaseDuration by remember { mutableStateOf(groupedDurations.first) }
    val editableSpecificDurations = remember { mutableStateOf(groupedDurations.second) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = stringResource(Res.string.schedule_import_fast_edit_classes_header),
                    title = stringResource(Res.string.schedule_import_fast_edit_classes_title),
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ImportNumberedDurationsList(
                        modifier = Modifier.padding(top = 12.dp),
                        baseDuration = editableBaseDuration,
                        specificDurations = editableSpecificDurations,
                        onChangeBaseDuration = { editableBaseDuration = it },
                        onChangeSpecificDurations = { editableSpecificDurations.value = it },
                    )
                }
                DialogButtons(
                    enabledConfirm = editableBaseDuration != null,
                    confirmTitle = stringResource(CoreRes.string.core_save_confirm_title),
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        editableBaseDuration?.let { duration ->
                            onConfirm(duration, editableSpecificDurations.value)
                        }
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportBreaksDurationEditorDialog(
    modifier: Modifier = Modifier,
    breaksDurations: List<Pair<Int, Millis>>,
    onDismiss: () -> Unit,
    onConfirm: (Millis, List<NumberedDurationUi>) -> Unit,
) {
    val groupedDurations = remember(breaksDurations) {
        ImportFastEditDurationMath.groupedDurations(breaksDurations)
    }
    var editableBaseDuration by remember { mutableStateOf(groupedDurations.first) }
    val editableSpecificDurations = remember { mutableStateOf(groupedDurations.second) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = stringResource(Res.string.schedule_import_fast_edit_breaks_header),
                    title = stringResource(Res.string.schedule_import_fast_edit_breaks_title),
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ImportNumberedDurationsList(
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
                    confirmTitle = stringResource(CoreRes.string.core_save_confirm_title),
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        editableBaseDuration?.let { duration ->
                            onConfirm(duration, editableSpecificDurations.value)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ImportStartOfClassesField(
    modifier: Modifier = Modifier,
    startOfClassTime: Instant?,
    onChangeTime: (Instant?) -> Unit,
) {
    var isOpenTimePickerDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ClickableTextField(
            onClick = { isOpenTimePickerDialog = true },
            value = startOfClassTime?.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            label = stringResource(Res.string.schedule_import_fast_edit_start_field),
            placeholder = stringResource(Res.string.schedule_import_fast_edit_start_placeholder),
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_clock_outline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        )

        if (isOpenTimePickerDialog) {
            TimePickerDialog(
                initTime = startOfClassTime,
                onDismiss = { isOpenTimePickerDialog = false },
                onConfirmTime = { instant ->
                    onChangeTime(instant)
                    isOpenTimePickerDialog = false
                },
            )
        }
    }
}
