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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.OutputResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ShareSchedulesInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.MediatedBaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ScheduleShareClaimUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi as mapOrganizationToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToDomain as mapSubjectToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain as mapEmployeeToDomain

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal interface ShareWorkProcessor :
    FlowWorkProcessor<ShareWorkCommand, ShareAction, ShareEffect, ShareOutput> {

    class Base(
        private val shareSchedulesInteractor: ShareSchedulesInteractor,
    ) : ShareWorkProcessor {

        override suspend fun work(command: ShareWorkCommand) = when (command) {
            is ShareWorkCommand.CreateShare -> createShareWork()
            is ShareWorkCommand.ClaimShare -> claimShareWork(command.code)
            is ShareWorkCommand.ReleaseShare -> releaseShareWork(command)
            is ShareWorkCommand.LinkOrganization -> linkOrganizationWork(command)
            is ShareWorkCommand.UpdateLinkedSubjects -> updateLinkedSubjectsWork(command)
            is ShareWorkCommand.UpdateLinkedEmployees -> updateLinkedEmployeesWork(command)
            is ShareWorkCommand.AcceptSharedSchedule -> acceptSharedScheduleWork(command)
            is ShareWorkCommand.PrepareImportReward -> prepareImportRewardWork(command.claim)
        }

        private fun createShareWork() = flow<ShareWorkResult> {
            shareSchedulesInteractor.createShare().handle(
                onLeftAction = { failure ->
                    when ((failure as? ScheduleFailures.OtherError)?.throwable) {
                        is ShareException.RateLimit,
                        is ShareException.ShareLimit,
                        is ShareException.ItemLimit,
                        is ShareException.PayloadTooLarge -> {
                            emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.INPUT)))
                            emit(EffectResult(ShareEffect.ShowError(failure)))
                        }
                        else -> when (failure) {
                            ScheduleFailures.InternetError -> emit(
                                ActionResult(ShareAction.UpdateStatus(ShareStatus.OFFLINE))
                            )
                            else -> emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.ERROR)))
                        }
                    }
                },
                onRightAction = { link -> emit(ActionResult(ShareAction.SetupLink(link.mapToUi()))) },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.LOADING)))
        }

        private fun claimShareWork(code: String) = flow<ShareWorkResult> {
            shareSchedulesInteractor.claimShare(code).handle(
                onLeftAction = { failure ->
                    when ((failure as? ScheduleFailures.OtherError)?.throwable) {
                        is ShareException.InvalidCode -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.INVALID))
                        )
                        is ShareException.Expired -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.EXPIRED))
                        )
                        is ShareException.Claimed -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.CLAIMED))
                        )
                        is ShareException.Consumed -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.CONSUMED))
                        )
                        is ShareException.RateLimit,
                        is ShareException.ShareLimit,
                        is ShareException.ItemLimit,
                        is ShareException.PayloadTooLarge -> {
                            emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.INPUT)))
                            emit(EffectResult(ShareEffect.ShowError(failure)))
                        }
                        else -> when (failure) {
                            ScheduleFailures.InternetError -> emit(
                                ActionResult(ShareAction.UpdateStatus(ShareStatus.OFFLINE))
                            )
                            else -> emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.ERROR)))
                        }
                    }
                },
                onRightAction = { preview ->
                    emit(ActionResult(ShareAction.SetupClaim(
                        claim = preview.claim.mapToUi(),
                        organizationsLinkData = preview.links.map { link -> link.mapToUi() },
                        linkedSchedules = preview.schedules.map { schedule -> schedule.mapToUi() },
                        maxNumberOfWeek = preview.maxNumberOfWeek,
                    )))
                    emit(ActionResult(ShareAction.UpdateOrganizations(
                        preview.organizations.map { organization -> organization.mapOrganizationToUi() },
                    )))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.LOADING)))
        }

        private fun releaseShareWork(command: ShareWorkCommand.ReleaseShare) = flow<ShareWorkResult> {
            shareSchedulesInteractor.releaseShare(command.claim.mapToDomain()).handle(
                onLeftAction = { failure ->
                    if (command.navigateBack) {
                        emit(OutputResult(ShareOutput.NavigateToBack))
                    } else {
                        emit(ActionResult(ShareAction.Reset))
                        emit(EffectResult(ShareEffect.ShowError(failure)))
                    }
                },
                onRightAction = {
                    emit(ActionResult(ShareAction.Reset))
                    if (command.navigateBack) {
                        emit(OutputResult(ShareOutput.NavigateToBack))
                    }
                },
            )
        }

        private fun linkOrganizationWork(
            command: ShareWorkCommand.LinkOrganization,
        ) = flow<ShareWorkResult> {
            shareSchedulesInteractor.linkOrganization(
                links = command.allLinkData.map { link -> link.mapToDomain() },
                schedules = command.sharedSchedules.map { schedule -> schedule.mapToDomain() },
                sharedOrganizationId = command.sharedOrganization,
                targetOrganizationId = command.targetOrganization,
            ).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(ShareEffect.ShowError(failure)))
                },
                onRightAction = { result ->
                    emit(ActionResult(ShareAction.UpdateLinkData(
                        linkData = result.links.map { link -> link.mapToUi() },
                        linkedSchedules = result.schedules.map { schedule -> schedule.mapToUi() },
                    )))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateLoadingLinkedOrganization(true)))
        }.onCompletion {
            emit(ActionResult(ShareAction.UpdateLoadingLinkedOrganization(false)))
        }

        private fun updateLinkedSubjectsWork(
            command: ShareWorkCommand.UpdateLinkedSubjects,
        ) = flow<ShareWorkResult> {
            shareSchedulesInteractor.updateLinkedSubjects(
                links = command.allLinkData.map { link -> link.mapToDomain() },
                schedules = command.sharedSchedules.map { schedule -> schedule.mapToDomain() },
                sharedOrganizationId = command.sharedOrganization,
                subjects = command.subjects.mapValues { (_, subject) -> subject.mapSubjectToDomain() },
            ).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(ShareEffect.ShowError(failure)))
                },
                onRightAction = { result ->
                    emit(ActionResult(ShareAction.UpdateLinkData(
                        linkData = result.links.map { link -> link.mapToUi() },
                        linkedSchedules = result.schedules.map { schedule -> schedule.mapToUi() },
                    )))
                },
            )
        }

        private fun updateLinkedEmployeesWork(
            command: ShareWorkCommand.UpdateLinkedEmployees,
        ) = flow<ShareWorkResult> {
            shareSchedulesInteractor.updateLinkedEmployees(
                links = command.allLinkData.map { link -> link.mapToDomain() },
                schedules = command.sharedSchedules.map { schedule -> schedule.mapToDomain() },
                sharedOrganizationId = command.sharedOrganization,
                employees = command.teachers.mapValues { (_, employee) -> employee.mapEmployeeToDomain() },
            ).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(ShareEffect.ShowError(failure)))
                },
                onRightAction = { result ->
                    emit(ActionResult(ShareAction.UpdateLinkData(
                        linkData = result.links.map { link -> link.mapToUi() },
                        linkedSchedules = result.schedules.map { schedule -> schedule.mapToUi() },
                    )))
                },
            )
        }

        private fun acceptSharedScheduleWork(
            command: ShareWorkCommand.AcceptSharedSchedule,
        ) = flow<ShareWorkResult> {
            shareSchedulesInteractor.importShare(
                rewardChallengeId = command.rewardChallengeId,
                claim = command.claim.mapToDomain(),
                links = command.organizationsLinkData.map { link -> link.mapToDomain() },
            ).handle(
                onLeftAction = { failure ->
                    when ((failure as? ScheduleFailures.OtherError)?.throwable) {
                        is ShareException.InvalidCode -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.INVALID))
                        )
                        is ShareException.Expired -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.EXPIRED))
                        )
                        is ShareException.Claimed -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.CLAIMED))
                        )
                        is ShareException.Consumed -> emit(
                            ActionResult(ShareAction.UpdateStatus(ShareStatus.CONSUMED))
                        )
                        else -> when (failure) {
                            ScheduleFailures.InternetError -> emit(
                                ActionResult(ShareAction.UpdateStatus(ShareStatus.OFFLINE))
                            )
                            else -> emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.ERROR)))
                        }
                    }
                },
                onRightAction = {
                    emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.SUCCESS)))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateStatus(ShareStatus.IMPORTING)))
        }

        private fun prepareImportRewardWork(
            claim: ScheduleShareClaimUi,
        ) = flow<ShareWorkResult> {
            shareSchedulesInteractor.createImportReward(claim.mapToDomain()).handle(
                onLeftAction = { failure ->
                    emit(ActionResult(ShareAction.UpdateRewardChallenge(null, false)))
                    emit(EffectResult(ShareEffect.ShowError(failure)))
                },
                onRightAction = { challenge ->
                    val action = ShareAction.UpdateRewardChallenge(
                        challengeId = challenge.id,
                        isInProgress = true,
                    )
                    emit(ActionResult(action))
                },
            )
        }.onStart {
            emit(ActionResult(ShareAction.UpdateRewardChallenge(null, true)))
        }

    }
}

internal sealed class ShareWorkCommand : WorkCommand {
    data object CreateShare : ShareWorkCommand()
    data class ClaimShare(val code: String) : ShareWorkCommand()
    data class ReleaseShare(
        val claim: ScheduleShareClaimUi,
        val navigateBack: Boolean,
    ) : ShareWorkCommand()
    data class LinkOrganization(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val targetOrganization: UID?,
    ) : ShareWorkCommand()
    data class UpdateLinkedSubjects(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val subjects: Map<UID, SubjectUi>,
    ) : ShareWorkCommand()
    data class UpdateLinkedEmployees(
        val allLinkData: List<OrganizationLinkData>,
        val sharedSchedules: List<MediatedBaseScheduleUi>,
        val sharedOrganization: UID,
        val teachers: Map<UID, EmployeeUi>,
    ) : ShareWorkCommand()
    data class AcceptSharedSchedule(
        val rewardChallengeId: String,
        val claim: ScheduleShareClaimUi,
        val organizationsLinkData: List<OrganizationLinkData>,
    ) : ShareWorkCommand()
    data class PrepareImportReward(val claim: ScheduleShareClaimUi) : ShareWorkCommand()
}

internal typealias ShareWorkResult = WorkResult<ShareAction, ShareEffect, ShareOutput>
