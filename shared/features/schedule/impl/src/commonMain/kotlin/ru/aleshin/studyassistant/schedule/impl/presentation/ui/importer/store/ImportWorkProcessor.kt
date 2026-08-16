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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleImportInteractor
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal interface ImportWorkProcessor :
    FlowWorkProcessor<ImportWorkCommand, ImportAction, ImportEffect, ImportOutput> {

    class Base(
        private val interactor: ScheduleImportInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
    ) : ImportWorkProcessor {

        private var preparedImage: CompressedScheduleImage? = null

        override suspend fun work(command: ImportWorkCommand) = when (command) {
            is ImportWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is ImportWorkCommand.LoadCatalog -> loadCatalogWork(command.organizationId)
            is ImportWorkCommand.PrepareImage -> prepareImageWork(command.imageBytes)
            is ImportWorkCommand.ExtractDraft -> extractDraftWork(command)
            is ImportWorkCommand.PrepareImportReward -> prepareImportRewardWork(command.requestId)
            is ImportWorkCommand.ApplyDraft -> applyDraftWork(command)
        }

        private fun loadOrganizationsWork() = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { failures ->
                    emit(EffectResult(ImportEffect.ShowError(failures)))
                },
                onRightAction = { organizations ->
                    val action = ImportAction.SetupOrganizations(
                        organizations = organizations.map { organization -> organization.mapToUi() },
                    )
                    emit(ActionResult(action))
                },
            )
        }

        private fun loadCatalogWork(organizationId: UID) = flow {
            organizationsInteractor.fetchOrganizationById(organizationId).collectAndHandle(
                onLeftAction = { failures ->
                    emit(EffectResult(ImportEffect.ShowError(failures)))
                },
                onRightAction = { organization ->
                    val action = ImportAction.SetupCatalog(
                        subjects = organization.subjects.map { subject -> subject.mapToUi() },
                        employees = organization.employee.map { employee -> employee.mapToUi() },
                    )
                    emit(ActionResult(action))
                },
            )
        }

        private fun prepareImageWork(imageBytes: ByteArray) = flow<ImportWorkResult> {
            interactor.prepareImage(imageBytes).handle(
                onLeftAction = { failure ->
                    preparedImage = null
                    emit(ActionResult(ImportAction.UpdateHasPhoto(false)))
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = { image ->
                    preparedImage = image
                    emit(ActionResult(ImportAction.UpdateHasPhoto(true)))
                },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }

        private fun extractDraftWork(command: ImportWorkCommand.ExtractDraft) = flow<ImportWorkResult> {
            val image = preparedImage
            if (image == null) {
                emit(EffectResult(ImportEffect.ShowError(ScheduleFailures.InvalidImage)))
                return@flow
            }
            interactor.extractDraft(
                requestId = command.requestId,
                image = image,
                note = command.note,
                organizationId = command.organizationId,
            ).handle(
                onLeftAction = { failure -> emit(EffectResult(ImportEffect.ShowError(failure))) },
                onRightAction = { draft ->
                    val action = ImportAction.SetupDraft(
                        draft = draft.mapToUi(),
                        requestId = command.requestId,
                    )
                    emit(ActionResult(action))
                },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }

        private fun prepareImportRewardWork(requestId: UID) = flow<ImportWorkResult> {
            interactor.createImportReward(requestId).handle(
                onLeftAction = { failure ->
                    emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = { challenge ->
                    val action = ImportAction.UpdateRewardChallenge(
                        challengeId = challenge.id,
                        isInProgress = true,
                    )
                    emit(ActionResult(action))
                },
            )
        }

        private fun applyDraftWork(command: ImportWorkCommand.ApplyDraft) = flow<ImportWorkResult> {
            interactor.applyDraft(
                draft = command.draft.mapToDomain(),
                organizationId = command.organizationId,
                rewardChallengeId = command.rewardChallengeId,
            ).handle(
                onLeftAction = { failure ->
                    emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = {
                    emit(ActionResult(ImportAction.UpdateApplied(true)))
                },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }
    }
}

internal sealed class ImportWorkCommand : WorkCommand {
    data object LoadOrganizations : ImportWorkCommand()
    data class LoadCatalog(val organizationId: UID) : ImportWorkCommand()
    data class PrepareImage(val imageBytes: ByteArray) : ImportWorkCommand()
    data class ExtractDraft(
        val requestId: UID,
        val note: String,
        val organizationId: UID,
    ) : ImportWorkCommand()
    data class PrepareImportReward(val requestId: UID) : ImportWorkCommand()
    data class ApplyDraft(
        val draft: ScheduleImportDraftUi,
        val organizationId: UID,
        val rewardChallengeId: String,
    ) : ImportWorkCommand()
}

internal typealias ImportWorkResult = WorkResult<ImportAction, ImportEffect, ImportOutput>
