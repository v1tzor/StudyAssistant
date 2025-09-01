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

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.user.AppUserUi
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
@Serializable
internal data class VerificationState(
    val appUser: AppUserUi? = null,
    val retryAvailableTime: Long? = null,
) : StoreState

internal sealed class VerificationEvent : StoreEvent {
    data object Started : VerificationEvent()
    data object ClickSendEmail : VerificationEvent()
    data object ClickSignOut : VerificationEvent()
}

internal sealed class VerificationEffect : StoreEffect {
    data class ShowError(val failures: AuthFailures) : VerificationEffect()
}

internal sealed class VerificationAction : StoreAction {
    data class UpdateAppUser(val appUser: AppUserUi) : VerificationAction()
    data class UpdateRetryAvailableTime(val retryAvailableTime: Long?) : VerificationAction()
}

internal sealed class VerificationOutput : BaseOutput {
    data object NavigateToLogin : VerificationOutput()
    data object NavigateToFirstSetup : VerificationOutput()
}