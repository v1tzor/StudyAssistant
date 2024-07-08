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

import androidx.compose.runtime.Immutable
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.domain.entities.MainFailures
import ru.aleshin.studyassistant.presentation.models.GeneralSettingsUi

/**
 * @author Stanislav Aleshin on 27.01.2024
 */
@Immutable
@Parcelize
data class MainViewState(
    val generalSettings: GeneralSettingsUi = GeneralSettingsUi(),
) : BaseViewState

sealed class MainEvent : BaseEvent {
    data object Init : MainEvent()
}

sealed class MainEffect : BaseUiEffect {
    data class ShowError(val failures: MainFailures) : MainEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : MainEffect()
}

sealed class MainAction : BaseAction {
    data class UpdateSettings(val settings: GeneralSettingsUi) : MainAction()
}