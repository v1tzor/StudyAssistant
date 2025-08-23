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

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.PasswordValidError
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.UsernameValidError
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

@Serializable
internal data class RegisterState(
    val isLoading: Boolean = false,
    val usernameValidError: UsernameValidError? = null,
    val emailValidError: EmailValidError? = null,
    val passwordValidError: PasswordValidError? = null,
) : StoreState

internal sealed class RegisterEvent : StoreEvent {
    data class SubmitCredentials(val credentials: RegisterCredentialsUi) : RegisterEvent()
    data object ClickLogin : RegisterEvent()
}

internal sealed class RegisterEffect : StoreEffect {
    data class ShowError(val failures: AuthFailures) : RegisterEffect()
}

internal sealed class RegisterAction : StoreAction {

    data class UpdateLoading(val isLoading: Boolean) : RegisterAction()

    data class UpdateValidErrors(
        val username: UsernameValidError?,
        val email: EmailValidError?,
        val password: PasswordValidError?,
    ) : RegisterAction()
}

internal sealed class RegisterOutput : BaseOutput {
    data object NavigateToLogin : RegisterOutput()
    data object NavigateToVerification : RegisterOutput()
}