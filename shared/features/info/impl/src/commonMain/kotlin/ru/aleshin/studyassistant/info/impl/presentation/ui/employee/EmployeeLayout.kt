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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeState
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.DetailsEmployeeViewItem
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.no_result_title as core_no_result_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun EmployeeLayout(
    modifier: Modifier = Modifier,
    layoutMode: InfoLayoutMode,
    state: EmployeeState,
    onOpenEmployeeProfile: (UID) -> Unit,
    onEditEmployee: (UID) -> Unit,
    onDeleteEmployee: (UID) -> Unit,
) {
    when (layoutMode) {
        InfoLayoutMode.COMPACT -> EmployeeCompactLayout(
            modifier = modifier,
            state = state,
            onOpenEmployeeProfile = onOpenEmployeeProfile,
            onEditEmployee = onEditEmployee,
            onDeleteEmployee = onDeleteEmployee,
        )
        InfoLayoutMode.EXPANDED -> EmployeeExpandedLayout(
            modifier = modifier,
            state = state,
            onOpenEmployeeProfile = onOpenEmployeeProfile,
            onEditEmployee = onEditEmployee,
            onDeleteEmployee = onDeleteEmployee,
        )
    }
}

@Composable
private fun EmployeeCompactLayout(
    modifier: Modifier = Modifier,
    state: EmployeeState,
    listState: LazyListState = rememberLazyListState(),
    onOpenEmployeeProfile: (UID) -> Unit,
    onEditEmployee: (UID) -> Unit,
    onDeleteEmployee: (UID) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 12.dp, end = 16.dp, top = 16.dp),
        targetState = state.isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (loading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        } else if (state.employees.isNotEmpty()) {
            val employeesList = remember(state.employees) {
                state.employees.toList()
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(employeesList, key = { it.first }) { alphabeticEmployees ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = alphabeticEmployees.first.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            alphabeticEmployees.second.forEach { employee ->
                                DetailsEmployeeViewItem(
                                    avatar = employee.data.avatar,
                                    post = employee.data.post,
                                    firstName = employee.data.firstName,
                                    secondName = employee.data.secondName,
                                    patronymic = employee.data.patronymic,
                                    subjects = employee.subjects,
                                    isHavePhone = employee.data.phones.isNotEmpty(),
                                    isHaveEmail = employee.data.emails.isNotEmpty(),
                                    isHaveWebsite = employee.data.webs.isNotEmpty(),
                                    onOpenProfile = { onOpenEmployeeProfile(employee.data.uid) },
                                    onEdit = { onEditEmployee(employee.data.uid) },
                                    onDelete = { onDeleteEmployee(employee.data.uid) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(CoreRes.string.core_no_result_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun EmployeeExpandedLayout(
    modifier: Modifier = Modifier,
    state: EmployeeState,
    listState: LazyListState = rememberLazyListState(),
    onOpenEmployeeProfile: (UID) -> Unit,
    onEditEmployee: (UID) -> Unit,
    onDeleteEmployee: (UID) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Crossfade(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = AdaptiveLayoutDefaults.ExpandedContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    start = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                    end = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                    top = AdaptiveLayoutDefaults.SpaceExtraLarge,
                ),
            targetState = state.isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (loading) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceExtraLarge),
                ) {
                    items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                        PlaceholderBox(
                            modifier = Modifier.fillMaxWidth().height(96.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                    }
                }
            } else if (state.employees.isNotEmpty()) {
                val employeesList = remember(state.employees) {
                    state.employees.toList()
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceSection),
                    horizontalAlignment = Alignment.Start,
                ) {
                    items(employeesList, key = { it.first }) { alphabeticEmployees ->
                        Row(horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.SpaceLarge)) {
                            Text(
                                modifier = Modifier.padding(top = 16.dp),
                                text = alphabeticEmployees.first.toString(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium,
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                            ) {
                                alphabeticEmployees.second.chunked(2).forEach { rowEmployees ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                                    ) {
                                        rowEmployees.forEach { employee ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                DetailsEmployeeViewItem(
                                                    useExpandedStyle = true,
                                                    avatar = employee.data.avatar,
                                                    post = employee.data.post,
                                                    firstName = employee.data.firstName,
                                                    secondName = employee.data.secondName,
                                                    patronymic = employee.data.patronymic,
                                                    subjects = employee.subjects,
                                                    isHavePhone = employee.data.phones.isNotEmpty(),
                                                    isHaveEmail = employee.data.emails.isNotEmpty(),
                                                    isHaveWebsite = employee.data.webs.isNotEmpty(),
                                                    onOpenProfile = { onOpenEmployeeProfile(employee.data.uid) },
                                                    onEdit = { onEditEmployee(employee.data.uid) },
                                                    onDelete = { onDeleteEmployee(employee.data.uid) },
                                                )
                                            }
                                        }
                                        if (rowEmployees.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = stringResource(CoreRes.string.core_no_result_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
