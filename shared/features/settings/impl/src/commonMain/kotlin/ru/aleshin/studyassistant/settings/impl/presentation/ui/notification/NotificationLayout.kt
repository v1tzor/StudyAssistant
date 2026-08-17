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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.settings.impl.presentation.ui.SettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsExpandedPane
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSwitchView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.BeforeTimeChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.ExceptionOrganizationsChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.ReminderTimeChip
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views.WorkloadRateChip
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.begging_of_classes_notify_description
import ru.aleshin.studyassistant.settings.impl.resources.begging_of_classes_notify_title
import ru.aleshin.studyassistant.settings.impl.resources.end_of_classes_notify_description
import ru.aleshin.studyassistant.settings.impl.resources.end_of_classes_notify_title
import ru.aleshin.studyassistant.settings.impl.resources.high_workload_warning_notify_description
import ru.aleshin.studyassistant.settings.impl.resources.high_workload_warning_notify_title
import ru.aleshin.studyassistant.settings.impl.resources.unfinished_homeworks_notify_description
import ru.aleshin.studyassistant.settings.impl.resources.unfinished_homeworks_notify_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun NotificationLayout(
    modifier: Modifier = Modifier,
    layoutMode: SettingsLayoutMode,
    state: NotificationState,
    onUpdateBeggingOfClassesNotify: (Long?) -> Unit,
    onUpdateBeggingOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateEndOfClassesNotify: (Boolean) -> Unit,
    onUpdateEndOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateUnfinishedHomeworksNotify: (Long?) -> Unit,
    onUpdateWorkloadWarningNotify: (Int?) -> Unit,
) {
    when (layoutMode) {
        SettingsLayoutMode.COMPACT -> NotificationCompactLayout(
            modifier = modifier,
            state = state,
            onUpdateBeggingOfClassesNotify = onUpdateBeggingOfClassesNotify,
            onUpdateBeggingOfClassesExceptions = onUpdateBeggingOfClassesExceptions,
            onUpdateEndOfClassesNotify = onUpdateEndOfClassesNotify,
            onUpdateEndOfClassesExceptions = onUpdateEndOfClassesExceptions,
            onUpdateUnfinishedHomeworksNotify = onUpdateUnfinishedHomeworksNotify,
            onUpdateWorkloadWarningNotify = onUpdateWorkloadWarningNotify,
        )
        SettingsLayoutMode.EXPANDED -> NotificationExpandedLayout(
            modifier = modifier,
            state = state,
            onUpdateBeggingOfClassesNotify = onUpdateBeggingOfClassesNotify,
            onUpdateBeggingOfClassesExceptions = onUpdateBeggingOfClassesExceptions,
            onUpdateEndOfClassesNotify = onUpdateEndOfClassesNotify,
            onUpdateEndOfClassesExceptions = onUpdateEndOfClassesExceptions,
            onUpdateUnfinishedHomeworksNotify = onUpdateUnfinishedHomeworksNotify,
            onUpdateWorkloadWarningNotify = onUpdateWorkloadWarningNotify,
        )
    }
}

@Composable
private fun NotificationCompactLayout(
    modifier: Modifier = Modifier,
    state: NotificationState,
    onUpdateBeggingOfClassesNotify: (Long?) -> Unit,
    onUpdateBeggingOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateEndOfClassesNotify: (Boolean) -> Unit,
    onUpdateEndOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateUnfinishedHomeworksNotify: (Long?) -> Unit,
    onUpdateWorkloadWarningNotify: (Int?) -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        NotificationSettingsItems(
            itemModifier = Modifier.padding(horizontal = 16.dp),
            dividerModifier = Modifier.padding(horizontal = 24.dp),
            useExpandedStyle = false,
            state = state,
            onUpdateBeggingOfClassesNotify = onUpdateBeggingOfClassesNotify,
            onUpdateBeggingOfClassesExceptions = onUpdateBeggingOfClassesExceptions,
            onUpdateEndOfClassesNotify = onUpdateEndOfClassesNotify,
            onUpdateEndOfClassesExceptions = onUpdateEndOfClassesExceptions,
            onUpdateUnfinishedHomeworksNotify = onUpdateUnfinishedHomeworksNotify,
            onUpdateWorkloadWarningNotify = onUpdateWorkloadWarningNotify,
        )
    }
}

@Composable
private fun NotificationExpandedLayout(
    modifier: Modifier = Modifier,
    state: NotificationState,
    onUpdateBeggingOfClassesNotify: (Long?) -> Unit,
    onUpdateBeggingOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateEndOfClassesNotify: (Boolean) -> Unit,
    onUpdateEndOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateUnfinishedHomeworksNotify: (Long?) -> Unit,
    onUpdateWorkloadWarningNotify: (Int?) -> Unit,
) {
    SettingsExpandedPane(modifier = modifier) {
        NotificationSettingsItems(
            itemModifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
            dividerModifier = Modifier,
            useExpandedStyle = true,
            state = state,
            onUpdateBeggingOfClassesNotify = onUpdateBeggingOfClassesNotify,
            onUpdateBeggingOfClassesExceptions = onUpdateBeggingOfClassesExceptions,
            onUpdateEndOfClassesNotify = onUpdateEndOfClassesNotify,
            onUpdateEndOfClassesExceptions = onUpdateEndOfClassesExceptions,
            onUpdateUnfinishedHomeworksNotify = onUpdateUnfinishedHomeworksNotify,
            onUpdateWorkloadWarningNotify = onUpdateWorkloadWarningNotify,
        )
    }
}

@Composable
private fun NotificationSettingsItems(
    itemModifier: Modifier,
    dividerModifier: Modifier,
    useExpandedStyle: Boolean,
    state: NotificationState,
    onUpdateBeggingOfClassesNotify: (Long?) -> Unit,
    onUpdateBeggingOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateEndOfClassesNotify: (Boolean) -> Unit,
    onUpdateEndOfClassesExceptions: (List<UID>) -> Unit,
    onUpdateUnfinishedHomeworksNotify: (Long?) -> Unit,
    onUpdateWorkloadWarningNotify: (Int?) -> Unit,
) = with(state) {
    SettingsSwitchView(
        enabled = settings != null,
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        checked = settings?.beginningOfClasses != null,
        onCheckedChange = { isChecked ->
            val result = if (isChecked) NotificationSettings.BEFORE_BEGINNING_CLASSES_NOTIFY_TIME else null
            onUpdateBeggingOfClassesNotify(result)
        },
        title = stringResource(Res.string.begging_of_classes_notify_title),
        description = stringResource(Res.string.begging_of_classes_notify_description),
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
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        checked = settings?.endOfClasses == true,
        onCheckedChange = { isChecked -> onUpdateEndOfClassesNotify(isChecked) },
        title = stringResource(Res.string.end_of_classes_notify_title),
        description = stringResource(Res.string.end_of_classes_notify_description),
    ) {
        if (settings?.endOfClasses == true) {
            ExceptionOrganizationsChip(
                exceptions = settings.exceptionsForEndOfClasses,
                allOrganizations = allOrganizations,
                onUpdateExceptions = onUpdateEndOfClassesExceptions,
            )
        }
    }
    HorizontalDivider(modifier = dividerModifier)
    SettingsSwitchView(
        enabled = settings != null,
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        checked = settings?.unfinishedHomeworks != null,
        onCheckedChange = { isChecked ->
            val result = if (isChecked) NotificationSettings.UNFINISHED_HOMEWORKS_NOTIFY_TIME else null
            onUpdateUnfinishedHomeworksNotify(result)
        },
        title = stringResource(Res.string.unfinished_homeworks_notify_title),
        description = stringResource(Res.string.unfinished_homeworks_notify_description),
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
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        checked = settings?.highWorkload != null,
        onCheckedChange = { isChecked ->
            val result = if (isChecked) NotificationSettings.WORKLOAD_HIGH_VALUE else null
            onUpdateWorkloadWarningNotify(result)
        },
        title = stringResource(Res.string.high_workload_warning_notify_title),
        description = stringResource(Res.string.high_workload_warning_notify_description),
    ) {
        if (settings?.highWorkload != null) {
            WorkloadRateChip(
                maxRate = settings.highWorkload,
                onRateChange = onUpdateWorkloadWarningNotify,
            )
        }
    }
}
