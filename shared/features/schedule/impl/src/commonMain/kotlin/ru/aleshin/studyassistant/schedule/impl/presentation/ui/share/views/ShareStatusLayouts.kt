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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.views.ShareQrCode
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthTimeFormat
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.sections.SharedScheduleSection
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.copy_link_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_description
import ru.aleshin.studyassistant.schedule.impl.resources.create_share_title
import ru.aleshin.studyassistant.schedule.impl.resources.new_and_linked_data_section_label
import ru.aleshin.studyassistant.schedule.impl.resources.new_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.receive_share_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.receive_share_title
import ru.aleshin.studyassistant.schedule.impl.resources.scan_qr_button_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_label
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.share_code_ready_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_expires_at_label
import ru.aleshin.studyassistant.schedule.impl.resources.shared_schedule_sender_header
import ru.aleshin.studyassistant.schedule.impl.resources.try_again_button_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ShareInputSection(
    modifier: Modifier = Modifier,
    state: ShareState,
    onCodeChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onClaimClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
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
internal fun ShareReadySection(
    modifier: Modifier = Modifier,
    state: ShareState,
    centered: Boolean,
    onCopyLinkClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    val link = state.link ?: return
    val contentAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    Column(
        modifier = modifier,
        horizontalAlignment = contentAlignment,
        verticalArrangement = if (centered) {
            Arrangement.Center
        } else {
            Arrangement.spacedBy(16.dp)
        },
    ) {
        Text(
            text = stringResource(Res.string.share_code_ready_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        if (centered) {
            Spacer(modifier = Modifier.height(16.dp))
        }
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
        if (centered) {
            Spacer(modifier = Modifier.height(16.dp))
        }
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
internal fun SharePreviewSection(
    modifier: Modifier = Modifier,
    state: ShareState,
    horizontalPadding: Dp,
    linkerMaxWidth: Dp? = null,
    onLinkOrganization: (UID, UID?) -> Unit,
    onLinkSubjects: (UID, Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (UID, Map<UID, EmployeeUi>) -> Unit,
) {
    val claim = state.claim ?: return
    val senderModifier = if (linkerMaxWidth != null) {
        Modifier
            .widthIn(max = linkerMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    }
    val linkerModifier = if (linkerMaxWidth != null) {
        Modifier.widthIn(max = linkerMaxWidth).fillMaxWidth()
    } else {
        Modifier.fillMaxWidth()
    }

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = senderModifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.shared_schedule_sender_header),
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large
            ) {
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
            horizontalPadding = horizontalPadding,
        )
        ShareDataLinkDivider(
            modifier = Modifier.padding(horizontal = horizontalPadding),
        )
        OrganizationDataLinker(
            modifier = linkerModifier,
            isLoading = false,
            isLoadingLinkedOrganization = state.isLoadingLinkedOrganization,
            allOrganizations = state.allOrganizations,
            organizationsLinkData = state.organizationsLinkData,
            horizontalPadding = horizontalPadding,
            onLinkOrganization = onLinkOrganization,
            onLinkSubjects = onLinkSubjects,
            onLinkTeachers = onLinkTeachers,
        )
        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
internal fun ShareLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
internal fun ShareResultState(
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
internal fun ShareDataLinkDivider(modifier: Modifier = Modifier) {
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

internal const val SHARE_CODE_LENGTH = 12
