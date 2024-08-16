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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.CircularStepsRow
import ru.aleshin.studyassistant.core.ui.views.MediumDragHandle
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.profile.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes

/**
 * @author Stanislav Aleshin on 15.08.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ScheduleSenderBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    pagerState: PagerState = rememberPagerState { ShareScheduleStep.entries.size },
    isLoadingSend: Boolean,
    allOrganizations: List<OrganizationShortUi>,
    allFriends: List<AppUserUi>,
    onDismissRequest: () -> Unit,
    onShareSchedule: (ShareSchedulesSendDataUi) -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var selectedOrganizations by remember { mutableStateOf<List<OrganizationShortUi>>(emptyList()) }
            var sendAllSubjectsOption by remember { mutableStateOf(false) }
            var sendAllEmployeesOption by remember { mutableStateOf(false) }
            var targetRecipient by remember { mutableStateOf<AppUserUi?>(null) }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false,
            ) { page ->
                when (ShareScheduleStep.byNumber(page)) {
                    ShareScheduleStep.ORGANIZATIONS -> ShareScheduleOrganizationsStep(
                        selectedOrganizations = selectedOrganizations,
                        allOrganizations = allOrganizations,
                        onSelectOrganization = { organization ->
                            selectedOrganizations = buildList {
                                addAll(selectedOrganizations)
                                add(organization)
                            }
                        },
                        onUnselectOrganization = { organization ->
                            selectedOrganizations = buildList {
                                addAll(selectedOrganizations)
                                remove(organization)
                            }
                        },
                    )
                    ShareScheduleStep.OPTIONS -> ShareScheduleOptionsStep(
                        sendAllSubjectsOption = sendAllSubjectsOption,
                        sendAllEmployeesOption = sendAllEmployeesOption,
                        onSubjectOptionChange = { sendAllSubjectsOption = it },
                        onEmployeeOptionChange = { sendAllEmployeesOption = it },
                    )
                    ShareScheduleStep.RECIPIENT -> ShareScheduleRecipientStep(
                        recipient = targetRecipient,
                        allFriends = allFriends,
                        onSelectedRecipient = { targetRecipient = it },
                    )
                }
            }
            CircularStepsRow(
                stepsCount = ShareScheduleStep.entries.size,
                currentStep = pagerState.currentPage,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val scope = rememberCoroutineScope()
                when (ShareScheduleStep.byNumber(pagerState.currentPage)) {
                    ShareScheduleStep.ORGANIZATIONS -> {
                        FilledTonalButton(
                            enabled = selectedOrganizations.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareScheduleStep.OPTIONS.number)
                                }
                            },
                            content = { Text(text = ProfileThemeRes.strings.nextStepButtonTitle) },
                        )
                    }
                    ShareScheduleStep.OPTIONS -> {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareScheduleStep.RECIPIENT.number)
                                }
                            },
                            content = { Text(text = ProfileThemeRes.strings.nextStepButtonTitle) },
                        )
                        Button(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareScheduleStep.ORGANIZATIONS.number)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            content = { Text(text = ProfileThemeRes.strings.previousStepButtonTitle) },
                        )
                    }
                    ShareScheduleStep.RECIPIENT -> {
                        var isSendSchedule by rememberSaveable { mutableStateOf(false) }
                        Button(
                            enabled = selectedOrganizations.isNotEmpty() && targetRecipient != null && !isLoadingSend,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val shareSchedulesSendData = ShareSchedulesSendDataUi(
                                    recipient = checkNotNull(targetRecipient),
                                    organizations = selectedOrganizations.map { it.uid },
                                    sendAllSubjects = sendAllSubjectsOption,
                                    sendAllEmployee = sendAllEmployeesOption,
                                )
                                onShareSchedule(shareSchedulesSendData)
                                isSendSchedule = true
                            },
                        ) {
                            if (!isLoadingSend) {
                                Text(text = ProfileThemeRes.strings.sendScheduleButtonTitle)
                            } else {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                            LaunchedEffect(isSendSchedule, isLoadingSend) {
                                if (isSendSchedule && !isLoadingSend) onDismissRequest()
                            }
                        }
                        Button(
                            enabled = !isLoadingSend,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(ShareScheduleStep.OPTIONS.number)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            content = { Text(text = ProfileThemeRes.strings.previousStepButtonTitle) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareScheduleOrganizationsStep(
    modifier: Modifier = Modifier,
    selectedOrganizations: List<OrganizationShortUi>,
    allOrganizations: List<OrganizationShortUi>,
    onSelectOrganization: (OrganizationShortUi) -> Unit,
    onUnselectOrganization: (OrganizationShortUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = ProfileThemeRes.strings.choosingOrganizationsStepHeader,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = ProfileThemeRes.strings.choosingOrganizationsStepLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allOrganizations, key = { it.uid }) { organization ->
                SharedOrganizationView(
                    checked = selectedOrganizations.contains(organization),
                    type = organization.type,
                    name = organization.shortName,
                    onCheckedChange = { isChecked ->
                        if (isChecked) onSelectOrganization(organization) else onUnselectOrganization(organization)
                    }
                )
            }
        }
    }
}

@Composable
private fun SharedOrganizationView(
    modifier: Modifier = Modifier,
    checked: Boolean,
    type: OrganizationType,
    name: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(60.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp, 36.dp),
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.full,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material.Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(type.mapToIcon(StudyAssistantRes.icons)),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.mapToSting(StudyAssistantRes.strings),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun ShareScheduleOptionsStep(
    modifier: Modifier = Modifier,
    sendAllSubjectsOption: Boolean,
    sendAllEmployeesOption: Boolean,
    onSubjectOptionChange: (Boolean) -> Unit,
    onEmployeeOptionChange: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = ProfileThemeRes.strings.specifyRecipientStepHeader,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = ProfileThemeRes.strings.specifyRecipientStepLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SharedScheduleOptionView(
                checked = sendAllSubjectsOption,
                title = ProfileThemeRes.strings.sendAllSubjectsOptionTitle,
                description = ProfileThemeRes.strings.sendAllSubjectsOptionDescription,
                onCheckedChange = onSubjectOptionChange,
            )
            SharedScheduleOptionView(
                checked = sendAllEmployeesOption,
                title = ProfileThemeRes.strings.sendAllEmployeesOptionTitle,
                description = ProfileThemeRes.strings.sendAllEmployeesOptionDescription,
                onCheckedChange = onEmployeeOptionChange,
            )
        }
    }
}

@Composable
private fun SharedScheduleOptionView(
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    description: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Switch(
            enabled = enabled,
            checked = checked,
            onCheckedChange = onCheckedChange,
            thumbContent = {
                AnimatedVisibility(
                    visible = checked,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                    )
                }
            }
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ShareScheduleRecipientStep(
    modifier: Modifier = Modifier,
    recipient: AppUserUi?,
    allFriends: List<AppUserUi>,
    onSelectedRecipient: (AppUserUi?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = ProfileThemeRes.strings.specifyRecipientStepHeader,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = ProfileThemeRes.strings.specifyRecipientStepLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(allFriends, key = { it.uid }) { friendUser ->
                UserView(
                    selected = friendUser.uid == recipient?.uid,
                    name = friendUser.username,
                    email = friendUser.email,
                    avatar = friendUser.avatar,
                    onSelect = { onSelectedRecipient(friendUser) },
                )
            }
        }
    }
}

@Composable
internal fun UserView(
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    name: String,
    email: String,
    avatar: String?,
    onSelect: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarView(
            modifier = Modifier.size(40.dp),
            firstName = name.split(' ').getOrElse(0) { "-" },
            secondName = name.split(' ').getOrNull(1),
            imageUrl = avatar,
        )
        Row(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                RadioButton(
                    selected = selected,
                    onClick = onSelect,
                    modifier = Modifier.size(40.dp),
                    enabled = enabled,
                )
            }
        }
    }
}

internal enum class ShareScheduleStep(val number: Int) {
    ORGANIZATIONS(0), OPTIONS(1), RECIPIENT(2);

    companion object {
        fun byNumber(number: Int) = when (number) {
            0 -> ORGANIZATIONS
            1 -> OPTIONS
            2 -> RECIPIENT
            else -> ORGANIZATIONS
        }
    }
}