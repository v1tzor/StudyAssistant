/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Serializable
internal data class ProfileState(
    val isLoading: Boolean = true,
    val profile: ProfileUi? = null,
) : StoreState

internal sealed class ProfileEvent : StoreEvent {
    data object Started : ProfileEvent()
    data object ClickAboutApp : ProfileEvent()
    data object ClickGeneralSettings : ProfileEvent()
    data object ClickNotifySettings : ProfileEvent()
    data object ClickCalendarSettings : ProfileEvent()
    data object ClickAiSettings : ProfileEvent()
    data object ClickShareSchedule : ProfileEvent()
    data object ClickEditProfile : ProfileEvent()
}

internal sealed class ProfileEffect : StoreEffect {
    data class ShowError(val failures: ProfileFailures) : ProfileEffect()
}

internal sealed class ProfileAction : StoreAction {

    data class UpdateProfile(
        val profile: ProfileUi?,
    ) : ProfileAction()

    data class UpdateLoading(val isLoading: Boolean) : ProfileAction()
}
