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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.models.InputFileUi
import ru.aleshin.studyassistant.editor.impl.domain.interactors.AppUserInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
internal interface ProfileWorkProcessor :
    FlowWorkProcessor<ProfileWorkCommand, ProfileAction, ProfileEffect> {

    class Base(
        private val appUserInteractor: AppUserInteractor,
    ) : ProfileWorkProcessor {

        override suspend fun work(command: ProfileWorkCommand) = when (command) {
            is ProfileWorkCommand.LoadAppUser -> loadAppUserWork()
            is ProfileWorkCommand.LoadPaidUserStatus -> loadPaidUserStatusWork()
            is ProfileWorkCommand.UpdateAppUser -> updateAppUserWork(command.user)
            is ProfileWorkCommand.UpdatePassword -> updatePasswordWork(command.oldPassword, command.newPassword)
            is ProfileWorkCommand.UpdateAvatar -> updateAvatarWork(command.user, command.file)
            is ProfileWorkCommand.DeleteAvatar -> deleteAvatarWork(command.user)
        }

        private fun loadAppUserWork() = flow {
            appUserInteractor.fetchAppUser().collectAndHandle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { user ->
                    emit(ActionResult(ProfileAction.SetupAppUser(user.mapToUi())))
                },
            )
        }

        private fun loadPaidUserStatusWork() = flow {
            appUserInteractor.fetchAppUserPaidStatus().collectAndHandle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { isPaidUser ->
                    emit(ActionResult(ProfileAction.UpdatePaidUserStatus(isPaidUser)))
                },
            )
        }

        private fun updateAppUserWork(user: AppUserUi) = flow {
            appUserInteractor.updateUser(user.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
            )
        }

        private fun updatePasswordWork(oldPassword: String?, newPassword: String) = flow {
            appUserInteractor.updatePassword(oldPassword, newPassword).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
            )
        }

        private fun updateAvatarWork(user: AppUserUi, file: InputFileUi) = flow {
            appUserInteractor.uploadAvatar(user.avatar, file.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { imageUrl ->
                    val updatedUser = user.copy(avatar = imageUrl)
                    appUserInteractor.updateUser(updatedUser.mapToDomain()).handle(
                        onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                    )
                }
            )
        }

        private fun deleteAvatarWork(user: AppUserUi) = flow {
            appUserInteractor.deleteAvatar(user.avatar ?: "").handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = {
                    val updatedUser = user.copy(avatar = null)
                    appUserInteractor.updateUser(updatedUser.mapToDomain()).handle(
                        onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                    )
                }
            )
        }
    }
}

internal sealed class ProfileWorkCommand : WorkCommand {
    data object LoadAppUser : ProfileWorkCommand()
    data object LoadPaidUserStatus : ProfileWorkCommand()
    data class UpdateAppUser(val user: AppUserUi) : ProfileWorkCommand()
    data class UpdateAvatar(val user: AppUserUi, val file: InputFileUi) : ProfileWorkCommand()
    data class DeleteAvatar(val user: AppUserUi) : ProfileWorkCommand()
    data class UpdatePassword(val oldPassword: String?, val newPassword: String) : ProfileWorkCommand()
}