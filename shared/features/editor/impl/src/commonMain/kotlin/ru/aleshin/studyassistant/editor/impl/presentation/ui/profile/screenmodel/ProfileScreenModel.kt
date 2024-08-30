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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileViewState

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
internal class ProfileScreenModel(
    private val workProcessor: ProfileWorkProcessor,
    stateCommunicator: ProfileStateCommunicator,
    effectCommunicator: ProfileEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ProfileViewState, ProfileEvent, ProfileAction, ProfileEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ProfileEvent.Init)
        }
    }

    override suspend fun WorkScope<ProfileViewState, ProfileAction, ProfileEffect>.handleEvent(
        event: ProfileEvent,
    ) {
        when (event) {
            is ProfileEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_APP_USER) {
                    val command = ProfileWorkCommand.LoadAppUser
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_PAID_STATUS) {
                    val command = ProfileWorkCommand.LoadPaidUserStatus
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateAvatar -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val user = checkNotNull(appUser)
                    val command = ProfileWorkCommand.UpdateAvatar(user, event.file)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.DeleteAvatar -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val user = checkNotNull(appUser)
                    val command = ProfileWorkCommand.DeleteAvatar(user)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateUsername -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(username = event.name)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateDescription -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(description = event.text)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateBirthday -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(birthday = event.text)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateGender -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(gender = event.gender)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateCity -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(city = event.city)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdateSocialNetworks -> with(state()) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val updatedUser = checkNotNull(appUser).copy(socialNetworks = event.socialNetworks)
                    val command = ProfileWorkCommand.UpdateAppUser(updatedUser)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.UpdatePassword -> with(event) {
                launchBackgroundWork(BackgroundKey.USER_ACTION) {
                    val command = ProfileWorkCommand.UpdatePassword(oldPassword, newPassword)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ProfileEvent.NavigateToBack -> {
                sendEffect(ProfileEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ProfileAction,
        currentState: ProfileViewState,
    ) = when (action) {
        is ProfileAction.SetupAppUser -> currentState.copy(
            appUser = action.user,
            isLoading = false,
        )
        is ProfileAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ProfileAction.UpdatePaidUserStatus -> currentState.copy(
            isPaidUser = action.isPaidUser,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_APP_USER, LOAD_PAID_STATUS, USER_ACTION
    }
}

@Composable
internal fun Screen.rememberProfileScreenModel(): ProfileScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ProfileScreenModel>() }
}