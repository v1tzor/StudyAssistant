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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
@Serializable
internal data class LoginState(
    val isLoading: Boolean = false,
    val isAvailableGoogle: Boolean = false,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : StoreState

internal sealed class LoginEvent : StoreEvent {
    data object Started : LoginEvent()
    data class SubmitCredentials(val credentials: LoginCredentialsUi) : LoginEvent()
    data class SocialNetworkAuthSucceeded(val session: UserSession) : LoginEvent()
    data object ClickSignUp : LoginEvent()
    data object ClickForgotPassword : LoginEvent()
}

internal sealed class LoginEffect : StoreEffect {
    data class ShowError(val failure: AuthFailures) : LoginEffect()
}

internal sealed class LoginAction : StoreAction {
    data class UpdateLoading(val isLoading: Boolean) : LoginAction()
    data class UpdateGoogleAvailability(val isAvailable: Boolean) : LoginAction()
    data class UpdateValidErrors(
        val email: EmailValidError?,
        val password: PasswordValidError?
    ) : LoginAction()
}

internal sealed class LoginOutput : BaseOutput {
    data object NavigateToSignUp : LoginOutput()
    data object NavigateToRecovery : LoginOutput()
    data object NavigateToVerification : LoginOutput()
    data object NavigateToFirstSetup : LoginOutput()
    data object NavigateToApp : LoginOutput()
}