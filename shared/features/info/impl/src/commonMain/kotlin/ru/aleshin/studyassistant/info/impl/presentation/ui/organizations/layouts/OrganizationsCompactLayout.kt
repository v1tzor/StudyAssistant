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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.layouts

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.DISABLED_ALPHA
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.extensions.limitSize
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.presentation.utils.groupedContactInfo
import ru.aleshin.studyassistant.core.ui.ads.AdPlacement
import ru.aleshin.studyassistant.core.ui.ads.YandexInlineBanner
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.NoneOrganizationInfoView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.NoneOrganizationView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationContactInfoItem
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortEmployeeView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortSubjectView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShowAllItemView
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.add_organization_title
import ru.aleshin.studyassistant.info.impl.resources.contact_info_section_title
import ru.aleshin.studyassistant.info.impl.resources.edit_organization_title
import ru.aleshin.studyassistant.info.impl.resources.employees_section_title
import ru.aleshin.studyassistant.info.impl.resources.subjects_section_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationsCompactLayout(
    modifier: Modifier = Modifier,
    state: OrganizationsState,
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
) {
    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        state = refreshState,
    ) {
        Column(
            modifier = Modifier.verticalScroll(scrollState).padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OrganizationsInfoSection(
                isLoading = state.isLoading,
                organizationData = state.organizationData,
                classesInfo = state.classesInfo,
                onAddOrganization = onAddOrganization,
                onEditOrganization = onEditOrganization,
            )
            OrganizationsContactSection(
                isLoading = state.isLoading,
                organizationData = state.organizationData,
                onCopyContactInfo = onCopyContactInfo,
            )
            OrganizationsEmployeesSection(
                isLoading = state.isLoading,
                organizationData = state.organizationData,
                onShowAllEmployee = onShowAllEmployee,
                onShowEmployeeProfile = onShowEmployeeProfile,
            )
            OrganizationsSubjectsSection(
                isLoading = state.isLoading,
                organizationData = state.organizationData,
                onShowAllSubjects = onShowAllSubjects,
                onShowSubjectEditor = onShowSubjectEditor,
            )
            YandexInlineBanner(
                modifier = Modifier.padding(horizontal = 16.dp),
                placement = AdPlacement.INFO_ORGANIZATIONS,
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
                        Text(text = stringResource(Res.string.edit_organization_title))
                    }
                } else {
                    NoneOrganizationView()
                    FilledTonalButton(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        onClick = onAddOrganization,
                    ) {
                        Text(text = stringResource(Res.string.add_organization_title))
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
                text = stringResource(Res.string.contact_info_section_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!loading) {
                    val groupedContactInfo = remember(organizationData) {
                        organizationData?.groupedContactInfo()
                    }
                    if (!groupedContactInfo.isNullOrEmpty()) {
                        groupedContactInfo.forEach { contactEntry ->
                            OrganizationContactInfoItem(
                                onClick = { onCopyContactInfo(contactEntry.key) },
                                icon = painterResource(
                                    contactEntry.value.mapToIcon()
                                ),
                                contactInfo = contactEntry.key
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
                    text = stringResource(Res.string.employees_section_title),
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
                    ) {
                        items(
                            organizationData.employee.limitSize(7),
                            key = { it.uid }) { employee ->
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
                        if (organizationData.employee.size > 6) {
                            item(key = "ShowAll", span = { GridItemSpan(2) }) {
                                ShowAllItemView(onClick = onShowAllEmployee)
                            }
                        }
                    }
                } else if (organizationData != null && organizationData.employee.isNotEmpty()) {
                    val listState = rememberLazyListState()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(88.dp),
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
                    text = stringResource(Res.string.subjects_section_title),
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
                    ) {
                        items(organizationData.subjects.limitSize(7), key = { it.uid }) { subject ->
                            ShortSubjectView(
                                onClick = { onShowSubjectEditor(subject.uid) },
                                eventType = subject.eventType,
                                office = subject.office,
                                color = Color(subject.color),
                                name = subject.name,
                                teacher = subject.teacher,
                            )
                        }
                        if (organizationData.subjects.size > 6) {
                            item(key = "ShowAll", span = { GridItemSpan(2) }) {
                                ShowAllItemView(onClick = onShowAllSubjects)
                            }
                        }
                    }
                } else if (organizationData != null && organizationData.subjects.isNotEmpty()) {
                    val listState = rememberLazyListState()
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(92.dp),
                        state = listState,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
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
