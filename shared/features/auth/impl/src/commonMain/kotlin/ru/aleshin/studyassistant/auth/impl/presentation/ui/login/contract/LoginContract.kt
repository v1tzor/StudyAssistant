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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.LoginCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
@Immutable
@Parcelize
internal data class LoginViewState(
    val isLoading: Boolean = false,
    val isAvailableGoogle: Boolean = false,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : BaseViewState

internal sealed class LoginEvent : BaseEvent {
    data object Init : LoginEvent()
    data class LoginWithEmail(val credentials: LoginCredentialsUi) : LoginEvent()
    data class LoginViaGoogle(val idToken: String?) : LoginEvent()
    data object NavigateToRegister : LoginEvent()
    data object NavigateToForgot : LoginEvent()
}

internal sealed class LoginEffect : BaseUiEffect {
    data class ShowError(val failure: AuthFailures) : LoginEffect()
    data class NavigateToLocal(val pushScreen: Screen) : LoginEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : LoginEffect()
}

internal sealed class LoginAction : BaseAction {
    data class UpdateValidErrors(
        val email: EmailValidError?,
        val password: PasswordValidError?
    ) : LoginAction()
    data class UpdateGoogleAvailable(val isAvailable: Boolean) : LoginAction()
    data class UpdateLoading(val isLoading: Boolean) : LoginAction()
}