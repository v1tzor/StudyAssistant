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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract

import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.user.AppUserUi
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
@Parcelize
internal data class VerificationViewState(
    val isLoadingSend: Boolean = false,
    val appUser: AppUserUi? = null,
    val retryAvailableTime: Long? = null,
) : BaseViewState

internal sealed class VerificationEvent : BaseEvent {
    data object Init : VerificationEvent()
    data object SendEmailVerification : VerificationEvent()
    data object SignOut : VerificationEvent()
}

internal sealed class VerificationEffect : BaseUiEffect {
    data class ShowError(val failures: AuthFailures) : VerificationEffect()
    data class ReplaceScreen(val screen: Screen) : VerificationEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : VerificationEffect()
}

internal sealed class VerificationAction : BaseAction {
    data class UpdateAppUser(val appUser: AppUserUi) : VerificationAction()
    data class UpdateRetryAvailableTime(val retryAvailableTime: Long?) : VerificationAction()
    data class UpdateLoadingSend(val isLoading: Boolean) : VerificationAction()
}