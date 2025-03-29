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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.users.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.users.impl.presentation.models.SubjectUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoNoneView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeContactInfoViewPlaceholder
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeProfileSubjectsNoneView
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeSubjectViewItem
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeSubjectViewPlaceholder

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
@Composable
internal fun EmployeeProfileContent(
    state: EmployeeProfileViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
) = with(state) {
    Column(
        modifier = modifier.padding(top = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        EmployeeProfileSubjectsSection(
            isLoading = isLoading,
            subjects = employee?.subjects ?: emptyList(),
        )
        EmployeeProfileContactInfoSection(
            isLoading = isLoading,
            emails = employee?.emails ?: emptyList(),
            phones = employee?.phones ?: emptyList(),
            webs = employee?.webs ?: emptyList(),
            locations = employee?.locations ?: emptyList(),
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun EmployeeProfileSubjectsSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    subjects: List<SubjectUi>,
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = UsersThemeRes.strings.employeeSubjectsHeader,
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
) {
    Column(
        modifier = modifier.animateContentSize().fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = UsersThemeRes.strings.employeeContactInfoHeader,
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
                                title = phone.label ?: UsersThemeRes.strings.employeePhoneTitle,
                                value = phone.value,
                            )
                        }
                    }
                    if (emails.isNotEmpty()) {
                        emails.forEach { email ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(email.value)) },
                                icon = Icons.Outlined.Email,
                                title = email.label ?: UsersThemeRes.strings.employeeEmailTitle,
                                value = email.value,
                            )
                        }
                    }
                    if (webs.isNotEmpty()) {
                        webs.forEach { web ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(web.value)) },
                                icon = Icons.Default.Language,
                                title = web.label ?: UsersThemeRes.strings.employeeWebsiteTitle,
                                value = web.value,
                            )
                        }
                    }
                    if (locations.isNotEmpty()) {
                        locations.forEach { location ->
                            EmployeeContactInfoView(
                                onClick = { clipboardManager.setText(AnnotatedString(location.value)) },
                                icon = Icons.Outlined.LocationOn,
                                title = location.label ?: UsersThemeRes.strings.employeeLocationTitle,
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