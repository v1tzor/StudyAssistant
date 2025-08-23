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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.forgot.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.ForgotCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
@Serializable
internal data class ForgotState(
    val isLoading: Boolean = false,
    val emailValidError: EmailValidError? = null,
) : StoreState

internal sealed class ForgotEvent : StoreEvent {
    data class ClickResetPassword(val credentials: ForgotCredentialsUi) : ForgotEvent()
    data object ClickLogin : ForgotEvent()
}

internal sealed class ForgotEffect : StoreEffect {
    data class ShowError(val failures: AuthFailures) : ForgotEffect()
}

internal sealed class ForgotAction : StoreAction {
    data class UpdateLoading(val isLoading: Boolean) : ForgotAction()
    data class UpdateValidError(val email: EmailValidError?) : ForgotAction()
}

internal sealed class ForgotOutput : BaseOutput {
    data object NavigateToLogin : ForgotOutput()
}