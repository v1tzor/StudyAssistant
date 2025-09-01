/*
 * Copyright 2023 Stanislav Aleshin
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

package ru.aleshin.studyassistant.presentation.ui.main.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl
import ru.aleshin.studyassistant.domain.entities.MainFailures
import ru.aleshin.studyassistant.presentation.models.GeneralSettingsUi

/**
 * @author Stanislav Aleshin on 27.01.2024
 */
@Serializable
data class MainState(
    val generalSettings: GeneralSettingsUi = GeneralSettingsUi(),
) : StoreState

sealed class MainEvent : StoreEvent {
    data object StartBackgroundWork : MainEvent()
    data object ExecuteNavigation : MainEvent()
}

sealed class MainEffect : StoreEffect {
    data class ShowError(val failures: MainFailures) : MainEffect()
}

sealed class MainAction : StoreAction {
    data class UpdateSettings(val settings: GeneralSettingsUi) : MainAction()
}

data class MainInput(
    val deepLinkUrl: DeepLinkUrl?,
) : BaseInput

sealed class MainOutput : BaseOutput {
    data object NavigateToIntro : MainOutput()
    data object NavigateToAuth : MainOutput()
    data object NavigateToVerification : MainOutput()
    data object NavigateToSetup : MainOutput()
    data object NavigateToApp : MainOutput()
}