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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.convertToShort
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 16.08.2024
 */
@Composable
internal fun OrganizationDataLinker(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isLoadingLinkedOrganization: Boolean,
    allOrganizations: List<OrganizationShortUi>,
    organizationsLinkData: List<OrganizationLinkData>,
    onLinkOrganization: (sharedOrganization: UID, linkedOrganization: UID?) -> Unit,
    onLinkSubjects: (sharedOrganization: UID, subjects: Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (sharedOrganization: UID, teachers: Map<UID, EmployeeUi>) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val organizationsPagerState = rememberPagerState { organizationsLinkData.size.takeIf { it > 0 } ?: 1 }
        val linkData = organizationsLinkData.getOrNull(organizationsPagerState.currentPage)

        OrganizationsSelectorSection(
            isLoading = isLoading,
            isLoadingLinkedOrganization = isLoadingLinkedOrganization,
            allOrganizations = allOrganizations,
            organizationsLinkData = organizationsLinkData,
            pagerState = organizationsPagerState,
            onLinkOrganization = onLinkOrganization,
        )
        SubjectsLinkerSection(
            isLoading = isLoading,
            organizationLinkData = linkData,
            onLinkSubjects = onLinkSubjects,
        )
        EmployeeLinkerSection(
            isLoading = isLoading,
            organizationLinkData = linkData,
            onLinkTeachers = onLinkTeachers,
        )
    }
}

@Composable
private fun OrganizationsSelectorSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isLoadingLinkedOrganization: Boolean,
    allOrganizations: List<OrganizationShortUi>,
    organizationsLinkData: List<OrganizationLinkData>,
    pagerState: PagerState,
    onLinkOrganization: (sharedOrganization: UID, linkedOrganization: UID?) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = ScheduleThemeRes.strings.sharedOrganizationHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            if (!loading && organizationsLinkData.isNotEmpty()) {
                HorizontalPager(state = pagerState) { organizationIndex ->
                    var organizationLinkedDialogState by remember { mutableStateOf(false) }
                    val linkData = organizationsLinkData[organizationIndex]

                    SharedOrganizationView(
                        isLinked = linkData.linkedOrganization != null,
                        isLoadingLinkedOrganization = isLoadingLinkedOrganization,
                        shortName = linkData.linkedOrganization?.shortName ?: linkData.sharedOrganization.shortName,
                        type = linkData.linkedOrganization?.type ?: linkData.sharedOrganization.type,
                        groupedContactInfo = linkData.linkedOrganization?.groupedContactInfo() ?: linkData.sharedOrganization.groupedContactInfo(),
                        onLinkedChange = {
                            if (linkData.linkedOrganization != null) {
                                onLinkOrganization(linkData.sharedOrganization.uid, null)
                            } else {
                                organizationLinkedDialogState = true
                            }
                        },
                    )

                    if (organizationLinkedDialogState) {
                        OrganizationLinkedDialog(
                            selected = linkData.linkedOrganization?.convertToShort(),
                            organizations = allOrganizations.filter { organization ->
                                organizationsLinkData.find { it.linkedOrganization?.uid == organization.uid } == null
                            },
                            onDismiss = { organizationLinkedDialogState = false },
                            onConfirm = {
                                onLinkOrganization(linkData.sharedOrganization.uid, it?.uid)
                                organizationLinkedDialogState = false
                            },
                        )
                    }
                }
            } else {
                SharedOrganizationViewPlaceholder()
            }
        }
    }
}

@Composable
private fun SubjectsLinkerSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationLinkData: OrganizationLinkData?,
    onLinkSubjects: (sharedOrganization: UID, subjects: Map<UID, SubjectUi>) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = ScheduleThemeRes.strings.sharedSubjectsHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            if (!loading && organizationLinkData != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    organizationLinkData.sharedOrganization.subjects.forEach { sharedSubject ->
                        var subjectLinkedDialogState by remember { mutableStateOf(false) }
                        val linkedSubject = organizationLinkData.linkedSubjects[sharedSubject.uid]

                        LinkedSubjectsView(
                            enabledLink = organizationLinkData.linkedOrganization != null,
                            sharedSubject = sharedSubject,
                            linkedSubject = linkedSubject,
                            onLinkSubject = { subjectLinkedDialogState = true }
                        )

                        if (subjectLinkedDialogState) {
                            SubjectLinkerDialog(
                                selected = organizationLinkData.linkedSubjects[sharedSubject.uid],
                                subjects = organizationLinkData.linkedOrganization?.subjects ?: emptyList(),
                                onDismiss = { subjectLinkedDialogState = false },
                                onConfirm = { subject ->
                                    val sharedOrganizationId = organizationLinkData.sharedOrganization.uid
                                    val subjects = buildMap {
                                        putAll(organizationLinkData.linkedSubjects)
                                        if (subject != null) {
                                            put(sharedSubject.uid, subject)
                                        } else {
                                            remove(sharedSubject.uid)
                                        }
                                        Unit
                                    }
                                    onLinkSubjects(sharedOrganizationId, subjects)
                                    subjectLinkedDialogState = false
                                },
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(Placeholder.LINK_SUBJECTS) {
                        LinkedSubjectsViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeLinkerSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    organizationLinkData: OrganizationLinkData?,
    onLinkTeachers: (sharedOrganization: UID, teachers: Map<UID, EmployeeUi>) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = ScheduleThemeRes.strings.sharedEmployeesHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            if (!loading && organizationLinkData != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    organizationLinkData.sharedOrganization.employee.forEach { shareEmployee ->
                        var employeesLinkedDialogState by remember { mutableStateOf(false) }
                        val linkedEmployee = organizationLinkData.linkedTeachers[shareEmployee.uid]

                        LinkedEmployeesView(
                            enabledLink = organizationLinkData.linkedOrganization != null,
                            sharedTeacher = shareEmployee,
                            linkedTeacher = linkedEmployee,
                            onLinkEmployee = { employeesLinkedDialogState = true }
                        )

                        if (employeesLinkedDialogState) {
                            EmployeeLinkerDialog(
                                selected = organizationLinkData.linkedTeachers[shareEmployee.uid],
                                employees = organizationLinkData.linkedOrganization?.employee ?: emptyList(),
                                onDismiss = { employeesLinkedDialogState = false },
                                onConfirm = { employee ->
                                    val sharedOrganizationId = organizationLinkData.sharedOrganization.uid
                                    val employees = buildMap {
                                        putAll(organizationLinkData.linkedTeachers)
                                        if (employee != null) {
                                            put(shareEmployee.uid, employee)
                                        } else {
                                            remove(shareEmployee.uid)
                                        }
                                        Unit
                                    }
                                    onLinkTeachers(sharedOrganizationId, employees)
                                    employeesLinkedDialogState = false
                                },
                            )
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(Placeholder.LINK_EMPLOYEES) {
                        LinkedEmployeesViewPlaceholder()
                    }
                }
            }
        }
    }
}