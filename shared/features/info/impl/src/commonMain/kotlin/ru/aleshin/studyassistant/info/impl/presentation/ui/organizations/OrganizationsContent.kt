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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.DISABLED_ALPHA
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsViewState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.NoneOrganizationInfoView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.NoneOrganizationView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationContactInfoItem
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortEmployeeView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortSubjectView
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 16.06.2024
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationsContent(
    state: OrganizationsViewState,
    modifier: Modifier = Modifier,
    refreshState: PullToRefreshState = rememberPullToRefreshState(),
    scrollState: ScrollState = rememberScrollState(),
    onRefresh: () -> Unit,
    onAddOrganization: () -> Unit,
    onEditOrganization: () -> Unit,
    onCopyContactInfo: (ContactInfoUi) -> Unit,
    onShowAllEmployee: () -> Unit,
    onShowEmployeeProfile: (UID) -> Unit,
    onShowAllSubjects: () -> Unit,
    onShowSubjectEditor: (UID) -> Unit,
) = with(state) {
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        state = refreshState,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OrganizationsInfoSection(
                isLoading = isLoading,
                organizationData = organizationData,
                classesInfo = classesInfo,
                onAddOrganization = onAddOrganization,
                onEditOrganization = onEditOrganization,
            )
            OrganizationsContactSection(
                isLoading = isLoading,
                organizationData = organizationData,
                onCopyContactInfo = onCopyContactInfo,
            )
            OrganizationsEmployeesSection(
                isLoading = isLoading,
                organizationData = organizationData,
                onShowAllEmployee = onShowAllEmployee,
                onShowEmployeeProfile = onShowEmployeeProfile,
            )
            OrganizationsSubjectsSection(
                isLoading = isLoading,
                organizationData = organizationData,
                onShowAllSubjects = onShowAllSubjects,
                onShowSubjectEditor = onShowSubjectEditor,
            )
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
private fun OrganizationsInfoSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    classesInfo: OrganizationClassesInfoUi?,
    onAddOrganization: () -> Unit,
    onEditOrganization: () -> Unit,
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier.padding(horizontal = 16.dp),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        Column(
            modifier = Modifier.animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!loading) {
                if (organizationData != null) {
                    OrganizationView(
                        organizationData = organizationData,
                        classesInfo = classesInfo,
                    )
                    Button(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        onClick = onEditOrganization,
                    ) {
                        Text(text = InfoThemeRes.strings.editOrganizationTitle)
                    }
                } else {
                    NoneOrganizationView()
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        onClick = onAddOrganization,
                    ) {
                        Text(text = InfoThemeRes.strings.addOrganizationTitle)
                    }
                }
            } else {
                PlaceholderBox(
                    modifier = Modifier.fillMaxWidth().height(201.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
                PlaceholderBox(
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OrganizationsContactSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onCopyContactInfo: (ContactInfoUi) -> Unit,
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier.animateContentSize(tween()).padding(horizontal = 16.dp),
    ) { loading ->
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = InfoThemeRes.strings.contactInfoSectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!loading) {
                    if (organizationData != null) {
                        organizationData.groupedContactInfo().forEach { contactEntry ->
                            OrganizationContactInfoItem(
                                onClick = { onCopyContactInfo(contactEntry.value) },
                                icon = painterResource(contactEntry.key.mapToIcon(StudyAssistantRes.icons)),
                                contactInfo = contactEntry.value
                            )
                        }
                    } else {
                        NoneOrganizationInfoView()
                    }
                } else {
                    repeat(Placeholder.CONTACT_INFO) {
                        PlaceholderBox(
                            modifier = Modifier.size(160.dp, 40.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    }
                    PlaceholderBox(
                        modifier = Modifier.size(100.dp, 40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                    PlaceholderBox(
                        modifier = Modifier.size(160.dp, 40.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizationsEmployeesSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onShowAllEmployee: () -> Unit,
    onShowEmployeeProfile: (UID) -> Unit,
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier.padding(horizontal = 16.dp),
    ) { loading ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = InfoThemeRes.strings.employeesSectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(
                    onClick = onShowAllEmployee,
                    modifier = Modifier.alphaByEnabled(!isLoading).size(32.dp),
                    enabled = !isLoading && organizationData != null,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = DISABLED_ALPHA),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }
            }
            if (!loading) {
                if (organizationData != null && organizationData.employee.size > 2) {
                    val gridState: LazyGridState = rememberLazyGridState()
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().height(188.dp),
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(gridState)),
                    ) {
                        items(organizationData.employee, key = { it.uid }) { employee ->
                            ShortEmployeeView(
                                onClick = { onShowEmployeeProfile(employee.uid) },
                                avatar = employee.avatar,
                                post = employee.post,
                                firstName = employee.firstName,
                                secondName = employee.patronymic ?: employee.secondName,
                                subjects = organizationData.subjects.filter {
                                    it.teacher?.uid == employee.uid
                                },
                            )
                        }
                    }
                } else if (organizationData != null) {
                    val listState = rememberLazyListState()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(88.dp),
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        flingBehavior = rememberSnapFlingBehavior(listState),
                    ) {
                        items(organizationData.employee, key = { it.uid }) { employee ->
                            ShortEmployeeView(
                                onClick = { onShowEmployeeProfile(employee.uid) },
                                avatar = employee.avatar,
                                post = employee.post,
                                firstName = employee.firstName,
                                secondName = employee.patronymic ?: employee.secondName,
                                subjects = organizationData.subjects.filter {
                                    it.teacher?.uid == employee.uid
                                },
                            )
                        }
                    }
                } else {
                    NoneOrganizationInfoView()
                }
            } else {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(188.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(Placeholder.SHORT_EMPLOYEES) {
                        PlaceholderBox(
                            modifier = Modifier.size(300.dp, 88.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrganizationsSubjectsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onShowAllSubjects: () -> Unit,
    onShowSubjectEditor: (UID) -> Unit,
) {
    Crossfade(
        targetState = isLoading,
        modifier = modifier.padding(horizontal = 16.dp),
    ) { loading ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = InfoThemeRes.strings.subjectsSectionTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(
                    onClick = onShowAllSubjects,
                    modifier = Modifier.alphaByEnabled(!isLoading).size(32.dp),
                    enabled = !isLoading && organizationData != null,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = DISABLED_ALPHA),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                    )
                }
            }
            if (!loading) {
                if (organizationData != null && organizationData.subjects.size > 2) {
                    val gridState = rememberLazyGridState()
                    LazyHorizontalGrid(
                        rows = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().height(196.dp),
                        state = gridState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        flingBehavior = rememberSnapFlingBehavior(SnapLayoutInfoProvider(gridState)),
                    ) {
                        items(organizationData.subjects, key = { it.uid }) { subject ->
                            ShortSubjectView(
                                onClick = { onShowSubjectEditor(subject.uid) },
                                eventType = subject.eventType,
                                office = subject.office,
                                color = Color(subject.color),
                                name = subject.name,
                                teacher = subject.teacher,
                            )
                        }
                    }
                } else if (organizationData != null) {
                    val listState = rememberLazyListState()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(92.dp),
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        flingBehavior = rememberSnapFlingBehavior(listState),
                    ) {
                        items(organizationData.subjects, key = { it.uid }) { subject ->
                            ShortSubjectView(
                                onClick = { onShowSubjectEditor(subject.uid) },
                                eventType = subject.eventType,
                                office = subject.office,
                                color = Color(subject.color),
                                name = subject.name,
                                teacher = subject.teacher,
                            )
                        }
                    }
                } else {
                    NoneOrganizationInfoView()
                }
            } else {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(196.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(Placeholder.SHORT_EMPLOYEES) {
                        PlaceholderBox(
                            modifier = Modifier.size(230.dp, 92.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                        )
                    }
                }
            }
        }
    }
}