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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.ForgotCredentialsUi

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
internal data class ForgotViewState(
    val isLoading: Boolean = false,
    val emailValidError: EmailValidError? = null,
) : BaseViewState

internal sealed class ForgotEvent : BaseEvent {
    data class SendResetPasswordEmail(val credentials: ForgotCredentialsUi) : ForgotEvent()
    data object NavigateToLogin : ForgotEvent()
}

internal sealed class ForgotEffect : BaseUiEffect {
    data class PushScreen(val screen: Screen) : ForgotEffect()
    data class ShowError(val failures: AuthFailures) : ForgotEffect()
}

internal sealed class ForgotAction : BaseAction {
    data class UpdateLoading(val isLoading: Boolean) : ForgotAction()
    data class UpdateValidError(val email: EmailValidError?) : ForgotAction()
}
