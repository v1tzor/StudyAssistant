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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.ui.UsersLayoutMode
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileState
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.EmployeeProfileComponent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoNoneView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoViewPlaceholder
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeProfileSubjectsNoneView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeProfileTopBar
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeSubjectViewItem
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeSubjectViewPlaceholder
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeTopSheet
import ru.aleshin.studyassistant.users.impl.presentation.ui.fetchUsersLayoutMode
import ru.aleshin.studyassistant.users.impl.resources.Res
import ru.aleshin.studyassistant.users.impl.resources.employee_contact_info_header
import ru.aleshin.studyassistant.users.impl.resources.employee_email_title
import ru.aleshin.studyassistant.users.impl.resources.employee_location_title
import ru.aleshin.studyassistant.users.impl.resources.employee_phone_title
import ru.aleshin.studyassistant.users.impl.resources.employee_subjects_header
import ru.aleshin.studyassistant.users.impl.resources.employee_website_title

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
@Composable
internal fun EmployeeProfileContent(
    employeeProfileComponent: EmployeeProfileComponent,
    modifier: Modifier = Modifier,
) {
    val store = employeeProfileComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchUsersLayoutMode()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            EmployeeProfileLayout(
                state = state,
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
            )
        },
        topBar = {
            Column {
                EmployeeProfileTopBar(
                    enabledEdit = !state.employee?.uid.isNullOrBlank(),
                    isExpanded = layoutMode == UsersLayoutMode.EXPANDED,
                    onBackClick = { store.dispatchEvent(EmployeeProfileEvent.ClickBack) },
                    onEditClick = { store.dispatchEvent(EmployeeProfileEvent.ClickEdit) },
                )
                EmployeeTopSheet(
                    isLoading = state.isLoading,
                    employee = state.employee,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is EmployeeProfileEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun EmployeeProfileLayout(
    state: EmployeeProfileState,
    modifier: Modifier = Modifier,
    layoutMode: UsersLayoutMode,
) {
    when (layoutMode) {
        UsersLayoutMode.COMPACT -> BaseEmployeeProfileContent(
            state = state,
            modifier = modifier,
        )
        UsersLayoutMode.EXPANDED -> EmployeeProfileExpandedLayout(
            state = state,
            modifier = modifier,
        )
    }
}

@Composable
private fun EmployeeProfileExpandedLayout(
    state: EmployeeProfileState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                vertical = AdaptiveLayoutDefaults.SpaceExtraLarge,
            ),
        horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.PaneSpacing),
    ) {
        EmployeeProfileSubjectsSection(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            isLoading = state.isLoading,
            subjects = state.employee?.subjects ?: emptyList(),
            horizontalPadding = 0.dp,
        )
        EmployeeProfileContactInfoSection(
            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            isLoading = state.isLoading,
            emails = state.employee?.emails ?: emptyList(),
            phones = state.employee?.phones ?: emptyList(),
            webs = state.employee?.webs ?: emptyList(),
            locations = state.employee?.locations ?: emptyList(),
            horizontalPadding = 0.dp,
        )
    }
}

@Composable
private fun BaseEmployeeProfileContent(
    state: EmployeeProfileState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) {
    Column(
        modifier = modifier.padding(top = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        EmployeeProfileSubjectsSection(
            isLoading = state.isLoading,
            subjects = state.employee?.subjects ?: emptyList(),
        )
        EmployeeProfileContactInfoSection(
            isLoading = state.isLoading,
            emails = state.employee?.emails ?: emptyList(),
            phones = state.employee?.phones ?: emptyList(),
            webs = state.employee?.webs ?: emptyList(),
            locations = state.employee?.locations ?: emptyList(),
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun EmployeeProfileSubjectsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    subjects: List<SubjectUi>,
    horizontalPadding: Dp = 16.dp,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.employee_subjects_header),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Crossfade(
                targetState = isLoading,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) { loading ->
                if (!loading) {
                    MediumInfoBadge(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Text(text = subjects.size.toString(), maxLines = 1)
                    }
                } else {
                    PlaceholderBox(
                        modifier = Modifier.size(20.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    )
                }
            }
        }
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (subjects.isNotEmpty()) {
                        items(subjects, key = { it.uid }) { subject ->
                            EmployeeSubjectViewItem(
                                modifier = Modifier.animateItem(),
                                eventType = subject.eventType,
                                color = Color(subject.color),
                                name = subject.name,
                                office = subject.office,
                            )
                        }
                    } else {
                        item { EmployeeProfileSubjectsNoneView(modifier = Modifier.fillParentMaxWidth()) }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(Placeholder.SHORT_SUBJECTS) {
                        EmployeeSubjectViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeProfileContactInfoSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    emails: List<ContactInfoUi>,
    phones: List<ContactInfoUi>,
    locations: List<ContactInfoUi>,
    webs: List<ContactInfoUi>,
    horizontalPadding: Dp = 16.dp,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.employee_contact_info_header),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!loading) {
                    val clipboardManager = LocalClipboardManager.current
                    if (phones.isNotEmpty()) {
                        phones.forEach { phone ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(phone.value)) },
                                icon = Icons.Outlined.Phone,
                                title = phone.label ?: stringResource(Res.string.employee_phone_title),
                                value = phone.value,
                            )
                        }
                    }
                    if (emails.isNotEmpty()) {
                        emails.forEach { email ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(email.value)) },
                                icon = Icons.Outlined.Email,
                                title = email.label ?: stringResource(Res.string.employee_email_title),
                                value = email.value,
                            )
                        }
                    }
                    if (webs.isNotEmpty()) {
                        webs.forEach { web ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(web.value)) },
                                icon = Icons.Default.Language,
                                title = web.label ?: stringResource(Res.string.employee_website_title),
                                value = web.value,
                            )
                        }
                    }
                    if (locations.isNotEmpty()) {
                        locations.forEach { location ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(location.value)) },
                                icon = Icons.Outlined.LocationOn,
                                title = location.label
                                    ?: stringResource(Res.string.employee_location_title),
                                value = location.value,
                            )
                        }
                    }
                    if (emails.isEmpty() && phones.isEmpty() && webs.isEmpty() && locations.isEmpty()) {
                        EmployeeContactInfoNoneView()
                    }
                } else {
                    repeat(Placeholder.USER_CONTACT_INFO) {
                        EmployeeContactInfoViewPlaceholder()
                    }
                }
            }
        }
    }
}
