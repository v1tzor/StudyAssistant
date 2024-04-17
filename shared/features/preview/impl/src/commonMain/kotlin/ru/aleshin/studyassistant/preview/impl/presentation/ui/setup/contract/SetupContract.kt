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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal data class SetupViewState(
    val currentStep: Int = 0
) : BaseViewState

internal sealed class SetupEvent : BaseEvent {
    // TODO: Contract Event
}

internal sealed class SetupEffect : BaseUiEffect {
    data class ShowError(val failures: PreviewFailures) : SetupEffect()
    data class ReplacePage(val pageIndex: Int) : SetupEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : SetupEffect()
}

internal sealed class SetupAction : BaseAction {
    // TODO: Contract Action
}