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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.NotificationSettingsUi

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
@Immutable
@Parcelize
internal data class NotificationViewState(
    val settings: NotificationSettingsUi? = null,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
) : BaseViewState

internal sealed class NotificationEvent : BaseEvent {
    data object Init : NotificationEvent()
    data class UpdateBeggingOfClassesNotify(val beforeDelay: Long?) : NotificationEvent()
    data class UpdateBeggingOfClassesExceptions(val organizations: List<UID>) : NotificationEvent()
    data class UpdateEndOfClassesNotify(val isNotify: Boolean) : NotificationEvent()
    data class UpdateEndOfClassesExceptions(val organizations: List<UID>) : NotificationEvent()
    data class UpdateUnfinishedHomeworksNotify(val time: Long?) : NotificationEvent()
    data class UpdateHighWorkloadWarningNotify(val maxRate: Int?) : NotificationEvent()
}

internal sealed class NotificationEffect : BaseUiEffect {
    data class ShowError(val failures: SettingsFailures) : NotificationEffect()
}

internal sealed class NotificationAction : BaseAction {
    data class UpdateSettings(val settings: NotificationSettingsUi) : NotificationAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : NotificationAction()
}