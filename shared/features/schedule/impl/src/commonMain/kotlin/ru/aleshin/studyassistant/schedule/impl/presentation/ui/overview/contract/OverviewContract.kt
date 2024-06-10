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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Parcelize
internal data class OverviewViewState(
    val isLoading: Boolean = true,
) : BaseViewState

internal sealed class OverviewEvent : BaseEvent {
    data object Init : OverviewEvent()
    data object SelectedCurrentDay : OverviewEvent()
    data object NavigateToDetails : OverviewEvent()
}

internal sealed class OverviewEffect : BaseUiEffect {
    data class ShowError(val failures: ScheduleFailures) : OverviewEffect()
    data class NavigateToLocal(val pushScreen: Screen) : OverviewEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : OverviewEffect()
}

internal sealed class OverviewAction : BaseAction {
    data class UpdateLoading(val isLoading: Boolean) : OverviewAction()
}