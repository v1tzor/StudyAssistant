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
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleImportInteractor
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportSessionUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput
import kotlin.time.Clock

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal interface ImportWorkProcessor :
    FlowWorkProcessor<ImportWorkCommand, ImportAction, ImportEffect, ImportOutput> {

    class Base(
        private val importInteractor: ScheduleImportInteractor,
        private val organizationsInteractor: OrganizationsInteractor,
    ) : ImportWorkProcessor {

        override suspend fun work(command: ImportWorkCommand) = when (command) {
            is ImportWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is ImportWorkCommand.PrepareImage -> prepareImageWork(command.imageBytes)
            is ImportWorkCommand.ExtractDraft -> extractDraftWork(command.image, command.note, command.organizationId)
            is ImportWorkCommand.PrepareImportReward -> prepareImportRewardWork(command.requestId, command.session)
            is ImportWorkCommand.ApplySession -> applySessionWork(command.session, command.rewardChallengeId)
        }

        private fun loadOrganizationsWork() = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { failures ->
                    emit(EffectResult(ImportEffect.ShowError(failures)))
                },
                onRightAction = { organizations ->
                    val action = ImportAction.SetupOrganizations(
                        organizations = organizations.map { organization -> organization.mapToUi() }
                    )
                    emit(ActionResult(action))
                }
            )
        }

        private fun prepareImageWork(imageBytes: ByteArray) = flow<ImportWorkResult> {
            importInteractor.prepareImage(imageBytes).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = { image ->
                    emit(ActionResult(ImportAction.UpdatePhoto(image)))
                }
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoadingPhoto(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoadingPhoto(false)))
        }

        private fun extractDraftWork(
            image: CompressedScheduleImage,
            note: String,
            organizationId: UID
        ) = flow<ImportWorkResult> {
            val requestId = randomUUID()
            importInteractor.extractDraft(
                requestId = requestId,
                image = image,
                note = note,
                organizationId = organizationId,
            ).handle(
                onLeftAction = { failure -> emit(EffectResult(ImportEffect.ShowError(failure))) },
                onRightAction = { session ->
                    val action = ImportAction.SetupSession(session = session.mapToUi(), requestId = requestId)
                    emit(ActionResult(action))
                },
            )
        }.onStart {
            emit(
                ActionResult(
                    ImportAction.UpdateAnalysisProgress(
                        isLoading = true,
                        startedAt = Clock.System.now().toEpochMilliseconds(),
                    )
                )
            )
        }.onCompletion {
            emit(
                ActionResult(
                    ImportAction.UpdateAnalysisProgress(
                        isLoading = false,
                        startedAt = null,
                    )
                )
            )
        }

        private fun prepareImportRewardWork(
            requestId: UID,
            session: ScheduleImportSessionUi
        ) = flow<ImportWorkResult> {
            importInteractor.createImportReward(
                requestId = requestId,
                session = session.mapToDomain(),
            ).handle(
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

        private fun applySessionWork(
            session: ScheduleImportSessionUi,
            rewardChallengeId: String,
        ) = flow<ImportWorkResult> {
            importInteractor.applySession(
                session = session.mapToDomain(),
                rewardChallengeId = rewardChallengeId,
            ).handle(
                onLeftAction = { failure ->
                    emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = {
                    emit(ActionResult(ImportAction.UpdateApplied(true)))
                    emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
                },
            )
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateRewardChallenge(null, false)))
        }
    }
}

internal sealed class ImportWorkCommand : WorkCommand {
    data object LoadOrganizations : ImportWorkCommand()
    data class PrepareImage(val imageBytes: ByteArray) : ImportWorkCommand()
    data class ExtractDraft(
        var image: CompressedScheduleImage,
        val note: String,
        val organizationId: UID
    ) : ImportWorkCommand()
    data class PrepareImportReward(
        val requestId: UID,
        val session: ScheduleImportSessionUi,
    ) : ImportWorkCommand()
    data class ApplySession(
        val session: ScheduleImportSessionUi,
        val rewardChallengeId: String,
    ) : ImportWorkCommand()
}

internal typealias ImportWorkResult = WorkResult<ImportAction, ImportEffect, ImportOutput>
