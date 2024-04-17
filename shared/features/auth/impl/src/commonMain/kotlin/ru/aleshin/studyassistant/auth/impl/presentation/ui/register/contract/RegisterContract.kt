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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.NicknameValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.RegisterCredentialsUi

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
internal data class RegisterViewState(
    val isLoading: Boolean = false,
    val nicknameValidError: NicknameValidError? = null,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : BaseViewState

internal sealed class RegisterEvent : BaseEvent {
    data class PressRegisterButton(val credentials: RegisterCredentialsUi) : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
}

internal sealed class RegisterEffect : BaseUiEffect {
    data class PushScreen(val screen: Screen) : RegisterEffect()
    data class PushGlobalScreen(val screen: Screen) : RegisterEffect()
    data class ShowError(val failures: AuthFailures) : RegisterEffect()
}

internal sealed class RegisterAction : BaseAction {
    data class UpdateLoading(val isLoading: Boolean) : RegisterAction()
    data class UpdateValidErrors(
        val nickname: NicknameValidError?,
        val email: EmailValidError?,
        val password: PasswordValidError?,
    ) : RegisterAction()
}
