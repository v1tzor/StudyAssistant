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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.AiServiceTypeUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComponent
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.ai_key_saved_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_key_tested_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_description
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_title
import ru.aleshin.studyassistant.settings.impl.resources.delete_ai_key_title
import ru.aleshin.studyassistant.settings.impl.resources.personal_ai_description
import ru.aleshin.studyassistant.settings.impl.resources.personal_ai_key_description
import ru.aleshin.studyassistant.settings.impl.resources.personal_ai_key_label
import ru.aleshin.studyassistant.settings.impl.resources.personal_ai_title
import ru.aleshin.studyassistant.settings.impl.resources.save_ai_key_title
import ru.aleshin.studyassistant.settings.impl.resources.shared_ai_description
import ru.aleshin.studyassistant.settings.impl.resources.shared_ai_title
import ru.aleshin.studyassistant.settings.impl.resources.shared_quota_title
import ru.aleshin.studyassistant.settings.impl.resources.test_ai_key_title

/**
 * @author Stanislav Aleshin on 05.07.2026.
 */
@Composable
internal fun AiSettingsContent(
    component: AiSettingsComponent,
    modifier: Modifier = Modifier,
) {
    val store = component.store
    val state by store.stateAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyTestedMessage = stringResource(Res.string.ai_key_tested_title)
    val keySavedMessage = stringResource(Res.string.ai_key_saved_title)
    var personalKey by remember { mutableStateOf("") }
    var testedPersonalKey by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.ai_settings_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = stringResource(Res.string.ai_settings_description),
            style = MaterialTheme.typography.bodyMedium
        )

        state.settings?.let { settings ->
            ServiceRow(
                title = stringResource(Res.string.shared_ai_title),
                description = stringResource(Res.string.shared_ai_description),
                selected = settings.serviceType == AiServiceTypeUi.SHARED,
                onClick = { store.dispatchEvent(AiSettingsEvent.SelectSharedService) },
            )
            Text(
                text = stringResource(Res.string.shared_quota_title, settings.sharedQuotaRemaining),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            ServiceRow(
                title = stringResource(Res.string.personal_ai_title),
                description = stringResource(Res.string.personal_ai_description),
                selected = settings.serviceType == AiServiceTypeUi.PERSONAL,
                onClick = {
                    if (settings.hasPersonalKey) {
                        store.dispatchEvent(AiSettingsEvent.SelectPersonalService)
                    }
                },
            )
            OutlinedTextField(
                value = personalKey,
                onValueChange = {
                    personalKey = it
                    testedPersonalKey = null
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving,
                shape = MaterialTheme.shapes.large,
                label = { Text(stringResource(Res.string.personal_ai_key_label)) },
                supportingText = { Text(stringResource(Res.string.personal_ai_key_description)) },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledTonalButton(
                    onClick = { store.dispatchEvent(AiSettingsEvent.TestPersonalKey(personalKey)) },
                    modifier = Modifier.weight(1f),
                    enabled = personalKey.isNotBlank() && !state.isSaving,
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                    }
                    Text(stringResource(Res.string.test_ai_key_title))
                }
                Button(
                    onClick = { store.dispatchEvent(AiSettingsEvent.SavePersonalKey(personalKey)) },
                    modifier = Modifier.weight(1f),
                    enabled = personalKey.isNotBlank() &&
                        testedPersonalKey == personalKey &&
                        !state.isSaving,
                ) {
                    Text(stringResource(Res.string.save_ai_key_title))
                }
            }
            if (settings.hasPersonalKey) {
                OutlinedButton(
                    onClick = {
                        personalKey = ""
                        testedPersonalKey = null
                        store.dispatchEvent(AiSettingsEvent.DeletePersonalKey)
                    },
                    enabled = !state.isSaving,
                ) {
                    Text(stringResource(Res.string.delete_ai_key_title))
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState) { ErrorSnackbar(it) }
    }

    store.handleEffects { effect ->
        when (effect) {
            is AiSettingsEffect.ShowError -> snackbarHostState.showSnackbar(
                message = effect.failure.mapToMessage(),
                withDismissAction = true,
            )
            is AiSettingsEffect.PersonalKeyTested -> {
                testedPersonalKey = personalKey
                snackbarHostState.showSnackbar(keyTestedMessage)
            }
            is AiSettingsEffect.PersonalKeySaved -> {
                personalKey = ""
                testedPersonalKey = null
                snackbarHostState.showSnackbar(keySavedMessage)
            }
        }
    }
}

@Composable
private fun ServiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
