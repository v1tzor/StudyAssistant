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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 14.04.2024
 */
@Parcelize
internal sealed class IntroViewState : BaseViewState {
    data object Default : IntroViewState()
}

internal sealed class IntroEvent : BaseEvent {
    data class NextPage(val currentPage: Int) : IntroEvent()
    data class PreviousPage(val currentPage: Int) : IntroEvent()
    data object NavigateToLogin : IntroEvent()
    data object NavigateToRegister : IntroEvent()
}

internal sealed class IntroEffect : BaseUiEffect {
    data class ShowError(val failures: PreviewFailures) : IntroEffect()
    data class ScrollToPage(val pageIndex: Int) : IntroEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : IntroEffect()
}

internal sealed class IntroAction : BaseAction
