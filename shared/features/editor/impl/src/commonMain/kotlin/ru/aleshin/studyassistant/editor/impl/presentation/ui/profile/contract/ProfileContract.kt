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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
@Serializable
internal data class ProfileState(
    val isLoading: Boolean = true,
    val profile: ProfileUi? = null,
) : StoreState

internal sealed class ProfileEvent : StoreEvent {
    data object Started : ProfileEvent()
    data class UpdateAvatar(val file: PlatformFile) : ProfileEvent()
    data object DeleteAvatar : ProfileEvent()
    data class UpdateUsername(val name: String) : ProfileEvent()
    data class UpdateBirthday(val text: String?) : ProfileEvent()
    data class UpdateGender(val gender: Gender?) : ProfileEvent()
    data class UpdateCity(val city: String?) : ProfileEvent()
    data object NavigateToBack : ProfileEvent()
}

internal sealed class ProfileEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : ProfileEffect()
}

internal sealed class ProfileAction : StoreAction {
    data class SetupProfile(val profile: ProfileUi) : ProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : ProfileAction()
}

internal sealed class ProfileOutput : BaseOutput {
    data object NavigateToBack : ProfileOutput()
}
