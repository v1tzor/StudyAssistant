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

import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SegmentedButtons
import ru.aleshin.studyassistant.core.ui.views.StickyBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseWeekScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.NumberOfWeekItem
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.toItem
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun WeekScheduleBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    layoutHeight: Int,
    isLoading: Boolean,
    weekSchedule: BaseWeekScheduleUi?,
    maxNumberOfWeek: NumberOfRepeatWeek?,
    selectedWeek: NumberOfRepeatWeek,
    organizations: List<OrganizationShortUi>,
    onSelectedWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateOrganization: (OrganizationShortUi) -> Unit,
    onAddOrganization: () -> Unit,
    onSaveClick: () -> Unit,
) {
    StickyBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        layoutHeight = layoutHeight,
        header = {
            WeekScheduleBottomSheetHeader(
                isLoading = isLoading,
                numberOfClasses = weekSchedule?.weekDaySchedules?.values?.sumOf { schedule ->
                    schedule.classes.size
                } ?: 0,
                maxNumberOfWeek = maxNumberOfWeek,
                selectedWeek = selectedWeek,
                onSelectedWeek = onSelectedWeek,
            )
        },
        expandedContent = {
            WeekScheduleBottomSheetContent(
                isLoading = isLoading,
                organizations = organizations,
                onAddOrganization = onAddOrganization,
                onUpdateOrganization = onUpdateOrganization,
            )
        },
        footer = { paddingValues ->
            WeekScheduleBottomSheetFooter(
                modifier = Modifier.padding(paddingValues),
                onSaveClick = onSaveClick,
            )
        },
    )
}

@Composable
private fun WeekScheduleBottomSheetHeader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    numberOfClasses: Int,
    maxNumberOfWeek: NumberOfRepeatWeek?,
    selectedWeek: NumberOfRepeatWeek,
    onSelectedWeek: (NumberOfRepeatWeek) -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = EditorThemeRes.strings.numberOfClassesLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
            Crossfade(
                targetState = isLoading,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) { loading ->
                if (!loading) {
                    Text(
                        text = "$numberOfClasses ${StudyAssistantRes.strings.pcsUnitSuffix}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                } else {
                    PlaceholderBox(
                        modifier = Modifier.size(55.dp, 22.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }
        }
        SegmentedButtons(
            modifier = Modifier.width(180.dp),
            enabled = { it.isoWeekNumber <= (maxNumberOfWeek?.toItem()?.isoWeekNumber ?: -1) },
            items = NumberOfWeekItem.entries.toTypedArray(),
            selectedItem = selectedWeek.toItem(),
            onItemClick = { onSelectedWeek(it.toModel()) },
        )
    }
}

@Composable
private fun WeekScheduleBottomSheetContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizations: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onUpdateOrganization: (OrganizationShortUi) -> Unit,
) {
    var dialogOrganization by remember { mutableStateOf<OrganizationShortUi?>(null) }
    var scheduleIntervalsDialogState by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = EditorThemeRes.strings.standardTimeIntervalHeader,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessLow),
        ) { loading ->
            LazyRow(
                modifier = Modifier.height(126.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (loading) {
                    items(3) {
                        ScheduleTimeIntervalsItemPlaceholder()
                    }
                } else if (organizations.isNotEmpty()) {
                    items(organizations, key = { it.uid }) { organization ->
                        ScheduleTimeIntervalsItem(
                            organization = organization.shortName,
                            intervals = organization.scheduleTimeIntervals,
                            onClick = {
                                dialogOrganization = organization
                                scheduleIntervalsDialogState = true
                            },
                        )
                    }
                } else {
                    item {
                        ScheduleTimeIntervalsAddItem(onAdd = onAddOrganization)
                    }
                }
            }
        }
    }

    if (scheduleIntervalsDialogState && dialogOrganization != null) {
        ScheduleIntervalsDialog(
            organization = dialogOrganization!!.shortName,
            intervals = dialogOrganization!!.scheduleTimeIntervals,
            onDismiss = { scheduleIntervalsDialogState = false },
            onConfirm = {
                onUpdateOrganization(dialogOrganization!!.copy(scheduleTimeIntervals = it))
                scheduleIntervalsDialogState = false
            },
        )
    }
}

@Composable
private fun WeekScheduleBottomSheetFooter(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onSaveClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(1f),
        ) {
            Text(text = EditorThemeRes.strings.saveButtonTitle)
        }
    }
}

@Composable
private fun ScheduleTimeIntervalsItem(
    modifier: Modifier = Modifier,
    organization: String,
    intervals: ScheduleTimeIntervalsUi,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(StudyAssistantRes.icons.organizationGeo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = organization,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(StudyAssistantRes.icons.classes),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Column {
                        val minDuration = intervals.minClassDuration()
                        val maxDuration = intervals.maxClassDuration()
                        Text(
                            text = EditorThemeRes.strings.classesTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(
                                    minDuration?.toMinutesOrHoursTitle()
                                        ?: StudyAssistantRes.strings.noneTitle
                                )
                                if (maxDuration != null) append(" - " + maxDuration.toMinutesOrHoursTitle())
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(EditorThemeRes.icons.breaks),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Column {
                        val minDuration = intervals.minBreakDuration()
                        val maxDuration = intervals.maxBreakDuration()
                        Text(
                            text = EditorThemeRes.strings.breaksTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = buildAnnotatedString {
                                append(
                                    minDuration?.toMinutesOrHoursTitle()
                                        ?: StudyAssistantRes.strings.noneTitle
                                )
                                if (maxDuration != null) append(" - " + maxDuration.toMinutesOrHoursTitle())
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimeIntervalsAddItem(
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
) {
    Surface(
        onClick = onAdd,
        modifier = modifier.size(212.dp, 126.dp),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = EditorThemeRes.strings.addTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScheduleTimeIntervalsItemPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.size(200.dp, 126.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    )
}