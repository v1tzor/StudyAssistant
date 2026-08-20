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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.functional.Constants.Class.MAX_NUMBER
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.presentation.models.organizations.NumberedDurationUi
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.material.endSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.startSide
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.core.ui.views.menu.BackMenuItem
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.ic_number
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_add_exception
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_all
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_duration
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_except
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_fast_edit_number
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.create_confirm_title as core_create_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline
import ru.aleshin.studyassistant.core.ui.resources.specify_title as core_specify_title

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
@Composable
internal fun ImportNumberedDurationsList(
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
        modifier = modifier.animateContentSize(spring()).heightIn(max = 252.dp)
            .verticalScroll(scrollState),
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
                    removeAll { item -> item.number == targetNumberedDuration.number }
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
@OptIn(ExperimentalMaterial3Api::class)
private fun NumberedDurationView(
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
                        onClick = { onNumberClick?.invoke() },
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
                        onClick = { onDurationClick?.invoke() },
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
                        text = stringResource(CoreRes.string.core_specify_title),
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
        number = stringResource(Res.string.schedule_import_fast_edit_all),
        duration = baseDuration,
        onNumberClick = null,
        onDurationClick = { durationPickerDialogState = true },
        onDelete = null,
        numberedContainerColor = numberedContainerColor,
        durationContainerColor = durationContainerColor,
    )

    if (durationPickerDialogState) {
        DurationPickerDialog(
            headerTitle = stringResource(Res.string.schedule_import_fast_edit_duration),
            duration = baseDuration,
            onDismiss = { durationPickerDialogState = false },
            onSelectedDuration = {
                onChangeBaseDuration(it)
                durationPickerDialogState = false
            },
        )
    }
}

@Composable
private fun SpecificNumberedDurationsSection(
    modifier: Modifier = Modifier,
    specificDurations: List<NumberedDurationUi>,
    numberedContainerColor: Color,
    durationContainerColor: Color,
    onDelete: (NumberedDurationUi) -> Unit,
    onChangeNumber: (Int, NumberedDurationUi) -> Unit,
    onChangeDuration: (Millis, NumberedDurationUi) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        specificDurations.sortedBy { duration -> duration.number }.forEach { numberedDuration ->
            key(numberedDuration.number) {
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
                        enabled = { number -> specificDurations.none { item -> item.number == number } },
                        currentNumber = numberedDuration.number,
                        onDismiss = { isOpenNumberChooserMenu = false },
                        onConfirm = { number ->
                            onChangeNumber(number, numberedDuration)
                            isOpenNumberChooserMenu = false
                        },
                    )

                    if (durationPickerDialogState) {
                        DurationPickerDialog(
                            headerTitle = stringResource(Res.string.schedule_import_fast_edit_duration),
                            duration = numberedDuration.duration,
                            onDismiss = { durationPickerDialogState = false },
                            onSelectedDuration = { duration ->
                                onChangeDuration(duration, numberedDuration)
                                durationPickerDialogState = false
                            },
                        )
                    }
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
                    text = stringResource(Res.string.schedule_import_fast_edit_add_exception),
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
                    val existingIndex = indexOfFirst { item -> item.number == numberedDuration.number }
                    if (existingIndex < 0) {
                        add(numberedDuration)
                    } else {
                        set(existingIndex, numberedDuration)
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
            text = stringResource(Res.string.schedule_import_fast_edit_except),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun NumberDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    enabled: (Int) -> Boolean,
    currentNumber: Int,
    numberRange: IntRange = 1..MAX_NUMBER,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.size(210.dp, 200.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        BackMenuItem(onClick = onDismiss)
        numberRange.forEach { number ->
            DropdownMenuItem(
                modifier = Modifier.alphaByEnabled(enabled(number)),
                onClick = { onConfirm(number) },
                enabled = enabled(number),
                text = {
                    Text(
                        text = stringResource(Res.string.schedule_import_fast_edit_number) + ": " + number,
                        color = if (number == currentNumber) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun NumberedDurationCreatorDropdownMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    specificDurations: List<NumberedDurationUi>,
    numberRange: IntRange = 1..15,
    onDismiss: () -> Unit,
    onCreate: (NumberedDurationUi) -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier.size(230.dp, 200.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        var isOpenDurationPickerDialog by remember { mutableStateOf(false) }
        var number by remember { mutableStateOf<Int?>(null) }
        var duration by remember { mutableStateOf<Millis?>(null) }
        var page by remember { mutableStateOf(NumberedDurationCreatorPage.MAIN) }

        when (page) {
            NumberedDurationCreatorPage.MAIN -> {
                BackMenuItem(onClick = onDismiss)
                DropdownMenuItem(
                    onClick = { page = NumberedDurationCreatorPage.NUMBER },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_number),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.schedule_import_fast_edit_number),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingIcon = {
                        if (number != null) {
                            Text(
                                text = number?.toString().orEmpty(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
                DropdownMenuItem(
                    onClick = { isOpenDurationPickerDialog = true },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(CoreRes.drawable.core_ic_clock_outline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(Res.string.schedule_import_fast_edit_duration),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingIcon = {
                        if (duration != null) {
                            Text(
                                text = (duration ?: 0).toMinutesOrHoursTitle(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
                val createEnabled = number != null && duration != null
                DropdownMenuItem(
                    enabled = createEnabled,
                    modifier = Modifier.alphaByEnabled(createEnabled),
                    onClick = {
                        val selectedNumber = number
                        val selectedDuration = duration
                        if (selectedNumber != null && selectedDuration != null) {
                            onCreate(NumberedDurationUi(selectedNumber, selectedDuration))
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(CoreRes.string.core_create_confirm_title),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
            NumberedDurationCreatorPage.NUMBER -> {
                BackMenuItem(onClick = { page = NumberedDurationCreatorPage.MAIN })
                numberRange.forEach { numberItem ->
                    val enabled = remember(specificDurations, numberItem) {
                        specificDurations.none { item -> item.number == numberItem }
                    }
                    DropdownMenuItem(
                        modifier = Modifier.alphaByEnabled(enabled),
                        enabled = enabled,
                        onClick = {
                            number = numberItem
                            page = NumberedDurationCreatorPage.MAIN
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.schedule_import_fast_edit_number) + ": " + numberItem,
                                color = if (number == numberItem) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        },
                    )
                }
            }
        }

        if (isOpenDurationPickerDialog) {
            DurationPickerDialog(
                headerTitle = stringResource(Res.string.schedule_import_fast_edit_duration),
                duration = duration,
                onDismiss = { isOpenDurationPickerDialog = false },
                onSelectedDuration = { selected ->
                    duration = selected
                    isOpenDurationPickerDialog = false
                },
            )
        }
    }
}

private enum class NumberedDurationCreatorPage {
    MAIN, NUMBER
}
