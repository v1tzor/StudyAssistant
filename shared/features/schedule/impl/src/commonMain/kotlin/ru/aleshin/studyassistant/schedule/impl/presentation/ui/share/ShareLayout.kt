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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.views.ShareQrCode
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthTimeFormat
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.OrganizationDataLinker
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.sections.SharedScheduleSection
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.copy_link_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_description
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_title
import ru.aleshin.studyassistant.schedule.impl.resources.new_and_linked_data_section_label
import ru.aleshin.studyassistant.schedule.impl.resources.new_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.other_error_message
import ru.aleshin.studyassistant.schedule.impl.resources.receive_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.receive_share_title
import ru.aleshin.studyassistant.schedule.impl.resources.scan_qr_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_claimed_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_label
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_ready_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_consumed_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_expired_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_expires_at_label
import ru.aleshin.studyassistant.schedule.impl.resources.share_import_success_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_invalid_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_offline_title
import ru.aleshin.studyassistant.schedule.impl.resources.shared_schedule_sender_header
import ru.aleshin.studyassistant.schedule.impl.resources.try_again_button_title

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
internal fun ShareLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    onCodeChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onClaimClick: () -> Unit,
    onScanClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onResetClick: () -> Unit,
    onLinkOrganization: (UID, UID?) -> Unit,
    onLinkSubjects: (UID, Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (UID, Map<UID, EmployeeUi>) -> Unit,
) {
    Crossfade(
        modifier = modifier.animateContentSize(),
        targetState = state.status,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Spring.DefaultDisplacementThreshold,
        ),
    ) { status ->
        when (status) {
            ShareStatus.INPUT -> ShareInputLayout(
                state = state,
                onCodeChange = onCodeChange,
                onCreateClick = onCreateClick,
                onClaimClick = onClaimClick,
                onScanClick = onScanClick,
            )
            ShareStatus.READY -> ShareReadyLayout(
                state = state,
                onCopyLinkClick = onCopyLinkClick,
                onResetClick = onResetClick,
            )
            ShareStatus.PREVIEW -> SharePreviewLayout(
                state = state,
                onLinkOrganization = onLinkOrganization,
                onLinkSubjects = onLinkSubjects,
                onLinkTeachers = onLinkTeachers,
            )
            ShareStatus.LOADING, ShareStatus.IMPORTING -> {
                ShareLoadingState()
            }
            ShareStatus.SUCCESS -> ShareResultState(
                title = stringResource(Res.string.share_import_success_title),
                onResetClick = onResetClick,
            )
            ShareStatus.INVALID -> ShareResultState(
                title = stringResource(Res.string.share_invalid_title),
                onResetClick = onResetClick,
            )
            ShareStatus.EXPIRED -> ShareResultState(
                title = stringResource(Res.string.share_expired_title),
                onResetClick = onResetClick,
            )
            ShareStatus.CLAIMED -> ShareResultState(
                title = stringResource(Res.string.share_claimed_title),
                onResetClick = onResetClick,
            )
            ShareStatus.CONSUMED -> ShareResultState(
                title = stringResource(Res.string.share_consumed_title),
                onResetClick = onResetClick,
            )
            ShareStatus.OFFLINE -> ShareResultState(
                title = stringResource(Res.string.share_offline_title),
                onResetClick = onResetClick,
            )
            ShareStatus.ERROR -> ShareResultState(
                title = stringResource(Res.string.other_error_message),
                onResetClick = onResetClick,
            )
        }
    }
}

@Composable
private fun ShareInputLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    onCodeChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onClaimClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.create_share_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(Res.string.create_share_description),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onCreateClick,
            ) {
                Text(text = stringResource(Res.string.create_share_button_title))
            }
        }
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(Res.string.receive_share_title),
                style = MaterialTheme.typography.titleLarge,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.code,
                onValueChange = onCodeChange,
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                label = { Text(text = stringResource(Res.string.share_code_label)) },
                placeholder = { Text(text = stringResource(Res.string.share_code_placeholder)) },
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = state.code.count { character -> character.isLetterOrDigit() } == SHARE_CODE_LENGTH,
                onClick = onClaimClick,
            ) {
                Text(text = stringResource(Res.string.receive_share_button_title))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onScanClick,
            ) {
                Text(text = stringResource(Res.string.scan_qr_button_title))
            }
        }
    }
}

@Composable
private fun ShareReadyLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    onCopyLinkClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    val link = checkNotNull(state.link)
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.share_code_ready_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            SelectionContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = link.code,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    ShareQrCode(
                        modifier = Modifier.size(220.dp),
                        content = link.deepLink,
                    )
                    val expiresAtFormat = DateTimeComponents.Formats.shortDayMonthTimeFormat()
                    Text(
                        text = "${stringResource(Res.string.share_expires_at_label)}: ${link.expiresAt.formatByTimeZone(expiresAtFormat)}",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCopyLinkClick,
        ) {
            Text(text = stringResource(Res.string.copy_link_button_title))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onResetClick,
        ) {
            Text(text = stringResource(Res.string.new_share_button_title))
        }
    }
}

@Composable
private fun SharePreviewLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    onLinkOrganization: (UID, UID?) -> Unit,
    onLinkSubjects: (UID, Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (UID, Map<UID, EmployeeUi>) -> Unit,
) {
    val claim = checkNotNull(state.claim)
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.shared_schedule_sender_header),
                style = MaterialTheme.typography.titleMedium,
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = claim.senderName,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        SharedScheduleSection(
            linkedSchedules = state.linkedSchedules,
            maxNumberOfWeek = state.maxNumberOfWeek,
        )
        ShareDataLinkDivider()
        OrganizationDataLinker(
            isLoading = false,
            isLoadingLinkedOrganization = state.isLoadingLinkedOrganization,
            allOrganizations = state.allOrganizations,
            organizationsLinkData = state.organizationsLinkData,
            onLinkOrganization = onLinkOrganization,
            onLinkSubjects = onLinkSubjects,
            onLinkTeachers = onLinkTeachers,
        )
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun ShareLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ShareResultState(
    modifier: Modifier = Modifier,
    title: String,
    onResetClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onResetClick) {
            Text(text = stringResource(Res.string.try_again_button_title))
        }
    }
}

@Composable
private fun ShareDataLinkDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.new_and_linked_data_section_label),
            style = MaterialTheme.typography.labelMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

private const val SHARE_CODE_LENGTH = 12
