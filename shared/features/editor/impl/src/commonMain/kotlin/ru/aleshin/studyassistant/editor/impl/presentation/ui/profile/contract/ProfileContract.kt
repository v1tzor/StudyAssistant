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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.SocialNetworkUi

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
@Immutable
@Parcelize
internal data class ProfileViewState(
    val isLoading: Boolean = true,
    val appUser: AppUserUi? = null,
) : BaseViewState

internal sealed class ProfileEvent : BaseEvent {
    data object Init : ProfileEvent()
    data class UpdateAvatar(val file: PlatformFile) : ProfileEvent()
    data object DeleteAvatar : ProfileEvent()
    data class UpdateUsername(val name: String) : ProfileEvent()
    data class UpdateDescription(val text: String?) : ProfileEvent()
    data class UpdateBirthday(val text: String?) : ProfileEvent()
    data class UpdateGender(val gender: Gender?) : ProfileEvent()
    data class UpdateCity(val city: String?) : ProfileEvent()
    data class UpdateSocialNetworks(val socialNetworks: List<SocialNetworkUi>) : ProfileEvent()
    data class UpdatePassword(val oldPassword: String, val newPassword: String) : ProfileEvent()
    data object NavigateToBack : ProfileEvent()
}

internal sealed class ProfileEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : ProfileEffect()
    data object NavigateToBack : ProfileEffect()
}

internal sealed class ProfileAction : BaseAction {
    data class SetupAppUser(val user: AppUserUi) : ProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : ProfileAction()
}