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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
@Serializable
internal object TabNavigationState : StoreState

internal sealed class TabNavigationEvent : StoreEvent {
    data object NavigateToGeneral : TabNavigationEvent()
    data object NavigateToNotification : TabNavigationEvent()
    data object NavigateToCalendar : TabNavigationEvent()
    data object NavigateToSubscription : TabNavigationEvent()
    data object NavigateToAboutApp : TabNavigationEvent()
    data object NavigateToBack : TabNavigationEvent()
}

internal sealed class TabNavigationEffect : StoreEffect

internal object TabNavigationAction : StoreAction

internal sealed class TabNavigationOutput : BaseOutput {
    data object NavigateToGeneral : TabNavigationOutput()
    data object NavigateToNotification : TabNavigationOutput()
    data object NavigateToCalendar : TabNavigationOutput()
    data object NavigateToSubscription : TabNavigationOutput()
    data object NavigateToAboutApp : TabNavigationOutput()
    data object NavigateToBack : TabNavigationOutput()
}