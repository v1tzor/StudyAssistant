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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.ForgotCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.models.validation.EmailValidError
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 17.04.2024.
 */
@Immutable
@Parcelize
internal data class ForgotViewState(
    val isLoading: Boolean = false,
    val emailValidError: EmailValidError? = null,
) : BaseViewState

internal sealed class ForgotEvent : BaseEvent {
    data class SendResetPasswordEmail(val credentials: ForgotCredentialsUi) : ForgotEvent()
    data object NavigateToLogin : ForgotEvent()
}

internal sealed class ForgotEffect : BaseUiEffect {
    data class ShowError(val failures: AuthFailures) : ForgotEffect()
    data class NavigateToLocal(val pushScreen: Screen) : ForgotEffect()
}

internal sealed class ForgotAction : BaseAction {
    data class UpdateValidError(val email: EmailValidError?) : ForgotAction()
    data class UpdateLoading(val isLoading: Boolean) : ForgotAction()
}