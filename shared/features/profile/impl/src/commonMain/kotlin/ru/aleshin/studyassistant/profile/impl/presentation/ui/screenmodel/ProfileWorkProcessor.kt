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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.auth.api.navigation.AuthScreen
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.profile.impl.domain.interactors.AuthInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.FriendRequestsInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.ReminderInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.ShareSchedulesInteractor
import ru.aleshin.studyassistant.profile.impl.domain.interactors.UserInteractor
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileScreenProvider
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileAction
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal interface ProfileWorkProcessor :
    FlowWorkProcessor<ProfileWorkCommand, ProfileAction, ProfileEffect> {

    class Base(
        private val authInteractor: AuthInteractor,
        private val userInteractor: UserInteractor,
        private val friendRequestsInteractor: FriendRequestsInteractor,
        private val shareSchedulesInteractor: ShareSchedulesInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
        private val reminderInteractor: ReminderInteractor,
        private val screenProvider: ProfileScreenProvider,
        private val deviceInfoProvider: DeviceInfoProvider,
    ) : ProfileWorkProcessor {

        override suspend fun work(command: ProfileWorkCommand) = when (command) {
            is ProfileWorkCommand.LoadProfileInfo -> loadProfileInfoWork()
            is ProfileWorkCommand.LoadSharedSchedules -> loadSharedSchedulesWork()
            is ProfileWorkCommand.LoadFriends -> loadFriendsWork()
            is ProfileWorkCommand.SendSharedSchedule -> sendSharedScheduleWork(command.sendData)
            is ProfileWorkCommand.CancelSentSharedSchedule -> cancelSentSharedScheduleWork(command.schedule)
            is ProfileWorkCommand.SignOut -> signOutWork()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadProfileInfoWork() = flow {
            val userInfoFlow = userInteractor.fetchAppUser()
            val friendsRequestsFlow = friendRequestsInteractor.fetchAllFriendRequests()

            userInfoFlow.flatMapLatestWithResult(
                secondFlow = friendsRequestsFlow,
                onError = { ProfileEffect.ShowError(it) },
                onData = { userInfo, friendRequest ->
                    val profile = userInfo.mapToUi()
                    val requests = friendRequest.mapToUi()
                    ProfileAction.UpdateProfileInfo(profile, requests)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(ProfileAction.UpdateLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadSharedSchedulesWork() = flow {
            val sharedSchedulesFlow = shareSchedulesInteractor.fetchShortSharedSchedules()
            val organizationsFlow = organizationsInteractor.fetchAllShortOrganizations()

            sharedSchedulesFlow.flatMapLatestWithResult(
                secondFlow = organizationsFlow,
                onError = { ProfileEffect.ShowError(it) },
                onData = { schedules, shortOrganizations ->
                    val sharedSchedules = schedules.mapToUi()
                    val organizations = shortOrganizations.map { it.mapToUi() }
                    ProfileAction.UpdateSharedSchedules(sharedSchedules, organizations)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(ProfileAction.UpdateLoadingShared(true)))
        }

        private fun loadFriendsWork() = flow {
            userInteractor.fetchAllFriends().collectAndHandle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = { friends ->
                    emit(ActionResult(ProfileAction.UpdateFriends(friends.map { it.mapToUi() })))
                },
            )
        }

        private fun sendSharedScheduleWork(sendData: ShareSchedulesSendDataUi) = flow<ProfileWorkResult> {
            shareSchedulesInteractor.shareSchedules(sendData.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
            )
        }.onStart {
            emit(ActionResult(ProfileAction.UpdateLoadingSend(true)))
        }.onCompletion {
            emit(ActionResult(ProfileAction.UpdateLoadingSend(false)))
        }

        private fun cancelSentSharedScheduleWork(schedule: SentMediatedSchedulesUi) = flow {
            shareSchedulesInteractor.cancelSendSchedules(schedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
            )
        }

        private fun signOutWork() = flow {
            val deviceId = deviceInfoProvider.fetchDeviceId()
            reminderInteractor.stopReminders().handle(
                onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                onRightAction = {
                    authInteractor.signOut(deviceId).handle(
                        onLeftAction = { emit(EffectResult(ProfileEffect.ShowError(it))) },
                        onRightAction = {
                            val authScreen = screenProvider.provideAuthScreen(AuthScreen.Login)
                            emit(EffectResult(ProfileEffect.ReplaceGlobalScreen(authScreen)))
                        },
                    )
                }
            )
        }
    }
}

internal sealed class ProfileWorkCommand : WorkCommand {
    data object LoadProfileInfo : ProfileWorkCommand()
    data object LoadSharedSchedules : ProfileWorkCommand()
    data object LoadFriends : ProfileWorkCommand()
    data class SendSharedSchedule(val sendData: ShareSchedulesSendDataUi) : ProfileWorkCommand()
    data class CancelSentSharedSchedule(val schedule: SentMediatedSchedulesUi) : ProfileWorkCommand()
    data object SignOut : ProfileWorkCommand()
}

internal typealias ProfileWorkResult = WorkResult<ProfileAction, ProfileEffect>