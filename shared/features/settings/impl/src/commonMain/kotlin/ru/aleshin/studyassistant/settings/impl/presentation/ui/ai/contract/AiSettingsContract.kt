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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.AiSettingsUi

/**
 * @author Stanislav Aleshin on 05.07.2026.
 */
@Serializable
internal data class AiSettingsState(
    val settings: AiSettingsUi? = null,
    val isSaving: Boolean = false,
) : StoreState

internal sealed class AiSettingsEvent : StoreEvent {
    data object Started : AiSettingsEvent()
    data object SelectSharedService : AiSettingsEvent()
    data object SelectPersonalService : AiSettingsEvent()
    data class TestPersonalKey(val apiKey: String) : AiSettingsEvent()
    data class SavePersonalKey(val apiKey: String) : AiSettingsEvent()
    data object DeletePersonalKey : AiSettingsEvent()
}

internal sealed class AiSettingsEffect : StoreEffect {
    data class ShowError(val failure: SettingsFailures) : AiSettingsEffect()
    data object PersonalKeyTested : AiSettingsEffect()
    data object PersonalKeySaved : AiSettingsEffect()
}

internal sealed class AiSettingsAction : StoreAction {
    data class UpdateSettings(val settings: AiSettingsUi) : AiSettingsAction()
    data class UpdateSaving(val isSaving: Boolean) : AiSettingsAction()
}
