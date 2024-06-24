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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import extensions.formatByTimeZone
import extensions.mapEpochTimeToInstant
import functional.UID
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassesForLinkedUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import theme.tokens.dayOfWeekShortNames
import theme.tokens.monthNames
import views.ClickableTextField

/**
 * @author Stanislav Aleshin on 23.06.2024.
 */
@Composable
internal fun LinkedClassInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    date: Instant?,
    linkedClass: UID?,
    classesForLinked: ClassesForLinkedUi,
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
                painter = painterResource(StudyAssistantRes.icons.calendarToday),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val strings = StudyAssistantRes.strings
            val dateFormat = DateTimeComponents.Format {
                dayOfWeek(strings.dayOfWeekShortNames())
                chars(", ")
                monthName(strings.monthNames())
                char(' ')
                dayOfMonth(Padding.NONE)
            }
            ClickableTextField(
                onClick = { datePickerState = true },
                enabled = !isLoading,
                value = date?.formatByTimeZone(dateFormat),
                label = EditorThemeRes.strings.homeworkDateFieldLabel,
                placeholder = EditorThemeRes.strings.homeworkDateFieldPlaceholder,
                trailingIcon = {
                    Icon(
                        painter = painterResource(StudyAssistantRes.icons.selectDate),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            )
            LinkClassView(
                isLoading = isLoading,
                linkedClass = linkedClass,
                classesForLinked = classesForLinked,
                onSelectedClass = onSelectedClass,
            )
        }
    }
    if (datePickerState) {
        HomeworkDatePicker(
            onDismiss = { datePickerState = false },
            onSelectedDate = { selectedDate ->
                onSelectedDate(selectedDate)
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
    linkedClass: UID?,
    classesForLinked: ClassesForLinkedUi,
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
                text = EditorThemeRes.strings.linkClassViewTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
            )
            Crossfade(
                targetState = classesForLinked,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) { linkClasses ->
                if (isLoading) {
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
                    val classes = buildList {
                        linkClasses.forEach { entry -> addAll(entry.value.map { Pair(entry.key, it) }) }
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
                        items(classes) { dateClassModel ->
                            LinkClassItem(
                                onClick = { onSelectedClass(dateClassModel.second, dateClassModel.first) },
                                selected = linkedClass == dateClassModel.second.uid,
                                date = dateClassModel.first,
                                numberOfClass = dateClassModel.second.number,
                            )
                        }
                    }
                } else {
                    Text(
                        text = EditorThemeRes.strings.linkClassViewEmptyTitle,
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
    date: Instant,
    numberOfClass: Int,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(100.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            val strings = StudyAssistantRes.strings
            val dateFormat = DateTimeComponents.Format {
                dayOfWeek(strings.dayOfWeekShortNames())
                chars(", ")
                monthName(strings.monthNames())
                char(' ')
                dayOfMonth(Padding.NONE)
            }
            Text(
                text = date.formatByTimeZone(dateFormat),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = buildString {
                    append(numberOfClass, " ")
                    append(EditorThemeRes.strings.numberOfClassSuffix)
                },
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
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
                text = StudyAssistantRes.strings.noneTitle,
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
                content = { Text(text = StudyAssistantRes.strings.selectConfirmTitle) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = StudyAssistantRes.strings.cancelTitle)
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = StudyAssistantRes.strings.datePickerDialogHeader,
                )
            },
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = EditorThemeRes.strings.homeworkDatePickerHeadline,
                )
            },
        )
    }
}