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

package ru.aleshin.studyassistant.chat.impl.presentation.ui.assistant.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.assistant_clear_history_description
import ru.aleshin.studyassistant.chat.impl.resources.assistant_expanded_header
import ru.aleshin.studyassistant.chat.impl.resources.assistant_quota_remaining
import ru.aleshin.studyassistant.chat.impl.resources.schedule_import_action_description
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_document_scanner as core_ic_document_scanner

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AssistantExpandedTopBar(
    modifier: Modifier = Modifier,
    quotaRemaining: Int,
    quotaLimit: Int,
    isVisibleClearButton: Boolean,
    onScheduleImport: () -> Unit,
    onClearChatHistory: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                modifier = Modifier.fillMaxWidth(),
                header = stringResource(Res.string.assistant_expanded_header),
                title = stringResource(
                    Res.string.assistant_quota_remaining,
                    quotaRemaining,
                    quotaLimit,
                ),
                textAlign = TextAlign.Start,
                headerStyle = MaterialTheme.typography.titleLarge,
                titleStyle = MaterialTheme.typography.labelLarge,
            )
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TopAppBarButton(
                    imagePainter = painterResource(CoreRes.drawable.core_ic_document_scanner),
                    imageDescription = stringResource(Res.string.schedule_import_action_description),
                    onButtonClick = onScheduleImport,
                )
                AnimatedVisibility(visible = isVisibleClearButton) {
                    TopAppBarButton(
                        enabled = isVisibleClearButton,
                        imageVector = Icons.Outlined.Delete,
                        imageDescription = stringResource(Res.string.assistant_clear_history_description),
                        onButtonClick = onClearChatHistory,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
