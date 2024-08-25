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

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.GeneralSettingsUi

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Immutable
@Parcelize
internal data class GeneralViewState(
    val settings: GeneralSettingsUi? = null,
) : BaseViewState

internal sealed class GeneralEvent : BaseEvent {
    data object Init : GeneralEvent()
    data class ChangeLanguage(val language: LanguageUiType) : GeneralEvent()
    data class ChangeTheme(val theme: ThemeUiType) : GeneralEvent()
}

internal sealed class GeneralEffect : BaseUiEffect {
    data class ShowError(val failures: SettingsFailures) : GeneralEffect()
}

internal sealed class GeneralAction : BaseAction {
    data class UpdateSettings(val settings: GeneralSettingsUi?) : GeneralAction()
}