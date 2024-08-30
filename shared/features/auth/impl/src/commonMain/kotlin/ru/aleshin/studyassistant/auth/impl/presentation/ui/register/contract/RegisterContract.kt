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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.UsernameValidError
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
@Immutable
@Parcelize
internal data class RegisterViewState(
    val isLoading: Boolean = false,
    val usernameValidError: UsernameValidError? = null,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : BaseViewState

internal sealed class RegisterEvent : BaseEvent {
    data class RegisterNewAccount(val credentials: RegisterCredentialsUi) : RegisterEvent()
    data object NavigateToLogin : RegisterEvent()
}

internal sealed class RegisterEffect : BaseUiEffect {
    data class ShowError(val failures: AuthFailures) : RegisterEffect()
    data class NavigateToLocal(val pushScreen: Screen) : RegisterEffect()
    data class ReplaceScreen(val screen: Screen) : RegisterEffect()
}

internal sealed class RegisterAction : BaseAction {

    data class UpdateValidErrors(
        val username: UsernameValidError?,
        val email: EmailValidError?,
        val password: PasswordValidError?,
    ) : RegisterAction()

    data class UpdateLoading(val isLoading: Boolean) : RegisterAction()
}