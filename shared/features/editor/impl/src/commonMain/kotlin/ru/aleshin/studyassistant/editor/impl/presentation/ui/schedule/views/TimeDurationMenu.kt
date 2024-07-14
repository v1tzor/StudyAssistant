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

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.functional.Constants.Class.MAX_NUMBER
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.core.ui.views.menu.BackMenuItem
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun NumberDropdownMenu(
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
                        text = EditorThemeRes.strings.numberOfClassTitle + ": " + number,
                        color = when (number == currentNumber) {
                            true -> MaterialTheme.colorScheme.primary
                            false -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                },
            )
        }
    }
}

@Composable
internal fun NumberedDurationCreatorDropdownMenu(
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
                            painter = painterResource(EditorThemeRes.icons.number),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    text = {
                        Text(
                            text = EditorThemeRes.strings.numberOfClassTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    trailingIcon = {
                        if (number != null) {
                            Text(
                                text = number?.toString() ?: "",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                )
                DropdownMenuItem(
                    onClick = { isOpenDurationPickerDialog = true },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(StudyAssistantRes.icons.timeOutline),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    text = {
                        Text(
                            text = EditorThemeRes.strings.durationTitle,
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
                    }
                )
                val createEnabled = number != null && duration != null
                DropdownMenuItem(
                    enabled = createEnabled,
                    modifier = Modifier.alphaByEnabled(createEnabled),
                    onClick = {
                        if (createEnabled) {
                            onCreate(NumberedDurationUi(number!!, duration!!))
                        }
                    },
                    text = {
                        Text(
                            text = StudyAssistantRes.strings.createConfirmTitle,
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
                    val enabled = specificDurations.find { it.number == numberItem } == null
                    DropdownMenuItem(
                        modifier = Modifier.alphaByEnabled(enabled),
                        enabled = enabled,
                        onClick = {
                            number = numberItem
                            page = NumberedDurationCreatorPage.MAIN
                        },
                        text = {
                            Text(
                                text = EditorThemeRes.strings.numberOfClassTitle + ": " + numberItem,
                                color = when (number == numberItem) {
                                    true -> MaterialTheme.colorScheme.primary
                                    false -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        },
                    )
                }
            }
        }

        if (isOpenDurationPickerDialog) {
            DurationPickerDialog(
                headerTitle = EditorThemeRes.strings.durationTitle,
                duration = duration,
                onDismiss = { isOpenDurationPickerDialog = false },
                onSelectedDuration = {
                    duration = it
                    isOpenDurationPickerDialog = false
                }
            )
        }
    }
}

internal enum class NumberedDurationCreatorPage {
    MAIN, NUMBER
}