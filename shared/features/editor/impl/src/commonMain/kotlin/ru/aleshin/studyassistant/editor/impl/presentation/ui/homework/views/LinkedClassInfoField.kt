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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.schedules.ClassUi
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassesForLinkedMapUi
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.homework_date_field_label
import ru.aleshin.studyassistant.editor.impl.resources.homework_date_field_placeholder
import ru.aleshin.studyassistant.editor.impl.resources.homework_date_picker_headline
import ru.aleshin.studyassistant.editor.impl.resources.link_class_view_empty_title
import ru.aleshin.studyassistant.editor.impl.resources.link_class_view_title
import ru.aleshin.studyassistant.editor.impl.resources.number_of_class_suffix
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title
import ru.aleshin.studyassistant.core.ui.resources.date_picker_dialog_header as core_date_picker_dialog_header
import ru.aleshin.studyassistant.core.ui.resources.ic_calendar_today as core_ic_calendar_today
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date
import ru.aleshin.studyassistant.core.ui.resources.none_title as core_none_title
import ru.aleshin.studyassistant.core.ui.resources.select_confirm_title as core_select_confirm_title

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun LinkedClassInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    selectedDate: Instant?,
    linkedClass: UID?,
    classesForLinked: ClassesForLinkedMapUi,
    onSelectedDate: (Instant?) -> Unit,
    onSelectedClass: (ClassUi?, Instant?) -> Unit,
) {
    var datePickerState by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(CoreRes.drawable.core_ic_calendar_today),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ClickableTextField(
                onClick = { datePickerState = true },
                enabled = !isLoading,
                value = selectedDate?.formatByTimeZone(
                    format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat()
                ),
                label = stringResource(Res.string.homework_date_field_label),
                placeholder = stringResource(Res.string.homework_date_field_placeholder),
                trailingIcon = {
                    Icon(
                        painter = painterResource(CoreRes.drawable.core_ic_select_date),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            )
            LinkClassView(
                isLoading = isLoading,
                currentDate = currentDate,
                selectedDate = selectedDate,
                linkedClass = linkedClass,
                classesForLinked = classesForLinked,
                onSelectedClass = onSelectedClass,
            )
        }
    }
    if (datePickerState) {
        HomeworkDatePicker(
            onDismiss = { datePickerState = false },
            onSelectedDate = { date ->
                onSelectedDate(date)
                datePickerState = false
            }
        )
    }
}

@Composable
private fun LinkClassView(
    modifier: Modifier = Modifier,
    gridState: LazyGridState = rememberLazyGridState(),
    isLoading: Boolean,
    currentDate: Instant,
    selectedDate: Instant?,
    linkedClass: UID?,
    classesForLinked: ClassesForLinkedMapUi,
    onSelectedClass: (ClassUi?, Instant?) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.animateContentSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.link_class_view_title),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
            Crossfade(
                targetState = if (isLoading) null else classesForLinked,
                animationSpec = floatSpring(),
            ) { linkClasses ->
                if (linkClasses == null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp,
                        )
                    }
                } else if (linkClasses.isNotEmpty()) {
                    val classes = remember(linkClasses) {
                        buildList {
                            linkClasses.forEach { entry ->
                                addAll(entry.value.map { Pair(entry.key, it) })
                            }
                        }.sortedBy {
                            it.first
                        }
                    }
                    val nextClass = remember(classes, currentDate) {
                        classes.find {
                            currentDate.daysUntil(it.first, TimeZone.currentSystemDefault()) >= 1
                        }
                    }
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            NoneLinkClassItem(
                                onClick = { onSelectedClass(null, null) },
                                selected = linkedClass == null,
                            )
                        }
                        items(
                            classes,
                            key = { it.first.toString() + it.second.uid }) { dateClassModel ->
                            LinkClassItem(
                                onClick = {
                                    onSelectedClass(
                                        dateClassModel.second,
                                        dateClassModel.first
                                    )
                                },
                                selected = linkedClass == dateClassModel.second.uid &&
                                        dateClassModel.first.equalsDay(selectedDate),
                                isNext = nextClass == dateClassModel,
                                date = dateClassModel.first,
                                numberOfClass = dateClassModel.second.number,
                            )
                        }
                    }

                    LaunchedEffect(classes, nextClass) {
                        if (linkedClass == null && nextClass != null && selectedDate == null) {
                            onSelectedClass(nextClass.second, nextClass.first)
                        }
                    }
                } else {
                    Text(
                        text = stringResource(Res.string.link_class_view_empty_title),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkClassItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
    isNext: Boolean,
    date: Instant,
    numberOfClass: Int,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(110.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else if (isNext) {
            StudyAssistantRes.colors.accents.orangeContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                text = date.formatByTimeZone(
                    format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat()
                ),
                color = if (isNext) {
                    StudyAssistantRes.colors.accents.orange
                } else {
                    MaterialTheme.colorScheme.primary
                },
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = buildString {
                    append(numberOfClass, " ")
                    append(stringResource(Res.string.number_of_class_suffix))
                },
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else if (isNext) {
                    StudyAssistantRes.colors.accents.onOrangeContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun NoneLinkClassItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(100.dp, 44.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(CoreRes.string.core_none_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeworkDatePicker(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSelectedDate: (Instant?) -> Unit,
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val birthday = selectedDate.mapEpochTimeToInstant()
                    onSelectedDate.invoke(birthday)
                },
                content = { Text(text = stringResource(CoreRes.string.core_select_confirm_title)) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(CoreRes.string.core_cancel_title))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = stringResource(CoreRes.string.core_date_picker_dialog_header),
                )
            },
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = stringResource(Res.string.homework_date_picker_headline),
                )
            },
        )
    }
}