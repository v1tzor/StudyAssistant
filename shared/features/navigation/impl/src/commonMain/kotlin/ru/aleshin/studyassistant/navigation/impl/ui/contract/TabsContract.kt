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
package ru.aleshin.studyassistant.navigation.impl.ui.contract

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
@Immutable
@Parcelize
internal data class TabsViewState(
    val screenKeys: Set<ScreenKey> = emptySet(),
) : BaseViewState

internal sealed class TabsEvent : BaseEvent {
    data object Init : TabsEvent()
    data object SelectedScheduleBottomItem : TabsEvent()
    data object SelectedTasksBottomItem : TabsEvent()
    data object SelectedChatBottomItem : TabsEvent()
    data object SelectedInfoBottomItem : TabsEvent()
    data object SelectedProfileBottomItem : TabsEvent()
}

internal sealed class TabsEffect : BaseUiEffect {
    data class ReplaceScreen(val screen: Screen) : TabsEffect()
}

internal sealed class TabsAction : BaseAction {
    data class ChangeScreenKeys(val keys: Set<ScreenKey>) : TabsAction()
}