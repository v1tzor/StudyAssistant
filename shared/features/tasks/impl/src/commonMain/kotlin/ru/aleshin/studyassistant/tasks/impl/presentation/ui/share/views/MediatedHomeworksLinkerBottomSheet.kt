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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.views.MediumDragHandle
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.NumberedClassUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 20.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MediatedHomeworksLinkerBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isLoading: Boolean,
    organizations: List<OrganizationShortUi>,
    linkDataList: List<MediatedHomeworkLinkData>,
    linkSchedule: ScheduleUi?,
    linkSubjects: List<SubjectUi>,
    onDismissRequest: () -> Unit,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onUpdateLinkData: (MediatedHomeworkLinkData) -> Unit,
    onAdd: (List<MediatedHomeworkLinkData>) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                MediatedHomeworksLinkerHeader()
                MediatedHomeworksLinkerContent(
                    isLoading = isLoading,
                    organizations = organizations,
                    linkDataList = linkDataList,
                    linkSubjects = linkSubjects,
                    linkSchedule = linkSchedule,
                    onAddSubject = onAddSubject,
                    onLoadSubjects = onLoadSubjects,
                    onUpdateLinkData = onUpdateLinkData,
                )
            }
            Button(
                enabled = linkDataList.find { it.actualSubject == null } == null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                onClick = { onAdd(linkDataList) },
            ) {
                Text(text = TasksThemeRes.strings.applySharedHomeworksTitle)
            }
        }
    }
}

@Composable
private fun MediatedHomeworksLinkerHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = TasksThemeRes.strings.homeworksLinkedHeader,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = TasksThemeRes.strings.homeworksLinkedLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MediatedHomeworksLinkerContent(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isLoading: Boolean,
    organizations: List<OrganizationShortUi>,
    linkDataList: List<MediatedHomeworkLinkData>,
    linkSchedule: ScheduleUi?,
    linkSubjects: List<SubjectUi>,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onUpdateLinkData: (MediatedHomeworkLinkData) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.targetUserSubjectsTitle,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.currentUserSubjectsTitle,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            )
        ) { loading ->
            if (loading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(350.dp),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(linkDataList, key = { it.homework.uid }) { linkData ->
                        MediatedHomeworksLinkView(
                            organizations = organizations,
                            linkData = linkData,
                            linkSchedule = linkSchedule,
                            linkSubjects = linkSubjects,
                            onAddSubject = onAddSubject,
                            onLoadSubjects = onLoadSubjects,
                            onSelectSubject = {
                                val updatedLinkData = linkData.copy(actualSubject = it)
                                onUpdateLinkData(updatedLinkData)
                            },
                            onSelectLinkedClass = {
                                val updatedLinkData = linkData.copy(actualLinkedClass = it)
                                onUpdateLinkData(updatedLinkData)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediatedHomeworksLinkView(
    modifier: Modifier = Modifier,
    organizations: List<OrganizationShortUi>,
    linkData: MediatedHomeworkLinkData,
    linkSchedule: ScheduleUi?,
    linkSubjects: List<SubjectUi>,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onSelectSubject: (SubjectUi?) -> Unit,
    onSelectLinkedClass: (NumberedClassUi?) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = linkData.receivedSubjectName,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 4,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            tint = if (linkData.actualSubject != null && linkData.actualLinkedClass != null) {
                StudyAssistantRes.colors.accents.green
            } else if (linkData.actualSubject != null) {
                StudyAssistantRes.colors.accents.orange
            } else {
                StudyAssistantRes.colors.accents.red
            },
        )
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = linkData.actualSubject,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { actualSubject ->
            if (actualSubject != null) {
                SubjectAndClassSelectorView(
                    organizations = organizations,
                    actualSubject = actualSubject,
                    actualLinkedClass = linkData.actualLinkedClass,
                    linkSchedule = linkSchedule,
                    linkSubjects = linkSubjects,
                    onAddSubject = onAddSubject,
                    onLoadSubjects = onLoadSubjects,
                    onSelectSubject = onSelectSubject,
                    onSelectLinkedClass = onSelectLinkedClass,
                )
            } else {
                SubjectSelectorView(
                    organizations = organizations,
                    linkSubjects = linkSubjects,
                    onAddSubject = onAddSubject,
                    onLoadSubjects = onLoadSubjects,
                    onSelectSubject = onSelectSubject,
                )
            }
        }
    }
}

@Composable
private fun SubjectAndClassSelectorView(
    modifier: Modifier = Modifier,
    organizations: List<OrganizationShortUi>,
    actualSubject: SubjectUi,
    actualLinkedClass: NumberedClassUi?,
    linkSchedule: ScheduleUi?,
    linkSubjects: List<SubjectUi>,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onSelectSubject: (SubjectUi?) -> Unit,
    onSelectLinkedClass: (NumberedClassUi?) -> Unit,
) {
    var subjectSelectorDialogState by rememberSaveable { mutableStateOf(false) }
    var linkedClassSelectorDialogState by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(MaterialTheme.shapes.large.topSide)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { subjectSelectorDialogState = true },
        ) {
            Surface(
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp).width(4.dp),
                shape = MaterialTheme.shapes.small,
                color = Color(actualSubject.color),
                content = { Box(modifier = Modifier.fillMaxHeight()) }
            )
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                Text(
                    text = actualSubject.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(MaterialTheme.shapes.large.bottomSide)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .clickable { linkedClassSelectorDialogState = true }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (actualLinkedClass != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = actualLinkedClass.data.subject?.name ?: StudyAssistantRes.strings.noneTitle,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        text = buildString {
                            append(actualLinkedClass.number)
                            append(" ", TasksThemeRes.strings.numberOfClassSuffix)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            } else if (linkSchedule?.classes?.isNotEmpty() == true) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = TasksThemeRes.strings.attachClassesLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            } else {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = TasksThemeRes.strings.classesNotFoundLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }

    if (subjectSelectorDialogState) {
        val actualOrganization = organizations.find { actualSubject.organizationId == it.uid }
        var organization by remember {
            mutableStateOf(actualOrganization ?: organizations.getOrNull(0))
        }

        LaunchedEffect(true) {
            if (actualOrganization != null) onLoadSubjects(actualOrganization.uid)
        }

        SubjectSelectorDialog(
            selected = actualSubject,
            targetOrganization = organization,
            subjects = linkSubjects,
            organizations = organizations,
            onDismiss = { subjectSelectorDialogState = false },
            onChangeOrganization = {
                organization = it
                onLoadSubjects(it.uid)
            },
            onAddSubject = onAddSubject,
            onConfirm = {
                onSelectSubject(it)
                subjectSelectorDialogState = false
            },
        )
    }

    if (linkedClassSelectorDialogState) {
        LinkedClassSelectorDialog(
            selected = actualLinkedClass,
            classes = linkSchedule?.classes?.filter { it.subject?.uid == actualSubject.uid }?.map {
                NumberedClassUi(it, linkSchedule.classes.indexOf(it).inc())
            } ?: emptyList(),
            onDismiss = { linkedClassSelectorDialogState = false },
            onConfirm = {
                onSelectLinkedClass(it)
                linkedClassSelectorDialogState = false
            },
        )
    }
}

@Composable
private fun SubjectSelectorView(
    modifier: Modifier = Modifier,
    organizations: List<OrganizationShortUi>,
    linkSubjects: List<SubjectUi>,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onSelectSubject: (SubjectUi?) -> Unit,
) {
    var subjectSelectorDialogState by remember { mutableStateOf(false) }

    Surface(
        onClick = { subjectSelectorDialogState = true },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier,
                text = TasksThemeRes.strings.specifySubjectLabel,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }

    if (subjectSelectorDialogState) {
        var organization by remember {
            mutableStateOf(organizations.find { it.isMain } ?: organizations.getOrNull(0))
        }
        SubjectSelectorDialog(
            selected = null,
            targetOrganization = organization,
            subjects = linkSubjects,
            organizations = organizations,
            onDismiss = { subjectSelectorDialogState = false },
            onChangeOrganization = {
                organization = it
                onLoadSubjects(it.uid)
            },
            onAddSubject = onAddSubject,
            onConfirm = {
                onSelectSubject(it)
                subjectSelectorDialogState = false
            },
        )
    }
}