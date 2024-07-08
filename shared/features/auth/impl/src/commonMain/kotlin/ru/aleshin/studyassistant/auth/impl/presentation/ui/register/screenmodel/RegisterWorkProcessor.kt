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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.register.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.auth.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.auth.impl.navigation.AuthScreenProvider
import ru.aleshin.studyassistant.auth.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.auth.impl.presentation.models.credentials.RegisterCredentialsUi
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterAction
import ru.aleshin.studyassistant.auth.impl.presentation.ui.register.contract.RegisterEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.preview.api.navigation.PreviewScreen

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface RegisterWorkProcessor :
    FlowWorkProcessor<RegisterWorkCommand, RegisterAction, RegisterEffect> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val screenProvider: AuthScreenProvider,
    ) : RegisterWorkProcessor {

        override suspend fun work(command: RegisterWorkCommand) = when (command) {
            is RegisterWorkCommand.RegisterNewAccount -> registerNewAccountWork(command.credentials)
        }

        private fun registerNewAccountWork(credentials: RegisterCredentialsUi) = flow {
            emit(ActionResult(RegisterAction.UpdateLoading(true)))
            authInteractor.registerNewAccount(credentials.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(RegisterEffect.ShowError(it))) },
                onRightAction = { user ->
                    val targetScreen = screenProvider.providePreviewScreen(PreviewScreen.Setup(user.uid))
                    emit(EffectResult(RegisterEffect.ReplaceGlobalScreen(targetScreen)))
                },
            )
            emit(ActionResult(RegisterAction.UpdateLoading(false)))
        }
    }
}

internal sealed class RegisterWorkCommand : WorkCommand {
    data class RegisterNewAccount(val credentials: RegisterCredentialsUi) : RegisterWorkCommand()
}