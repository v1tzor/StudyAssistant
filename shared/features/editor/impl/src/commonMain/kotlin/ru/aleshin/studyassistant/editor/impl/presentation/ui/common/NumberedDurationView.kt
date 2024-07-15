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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.endSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.startSide
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.NumberDropdownMenu
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views.NumberedDurationCreatorDropdownMenu

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun NumberedDurationView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    number: String,
    duration: Millis?,
    onNumberClick: (() -> Unit)?,
    onDurationClick: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            when (dismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete?.invoke()
                    true
                }

                SwipeToDismissBoxValue.StartToEnd -> false
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * .50f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                endToStartContent = {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                endToStartColor = MaterialTheme.colorScheme.errorContainer,
            )
        },
        enableDismissFromEndToStart = onDelete != null,
        enableDismissFromStartToEnd = false,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .animateContentSize()
                    .height(32.dp)
                    .clip(MaterialTheme.shapes.full.startSide)
                    .background(numberedContainerColor)
                    .clickable(
                        enabled = enabled && onNumberClick != null,
                        onClick = { if (onNumberClick != null) onNumberClick() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = number,
                    color = MaterialTheme.colorScheme.contentColorFor(numberedContainerColor),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .weight(1f)
                    .clip(MaterialTheme.shapes.full.endSide)
                    .background(durationContainerColor)
                    .clickable(
                        enabled = enabled && onDurationClick != null,
                        onClick = { if (onDurationClick != null) onDurationClick() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (duration != null) {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = duration.toMinutesOrHoursTitle(),
                        color = MaterialTheme.colorScheme.contentColorFor(durationContainerColor),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = StudyAssistantRes.strings.specifyTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Composable
internal fun NumberedDurationsList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseDuration: Millis?,
    specificDurations: State<List<NumberedDurationUi>>,
    onChangeBaseDuration: (Millis?) -> Unit,
    onChangeSpecificDurations: (List<NumberedDurationUi>) -> Unit,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Column(
        modifier = modifier.animateContentSize(spring()).heightIn(max = 252.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AllNumberedDurationView(
            baseDuration = baseDuration,
            numberedContainerColor = numberedContainerColor,
            durationContainerColor = durationContainerColor,
            onChangeBaseDuration = onChangeBaseDuration,
        )
        ExceptLine()
        SpecificNumberedDurationsSection(
            specificDurations = specificDurations.value,
            numberedContainerColor = numberedContainerColor,
            durationContainerColor = durationContainerColor,
            onDelete = { targetNumberedDuration ->
                val updatedDurations = specificDurations.value.toMutableList().apply {
                    removeAll { it.number == targetNumberedDuration.number }
                }
                onChangeSpecificDurations(updatedDurations)
            },
            onChangeNumber = { number, numberedDuration ->
                val updatedDurations = specificDurations.value.toMutableList().apply {
                    set(indexOf(numberedDuration), numberedDuration.copy(number = number))
                }
                onChangeSpecificDurations(updatedDurations)
            },
            onChangeDuration = { duration, numberedDuration ->
                val updatedDurations = specificDurations.value.toMutableList().apply {
                    set(indexOf(numberedDuration), numberedDuration.copy(duration = duration))
                }
                onChangeSpecificDurations(updatedDurations)
            },
        )
        AddNumberedDurationView(
            specificDurations = specificDurations.value,
            onChangeSpecificDurations = onChangeSpecificDurations,
        )
    }
}

@Composable
private fun AllNumberedDurationView(
    modifier: Modifier = Modifier,
    baseDuration: Millis?,
    numberedContainerColor: Color,
    durationContainerColor: Color,
    onChangeBaseDuration: (Millis?) -> Unit,
) {
    var durationPickerDialogState by remember { mutableStateOf(false) }

    NumberedDurationView(
        modifier = modifier,
        number = EditorThemeRes.strings.allTitle,
        duration = baseDuration,
        onNumberClick = null,
        onDurationClick = { durationPickerDialogState = true },
        onDelete = null,
        numberedContainerColor = numberedContainerColor,
        durationContainerColor = durationContainerColor,
    )

    if (durationPickerDialogState) {
        DurationPickerDialog(
            headerTitle = EditorThemeRes.strings.durationTitle,
            duration = baseDuration,
            onDismiss = { durationPickerDialogState = false },
            onSelectedDuration = {
                onChangeBaseDuration(it)
                durationPickerDialogState = false
            }
        )
    }
}

@Composable
private fun SpecificNumberedDurationsSection(
    modifier: Modifier = Modifier,
    specificDurations: List<NumberedDurationUi>,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onDelete: (NumberedDurationUi) -> Unit,
    onChangeNumber: (Int, NumberedDurationUi) -> Unit,
    onChangeDuration: (Millis, NumberedDurationUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        specificDurations.sortedBy { it.number }.forEach { numberedDuration ->
            Column {
                var isOpenNumberChooserMenu by remember { mutableStateOf(false) }
                var durationPickerDialogState by remember { mutableStateOf(false) }

                NumberedDurationView(
                    number = numberedDuration.number.toString(),
                    duration = numberedDuration.duration,
                    onNumberClick = { isOpenNumberChooserMenu = true },
                    onDurationClick = { durationPickerDialogState = true },
                    onDelete = { onDelete(numberedDuration) },
                    numberedContainerColor = numberedContainerColor,
                    durationContainerColor = durationContainerColor,
                )

                NumberDropdownMenu(
                    expanded = isOpenNumberChooserMenu,
                    enabled = { number -> specificDurations.find { it.number == number } == null },
                    currentNumber = numberedDuration.number,
                    onDismiss = { isOpenNumberChooserMenu = false },
                    onConfirm = {
                        onChangeNumber(it, numberedDuration)
                        isOpenNumberChooserMenu = false
                    },
                )

                if (durationPickerDialogState) {
                    DurationPickerDialog(
                        headerTitle = EditorThemeRes.strings.durationTitle,
                        duration = numberedDuration.duration,
                        onDismiss = { durationPickerDialogState = false },
                        onSelectedDuration = {
                            onChangeDuration(it, numberedDuration)
                            durationPickerDialogState = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNumberedDurationView(
    modifier: Modifier = Modifier,
    specificDurations: List<NumberedDurationUi>,
    onChangeSpecificDurations: (List<NumberedDurationUi>) -> Unit,
) {
    var isOpenNumberedDurationCreatorMenu by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Surface(
            onClick = { isOpenNumberedDurationCreatorMenu = true },
            modifier = Modifier.fillMaxWidth().height(28.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = EditorThemeRes.strings.addTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        NumberedDurationCreatorDropdownMenu(
            expanded = isOpenNumberedDurationCreatorMenu,
            specificDurations = specificDurations,
            onDismiss = { isOpenNumberedDurationCreatorMenu = false },
            onCreate = { numberedDuration ->
                val updatedDurations = specificDurations.toMutableList().apply {
                    if (specificDurations.find { it.number == numberedDuration.number } == null) {
                        add(numberedDuration)
                    } else {
                        set(
                            indexOfFirst { it.number == numberedDuration.number },
                            numberedDuration
                        )
                    }
                }
                onChangeSpecificDurations(updatedDurations)
                isOpenNumberedDurationCreatorMenu = false
            },
        )
    }
}

@Composable
private fun ExceptLine(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 4.dp),
) {
    Row(
        modifier = modifier.padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = EditorThemeRes.strings.exceptTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}