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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.PasswordValidError

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal data class LoginViewState(
    val isLoading: Boolean = false,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : BaseViewState

internal sealed class LoginEvent : BaseEvent {
    data class LoginWithEmail(val credentials: LoginCredentialsUi) : LoginEvent()
    data object LoginViaGoogle : LoginEvent()
    data object NavigateToRegister : LoginEvent()
    data object NavigateToForgot : LoginEvent()
}

internal sealed class LoginEffect : BaseUiEffect {
    data class PushScreen(val screen: Screen) : LoginEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : LoginEffect()
    data class ShowError(val failure: AuthFailures) : LoginEffect()
}

internal sealed class LoginAction : BaseAction {
    data class UpdateLoading(val isLoading: Boolean) : LoginAction()
    data class UpdateValidErrors(
        val email: EmailValidError?,
        val password: PasswordValidError?
    ) : LoginAction()
}