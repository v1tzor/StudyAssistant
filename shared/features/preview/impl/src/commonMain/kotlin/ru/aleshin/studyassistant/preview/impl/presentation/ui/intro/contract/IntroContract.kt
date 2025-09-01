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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 14.04.2024
 */
@Serializable
internal object IntroState : StoreState

internal sealed class IntroEvent : StoreEvent {
    data class SelectedNextPage(val currentPage: Int) : IntroEvent()
    data class SelectedPreviousPage(val currentPage: Int) : IntroEvent()
    data object ClickLogin : IntroEvent()
    data object ClickRegister : IntroEvent()
}

internal sealed class IntroEffect : StoreEffect {
    data class ShowError(val failures: PreviewFailures) : IntroEffect()
    data class ScrollToPage(val pageIndex: Int) : IntroEffect()
}

internal object IntroAction : StoreAction

internal sealed class IntroOutput : BaseOutput {
    data object NavigateToLogin : IntroOutput()
    data object NavigateToRegister : IntroOutput()
}