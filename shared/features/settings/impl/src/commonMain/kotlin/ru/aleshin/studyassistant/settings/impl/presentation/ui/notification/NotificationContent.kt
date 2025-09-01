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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSwitchView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.BeforeTimeChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.ExceptionOrganizationsChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.ReminderTimeChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.WorkloadRateChip

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
@Composable
internal fun NotificationContent(
    notificationComponent: NotificationComponent,
    modifier: Modifier = Modifier,
) {
    val store = notificationComponent.store
    val state by store.stateAsState()
    val strings = SettingsThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            NotificationContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onUpdateBeggingOfClassesNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateBeggingOfClassesNotify(it))
                },
                onUpdateBeggingOfClassesExceptions = {
                    store.dispatchEvent(NotificationEvent.UpdateBeggingOfClassesExceptions(it))
                },
                onUpdateEndOfClassesNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateEndOfClassesNotify(it))
                },
                onUpdateEndOfClassesExceptions = {
                    store.dispatchEvent(NotificationEvent.UpdateEndOfClassesExceptions(it))
                },
                onUpdateUnfinishedHomeworksNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateUnfinishedHomeworksNotify(it))
                },
                onUpdateWorkloadWarningNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateHighWorkloadWarningNotify(it))
                },
                onOpenBillingScreen = {
                    store.dispatchEvent(NotificationEvent.NavigateToBilling)
                }
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is NotificationEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun NotificationContent(
    state: NotificationState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateBeggingOfClassesNotify: (Long?) -> Unit,
    onUpdateBeggingOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateEndOfClassesNotify: (Boolean) -> Unit,
    onUpdateEndOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateUnfinishedHomeworksNotify: (Long?) -> Unit,
    onUpdateWorkloadWarningNotify: (Int?) -> Unit,
    onOpenBillingScreen: () -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSwitchView(
            enabled = settings != null,
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = settings?.beginningOfClasses != null,
            isPaidUser = isPaidUser,
            onBuyContent = onOpenBillingScreen,
            onCheckedChange = { isChecked ->
                val result = if (isChecked) NotificationSettings.BEFORE_BEGINNING_CLASSES_NOTIFY_TIME else null
                onUpdateBeggingOfClassesNotify(result)
            },
            title = SettingsThemeRes.strings.beggingOfClassesNotifyTitle,
            description = SettingsThemeRes.strings.beggingOfClassesNotifyDescription,
        ) {
            if (settings?.beginningOfClasses != null) {
                ExceptionOrganizationsChip(
                    exceptions = settings.exceptionsForBeginningOfClasses,
                    allOrganizations = allOrganizations,
                    onUpdateExceptions = onUpdateBeggingOfClassesExceptions,
                )
                BeforeTimeChip(
                    selectedTime = settings.beginningOfClasses,
                    onTimeChange = onUpdateBeggingOfClassesNotify,
                )
            }
        }
        SettingsSwitchView(
            enabled = settings != null,
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = settings?.endOfClasses == true,
            isPaidUser = isPaidUser,
            onBuyContent = onOpenBillingScreen,
            onCheckedChange = { isChecked -> onUpdateEndOfClassesNotify(isChecked) },
            title = SettingsThemeRes.strings.endOfClassesNotifyTitle,
            description = SettingsThemeRes.strings.endOfClassesNotifyDescription,
        ) {
            if (settings?.endOfClasses == true) {
                ExceptionOrganizationsChip(
                    exceptions = settings.exceptionsForEndOfClasses,
                    allOrganizations = allOrganizations,
                    onUpdateExceptions = onUpdateEndOfClassesExceptions,
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp))
        SettingsSwitchView(
            enabled = settings != null,
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = settings?.unfinishedHomeworks != null,
            isPaidUser = isPaidUser,
            onBuyContent = onOpenBillingScreen,
            onCheckedChange = { isChecked ->
                val result = if (isChecked) NotificationSettings.UNFINISHED_HOMEWORKS_NOTIFY_TIME else null
                onUpdateUnfinishedHomeworksNotify(result)
            },
            title = SettingsThemeRes.strings.unfinishedHomeworksNotifyTitle,
            description = SettingsThemeRes.strings.unfinishedHomeworksNotifyDescription,
        ) {
            if (settings?.unfinishedHomeworks != null) {
                ReminderTimeChip(
                    selectedTime = settings.unfinishedHomeworks,
                    onTimeChange = onUpdateUnfinishedHomeworksNotify,
                )
            }
        }
        SettingsSwitchView(
            enabled = settings != null,
            modifier = Modifier.padding(horizontal = 16.dp),
            checked = settings?.highWorkload != null,
            isPaidUser = isPaidUser,
            onBuyContent = onOpenBillingScreen,
            onCheckedChange = { isChecked ->
                val result = if (isChecked) NotificationSettings.WORKLOAD_HIGH_VALUE else null
                onUpdateWorkloadWarningNotify(result)
            },
            title = SettingsThemeRes.strings.highWorkloadWarningNotifyTitle,
            description = SettingsThemeRes.strings.highWorkloadWarningNotifyDescription,
        ) {
            if (settings?.highWorkload != null) {
                WorkloadRateChip(
                    maxRate = settings.highWorkload,
                    onRateChange = onUpdateWorkloadWarningNotify,
                )
            }
        }
    }
}