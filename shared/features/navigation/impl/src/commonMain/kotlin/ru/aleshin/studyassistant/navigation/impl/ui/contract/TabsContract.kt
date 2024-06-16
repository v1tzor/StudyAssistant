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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.navigation.impl.ui.views.TabsBottomBarItems

/**
 * @author Stanislav Aleshin on 18.02.2023.
 */
@Parcelize
internal data class TabsViewState(
    val bottomBarItem: TabsBottomBarItems = TabsBottomBarItems.SCHEDULE,
) : BaseViewState

internal sealed class TabsEvent : BaseEvent {
    data object Init : TabsEvent()
    data object SelectedScheduleBottomItem : TabsEvent()
    data object SelectedTasksBottomItem : TabsEvent()
    data object SelectedInfoBottomItem : TabsEvent()
    data object SelectedProfileBottomItem : TabsEvent()
}

internal sealed class TabsEffect : BaseUiEffect {
    data class ReplaceScreen(val screen: Screen) : TabsEffect()
}

internal sealed class TabsAction : BaseAction {
    data class ChangeNavItems(val item: TabsBottomBarItems) : TabsAction()
}