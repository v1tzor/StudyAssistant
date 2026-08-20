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

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.presentation.utils.groupedContactInfo
import ru.aleshin.studyassistant.core.ui.ads.AdPlacement
import ru.aleshin.studyassistant.core.ui.ads.YandexInlineBanner
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.contract.OrganizationsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.AddOrganizationSelectCard
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.NoneOrganizationInfoView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ORGANIZATION_SELECT_CARD_HEIGHT
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ORGANIZATION_SELECT_CARD_WIDTH
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationContactInfoItem
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.OrganizationSelectCard
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortEmployeeView
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views.ShortSubjectView
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.contact_info_section_title
import ru.aleshin.studyassistant.info.impl.resources.employees_section_title
import ru.aleshin.studyassistant.info.impl.resources.subjects_section_title
import androidx.compose.foundation.lazy.items as lazyItems

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationsExpandedLayout(
    modifier: Modifier = Modifier,
    state: OrganizationsState,
    onRefresh: () -> Unit,
    onAddOrganization: () -> Unit,
    onSelectOrganization: (UID?) -> Unit,
    onEditOrganizationId: (UID) -> Unit,
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
        state = rememberPullToRefreshState(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                        vertical = AdaptiveLayoutDefaults.SpaceExtraLarge,
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceSection),
            ) {
                OrganizationsExpandedInfoSection(
                    isLoading = state.isLoading,
                    organizations = state.shortOrganizations.orEmpty(),
                    selectedOrganizationId = state.organizationData?.uid,
                    classesInfo = state.classesInfo,
                    onSelectOrganization = onSelectOrganization,
                    onAddOrganization = onAddOrganization,
                    onEditOrganizationId = onEditOrganizationId,
                )
                OrganizationsExpandedContactSection(
                    isLoading = state.isLoading,
                    organizationData = state.organizationData,
                    onCopyContactInfo = onCopyContactInfo,
                )
                OrganizationsExpandedEmployeesSection(
                    isLoading = state.isLoading,
                    organizationData = state.organizationData,
                    onShowAllEmployee = onShowAllEmployee,
                    onShowEmployeeProfile = onShowEmployeeProfile,
                )
                OrganizationsExpandedSubjectsSection(
                    isLoading = state.isLoading,
                    organizationData = state.organizationData,
                    onShowAllSubjects = onShowAllSubjects,
                    onShowSubjectEditor = onShowSubjectEditor,
                )
                YandexInlineBanner(
                    modifier = Modifier.fillMaxWidth(),
                    placement = AdPlacement.INFO_ORGANIZATIONS,
                )
            }
        }
    }
}

@Composable
private fun OrganizationsExpandedInfoSection(
    isLoading: Boolean,
    organizations: List<OrganizationShortUi>,
    selectedOrganizationId: UID?,
    classesInfo: OrganizationClassesInfoUi?,
    onSelectOrganization: (UID?) -> Unit,
    onAddOrganization: () -> Unit,
    onEditOrganizationId: (UID) -> Unit,
) {
    if (isLoading && organizations.isEmpty()) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceLarge),
        ) {
            items(2) {
                PlaceholderBox(
                    modifier = Modifier
                        .width(ORGANIZATION_SELECT_CARD_WIDTH)
                        .height(ORGANIZATION_SELECT_CARD_HEIGHT),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                )
            }
        }
    } else {
        val state = rememberLazyListState()

        LazyRow(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceLarge),
            flingBehavior = rememberSnapFlingBehavior(state)
        ) {
            lazyItems(organizations, key = { it.uid }) { organization ->
                val isSelected = organization.uid == selectedOrganizationId
                OrganizationSelectCard(
                    organization = organization,
                    isSelected = isSelected,
                    classesInfo = classesInfo,
                    onClick = { onSelectOrganization(organization.uid) },
                    onEditClick = { onEditOrganizationId(organization.uid) },
                )
            }
            item(key = "add_organization") {
                AddOrganizationSelectCard(onClick = onAddOrganization)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun OrganizationsExpandedContactSection(
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onCopyContactInfo: (ContactInfoUi) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceMedium),
    ) {
        Text(
            text = stringResource(Res.string.contact_info_section_title),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLoading) {
                repeat(Placeholder.CONTACT_INFO) {
                    PlaceholderBox(
                        modifier = Modifier.size(180.dp, 48.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            } else {
                val groupedContactInfo = remember(organizationData) {
                    organizationData?.groupedContactInfo()
                }
                if (!groupedContactInfo.isNullOrEmpty()) {
                    groupedContactInfo.forEach { contactEntry ->
                        OrganizationContactInfoItem(
                            onClick = { onCopyContactInfo(contactEntry.key) },
                            icon = painterResource(contactEntry.value.mapToIcon()),
                            contactInfo = contactEntry.key,
                        )
                    }
                } else {
                    NoneOrganizationInfoView(
                        modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth)
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizationsExpandedEmployeesSection(
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onShowAllEmployee: () -> Unit,
    onShowEmployeeProfile: (UID) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceMedium),
    ) {
        OrganizationsExpandedSectionHeader(
            title = stringResource(Res.string.employees_section_title),
            enabledMore = !isLoading && organizationData != null,
            onMoreClick = onShowAllEmployee,
        )
        if (isLoading) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(ORGANIZATIONS_EXPANDED_EMPLOYEE_GRID_HEIGHT),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
            ) {
                items(Placeholder.SHORT_EMPLOYEES) {
                    PlaceholderBox(
                        modifier = Modifier
                            .width(ORGANIZATIONS_EXPANDED_EMPLOYEE_CARD_WIDTH)
                            .height(ORGANIZATIONS_EXPANDED_EMPLOYEE_CARD_HEIGHT),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
        } else {
            val employees = organizationData?.employee.orEmpty()
            if (employees.isNotEmpty()) {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(ORGANIZATIONS_EXPANDED_EMPLOYEE_GRID_HEIGHT),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                    verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                ) {
                    items(
                        items = employees,
                        key = { employee -> employee.uid },
                    ) { employee ->
                        ShortEmployeeView(
                            onClick = { onShowEmployeeProfile(employee.uid) },
                            avatar = employee.avatar,
                            post = employee.post,
                            firstName = employee.firstName,
                            secondName = employee.patronymic ?: employee.secondName,
                            subjects = organizationData?.subjects.orEmpty().filter { subject ->
                                subject.teacher?.uid == employee.uid
                            },
                        )
                    }
                }
            } else {
                NoneOrganizationInfoView(
                    modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth)
                )
            }
        }
    }
}

@Composable
private fun OrganizationsExpandedSubjectsSection(
    isLoading: Boolean,
    organizationData: OrganizationUi?,
    onShowAllSubjects: () -> Unit,
    onShowSubjectEditor: (UID) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceMedium),
    ) {
        OrganizationsExpandedSectionHeader(
            title = stringResource(Res.string.subjects_section_title),
            enabledMore = !isLoading && organizationData != null,
            onMoreClick = onShowAllSubjects,
        )
        if (isLoading) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height(ORGANIZATIONS_EXPANDED_SUBJECT_GRID_HEIGHT),
                horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
            ) {
                items(Placeholder.SHORT_EMPLOYEES) {
                    PlaceholderBox(
                        modifier = Modifier
                            .width(ORGANIZATIONS_EXPANDED_SUBJECT_CARD_WIDTH)
                            .height(ORGANIZATIONS_EXPANDED_SUBJECT_CARD_HEIGHT),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
        } else {
            val subjects = organizationData?.subjects.orEmpty()
            if (subjects.isNotEmpty()) {
                LazyHorizontalGrid(
                    rows = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().height(ORGANIZATIONS_EXPANDED_SUBJECT_GRID_HEIGHT),
                    state = rememberLazyGridState(),
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                    verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                ) {
                    items(
                        items = subjects,
                        key = { subject -> subject.uid },
                    ) { subject ->
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
                NoneOrganizationInfoView(
                    modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth)
                )
            }
        }
    }
}

@Composable
private fun OrganizationsExpandedSectionHeader(
    title: String,
    enabledMore: Boolean,
    onMoreClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.alphaByEnabled(enabledMore).size(32.dp),
            enabled = enabledMore,
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
}

private val ORGANIZATIONS_EXPANDED_EMPLOYEE_CARD_WIDTH = 300.dp
private val ORGANIZATIONS_EXPANDED_EMPLOYEE_CARD_HEIGHT = 88.dp
private val ORGANIZATIONS_EXPANDED_EMPLOYEE_GRID_HEIGHT =
    ORGANIZATIONS_EXPANDED_EMPLOYEE_CARD_HEIGHT * 2 + AdaptiveLayoutDefaults.GridSpacing
private val ORGANIZATIONS_EXPANDED_SUBJECT_CARD_WIDTH = 230.dp
private val ORGANIZATIONS_EXPANDED_SUBJECT_CARD_HEIGHT = 92.dp
private val ORGANIZATIONS_EXPANDED_SUBJECT_GRID_HEIGHT =
    ORGANIZATIONS_EXPANDED_SUBJECT_CARD_HEIGHT * 2 + AdaptiveLayoutDefaults.GridSpacing
