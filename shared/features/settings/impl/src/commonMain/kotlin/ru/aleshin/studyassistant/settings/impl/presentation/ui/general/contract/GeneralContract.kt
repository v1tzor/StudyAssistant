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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.GeneralSettingsUi

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Serializable
internal data class GeneralState(
    val settings: GeneralSettingsUi? = null,
) : StoreState

internal sealed class GeneralEvent : StoreEvent {
    data object Init : GeneralEvent()
    data class ChangeLanguage(val language: LanguageUiType) : GeneralEvent()
    data class ChangeTheme(val theme: ThemeUiType) : GeneralEvent()
}

internal sealed class GeneralEffect : StoreEffect {
    data class ShowError(val failures: SettingsFailures) : GeneralEffect()
}

internal sealed class GeneralAction : StoreAction {
    data class UpdateSettings(val settings: GeneralSettingsUi?) : GeneralAction()
}