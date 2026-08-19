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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import ru.aleshin.studyassistant.core.ui.ads.AdPlacement
import ru.aleshin.studyassistant.core.ui.ads.YandexInlineBanner
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.accept_homework_title
import ru.aleshin.studyassistant.tasks.impl.resources.duplicate_homework_share_title
import ru.aleshin.studyassistant.tasks.impl.resources.expired_homework_share_title
import ru.aleshin.studyassistant.tasks.impl.resources.homework_share_imported_title
import ru.aleshin.studyassistant.tasks.impl.resources.homework_share_subject_item
import ru.aleshin.studyassistant.tasks.impl.resources.invalid_homework_share_title
import ru.aleshin.studyassistant.tasks.impl.resources.open_share_title
import ru.aleshin.studyassistant.tasks.impl.resources.receive_homework_share_title
import ru.aleshin.studyassistant.tasks.impl.resources.scan_qr_button_title
import ru.aleshin.studyassistant.tasks.impl.resources.share_code_label
import ru.aleshin.studyassistant.tasks.impl.resources.share_code_placeholder
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.network_error_message as core_network_error_message
import ru.aleshin.studyassistant.core.ui.resources.ok_confirm_title as core_ok_confirm_title

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
internal fun ShareLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    contentMaxWidth: Dp? = null,
    onCodeChange: (String) -> Unit,
    onOpenClick: () -> Unit,
    onScanClick: () -> Unit,
    onLinkRequest: () -> Unit,
    onResetClick: () -> Unit,
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
            HomeworkShareStatus.INPUT -> ShareInputLayout(
                state = state,
                contentMaxWidth = contentMaxWidth,
                onCodeChange = onCodeChange,
                onOpenClick = onOpenClick,
                onScanClick = onScanClick,
            )
            HomeworkShareStatus.PREVIEW -> SharePreviewLayout(
                state = state,
                contentMaxWidth = contentMaxWidth,
                onLinkRequest = onLinkRequest,
            )
            HomeworkShareStatus.LOADING, HomeworkShareStatus.IMPORTING -> {
                ShareLoadingState()
            }
            HomeworkShareStatus.SUCCESS -> ShareResultState(
                title = stringResource(Res.string.homework_share_imported_title),
                onResetClick = onResetClick,
            )
            HomeworkShareStatus.DUPLICATE -> ShareResultState(
                title = stringResource(Res.string.duplicate_homework_share_title),
                onResetClick = onResetClick,
            )
            HomeworkShareStatus.INVALID -> ShareResultState(
                title = stringResource(Res.string.invalid_homework_share_title),
                onResetClick = onResetClick,
            )
            HomeworkShareStatus.EXPIRED -> ShareResultState(
                title = stringResource(Res.string.expired_homework_share_title),
                onResetClick = onResetClick,
            )
            HomeworkShareStatus.OFFLINE, HomeworkShareStatus.ERROR -> ShareResultState(
                title = stringResource(CoreRes.string.core_network_error_message),
                onResetClick = onResetClick,
            )
        }
    }
}

@Composable
private fun ShareInputLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    contentMaxWidth: Dp? = null,
    onCodeChange: (String) -> Unit,
    onOpenClick: () -> Unit,
    onScanClick: () -> Unit,
) {
    val form: @Composable (Modifier) -> Unit = { formModifier ->
        Column(
            modifier = formModifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(Res.string.receive_homework_share_title),
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
                onClick = onOpenClick,
            ) {
                Text(text = stringResource(Res.string.open_share_title))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onScanClick,
            ) {
                Text(text = stringResource(Res.string.scan_qr_button_title))
            }
            YandexInlineBanner(
                modifier = Modifier.fillMaxWidth(),
                placement = AdPlacement.HOMEWORK_RECEIVE,
            )
        }
    }

    if (contentMaxWidth == null) {
        form(
            modifier
                .fillMaxSize()
                .padding(16.dp),
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                form(Modifier.padding(24.dp))
            }
        }
    }
}

@Composable
private fun SharePreviewLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    contentMaxWidth: Dp? = null,
    onLinkRequest: () -> Unit,
) {
    val share = checkNotNull(state.share)
    val preview: @Composable (Modifier) -> Unit = { paneModifier ->
        Column(
            modifier = paneModifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = share.senderName,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = share.date.formatByTimeZone(DateTimeComponents.Formats.dayMonthYearFormat()),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    share.homeworks.forEach { homework ->
                        Text(
                            text = stringResource(Res.string.homework_share_subject_item, homework.subjectName),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLinkRequest,
            ) {
                Text(text = stringResource(Res.string.accept_homework_title))
            }
        }
    }

    if (contentMaxWidth == null) {
        preview(
            modifier
                .fillMaxSize()
                .padding(16.dp),
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            preview(
                Modifier
                    .fillMaxHeight()
                    .widthIn(max = contentMaxWidth)
                    .fillMaxWidth()
                    .padding(24.dp),
            )
        }
    }
}

@Composable
private fun ShareLoadingState(
    modifier: Modifier = Modifier
) {
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
            Text(text = stringResource(CoreRes.string.core_ok_confirm_title))
        }
    }
}

private const val SHARE_CODE_LENGTH = 12
