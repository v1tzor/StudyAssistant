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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.auth.impl.presentation.models.user.AppUserUi
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.EmailTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEffect
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationEvent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationState
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.store.VerificationComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.views.VerificationTopBar
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
@Composable
internal fun VerificationContent(
    verificationComponent: VerificationComponent,
    modifier: Modifier = Modifier,
) {
    val store = verificationComponent.store
    val strings = AuthThemeRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseVerificationContent(
                state = store.stateAsState().value,
                modifier = Modifier.padding(paddingValues),
                onSendEmailClick = {
                    store.dispatchEvent(VerificationEvent.ClickSendEmail)
                },
            )
        },
        topBar = {
            VerificationTopBar(
                onSignOut = { store.dispatchEvent(VerificationEvent.ClickSignOut) },
            )
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
            is VerificationEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseVerificationContent(
    state: VerificationState,
    modifier: Modifier = Modifier,
    onSendEmailClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(bottom = 32.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.verificationHeadline,
            title = AuthThemeRes.strings.verificationTitle,
            illustration = AuthThemeRes.icons.verificationIllustration,
            contentDescription = AuthThemeRes.strings.registerDesc,
        )
        VerificationDetails(
            modifier = Modifier.padding(horizontal = 24.dp),
            appUser = state.appUser,
            retryAvailableTime = state.retryAvailableTime,
            onSendEmailClick = onSendEmailClick,
        )
    }
}

@Composable
private fun VerificationDetails(
    modifier: Modifier = Modifier,
    appUser: AppUserUi?,
    retryAvailableTime: Long?,
    onSendEmailClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EmailTextField(
            enabled = true,
            readOnly = true,
            email = appUser?.email ?: "",
            onEmailChanged = {},
        )
        VerificationButtonSection(
            retryAvailableTime = retryAvailableTime,
            onSendEmailClick = onSendEmailClick,
        )
    }
}

@Composable
private fun VerificationButtonSection(
    modifier: Modifier = Modifier,
    retryAvailableTime: Long?,
    onSendEmailClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(
            onClick = onSendEmailClick,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            enabled = retryAvailableTime == null,
            shape = MaterialTheme.shapes.large,
        ) {
            Text(text = AuthThemeRes.strings.verificationButtonLabel)
        }

        if (retryAvailableTime != null) {
            RetryTimer(
                retryAvailableTime = retryAvailableTime
            )
        }
    }
}

@Composable
private fun RetryTimer(
    modifier: Modifier = Modifier,
    retryAvailableTime: Long
) {
    Text(
        modifier = modifier,
        text = buildString {
            append(AuthThemeRes.strings.retryAvailableTimeLabelPrefix)
            append(retryAvailableTime.toDuration(MILLISECONDS).toLanguageString())
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}