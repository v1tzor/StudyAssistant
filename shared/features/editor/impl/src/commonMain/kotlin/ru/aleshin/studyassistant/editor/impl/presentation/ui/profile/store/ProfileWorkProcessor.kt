/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.models.InputFileUi
import ru.aleshin.studyassistant.editor.impl.domain.interactors.ProfileInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileOutput

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
internal interface ProfileWorkProcessor :
    FlowWorkProcessor<ProfileWorkCommand, ProfileAction, ProfileEffect, ProfileOutput> {

    class Base(
        private val profileInteractor: ProfileInteractor,
    ) : ProfileWorkProcessor {

        override suspend fun work(command: ProfileWorkCommand) = when (command) {
            is ProfileWorkCommand.LoadProfile -> loadProfileWork()
            is ProfileWorkCommand.UpdateProfile -> updateProfileWork(command.profile)
            is ProfileWorkCommand.UpdateAvatar -> updateAvatarWork(command.user, command.file)
            is ProfileWorkCommand.DeleteAvatar -> deleteAvatarWork(command.user)
        }

        private fun loadProfileWork() = flow {
            profileInteractor.fetchProfile().collectAndHandle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { profile ->
                    emit(ActionResult(ProfileAction.SetupProfile(profile.mapToUi())))
                },
            )
        }

        private fun updateProfileWork(profile: ProfileUi) = flow {
            profileInteractor.updateProfile(profile.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
            )
        }

        private fun updateAvatarWork(user: ProfileUi, file: InputFileUi) = flow {
            profileInteractor.uploadAvatar(user.avatar, file.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { imageUrl ->
                    val updatedUser = user.copy(avatar = imageUrl)
                    profileInteractor.updateProfile(updatedUser.mapToDomain()).handle(
                        onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                    )
                }
            )
        }

        private fun deleteAvatarWork(user: ProfileUi) = flow {
            profileInteractor.deleteAvatar(user.avatar ?: "").handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = {
                    val updatedUser = user.copy(avatar = null)
                    profileInteractor.updateProfile(updatedUser.mapToDomain()).handle(
                        onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                    )
                }
            )
        }
    }
}

internal sealed class ProfileWorkCommand : WorkCommand {
    data object LoadProfile : ProfileWorkCommand()
    data class UpdateProfile(val profile: ProfileUi) : ProfileWorkCommand()
    data class UpdateAvatar(val user: ProfileUi, val file: InputFileUi) : ProfileWorkCommand()
    data class DeleteAvatar(val user: ProfileUi) : ProfileWorkCommand()
}
