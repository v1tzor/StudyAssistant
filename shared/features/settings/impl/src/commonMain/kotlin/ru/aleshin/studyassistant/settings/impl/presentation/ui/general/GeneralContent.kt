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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.ui.fetchSettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.views.DeleteScheduleDialog
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_warning_text
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_warning_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun GeneralContent(
    generalComponent: GeneralComponent,
    modifier: Modifier = Modifier,
) {
    val store = generalComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    var isDeleteScheduleOpen by remember { mutableStateOf(false) }
    var isDeleteAllDataOpen by remember { mutableStateOf(false) }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchSettingsLayoutMode()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            GeneralLayout(
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
                state = state,
                onSelectedLanguage = { store.dispatchEvent(GeneralEvent.ChangeLanguage(it)) },
                onSelectedTheme = { store.dispatchEvent(GeneralEvent.ChangeTheme(it)) },
                onDeleteScheduleClick = { isDeleteScheduleOpen = true },
                onDeleteAllDataClick = { isDeleteAllDataOpen = true },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.navigationBars
    )

    if (isDeleteScheduleOpen) {
        DeleteScheduleDialog(
            organizations = state.organizations,
            onDismiss = { isDeleteScheduleOpen = false },
            onConfirm = { organizationIds ->
                store.dispatchEvent(GeneralEvent.DeleteCurrentSchedule(organizationIds))
                isDeleteScheduleOpen = false
            },
        )
    }
    if (isDeleteAllDataOpen) {
        WarningAlertDialog(
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
            title = { Text(text = stringResource(Res.string.delete_all_data_warning_title)) },
            text = { Text(text = stringResource(Res.string.delete_all_data_warning_text)) },
            confirmTitle = stringResource(CoreRes.string.core_delete_confirm_title),
            onDismiss = { isDeleteAllDataOpen = false },
            onConfirm = {
                store.dispatchEvent(GeneralEvent.DeleteAllData)
                isDeleteAllDataOpen = false
            },
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is GeneralEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
