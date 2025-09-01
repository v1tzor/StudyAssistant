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

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.NotificationSettingsUi

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
@Serializable
internal data class NotificationState(
    val settings: NotificationSettingsUi? = null,
    val isPaidUser: Boolean = false,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
) : StoreState

internal sealed class NotificationEvent : StoreEvent {
    data object Init : NotificationEvent()
    data class UpdateBeggingOfClassesNotify(val beforeDelay: Long?) : NotificationEvent()
    data class UpdateBeggingOfClassesExceptions(val organizations: List<UID>) : NotificationEvent()
    data class UpdateEndOfClassesNotify(val isNotify: Boolean) : NotificationEvent()
    data class UpdateEndOfClassesExceptions(val organizations: List<UID>) : NotificationEvent()
    data class UpdateUnfinishedHomeworksNotify(val time: Long?) : NotificationEvent()
    data class UpdateHighWorkloadWarningNotify(val maxRate: Int?) : NotificationEvent()
    data object NavigateToBilling : NotificationEvent()
}

internal sealed class NotificationEffect : StoreEffect {
    data class ShowError(val failures: SettingsFailures) : NotificationEffect()
}

internal sealed class NotificationAction : StoreAction {
    data class UpdateSettings(val settings: NotificationSettingsUi) : NotificationAction()
    data class UpdatePaidUserStatus(val isPaidUser: Boolean) : NotificationAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : NotificationAction()
}

internal sealed class NotificationOutput : BaseOutput {
    data object NavigateToBilling : NotificationOutput()
}