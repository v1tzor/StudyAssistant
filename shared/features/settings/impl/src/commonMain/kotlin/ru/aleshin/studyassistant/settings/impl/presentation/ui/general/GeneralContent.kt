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

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSelectorView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store.GeneralComponent
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_button
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_warning_text
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_warning_title
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_button
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_warning_text
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_warning_title
import ru.aleshin.studyassistant.settings.impl.resources.ic_language
import ru.aleshin.studyassistant.settings.impl.resources.ic_palette
import ru.aleshin.studyassistant.settings.impl.resources.language_chooser_view_title
import ru.aleshin.studyassistant.settings.impl.resources.theme_chooser_view_title
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseGeneralContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
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
        WarningAlertDialog(
            icon = { Icon(imageVector = Icons.Default.Delete, contentDescription = null) },
            title = { Text(text = stringResource(Res.string.delete_schedule_warning_title)) },
            text = { Text(text = stringResource(Res.string.delete_schedule_warning_text)) },
            confirmTitle = stringResource(CoreRes.string.core_delete_confirm_title),
            onDismiss = { isDeleteScheduleOpen = false },
            onConfirm = {
                store.dispatchEvent(GeneralEvent.DeleteCurrentSchedule)
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

@Composable
private fun BaseGeneralContent(
    state: GeneralState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onSelectedLanguage: (LanguageUiType) -> Unit,
    onSelectedTheme: (ThemeUiType) -> Unit,
    onDeleteScheduleClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSelectorView(
            onSelect = onSelectedLanguage,
            modifier = Modifier.padding(horizontal = 16.dp),
            selected = state.settings?.languageType,
            allItems = remember { LanguageUiType.entries.toList() },
            icon = painterResource(Res.drawable.ic_language),
            title = stringResource(Res.string.language_chooser_view_title),
            itemName = { it.mapToString() },
        )
        SettingsSelectorView(
            onSelect = onSelectedTheme,
            modifier = Modifier.padding(horizontal = 16.dp),
            selected = state.settings?.themeType,
            allItems = remember { ThemeUiType.entries.toList() },
            icon = painterResource(Res.drawable.ic_palette),
            title = stringResource(Res.string.theme_chooser_view_title),
            itemName = { it.mapToString() },
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            onClick = onDeleteScheduleClick,
        ) {
            Text(text = stringResource(Res.string.delete_schedule_button))
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            onClick = onDeleteAllDataClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(text = stringResource(Res.string.delete_all_data_button))
        }
    }
}
