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
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleImportInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface ImportWorkProcessor :
    FlowWorkProcessor<ImportWorkCommand, ImportAction, ImportEffect, ImportOutput> {

    class Base(
        private val interactor: ScheduleImportInteractor,
    ) : ImportWorkProcessor {

        override suspend fun work(command: ImportWorkCommand) = when (command) {
            is ImportWorkCommand.RecognizeImage -> recognizeImageWork(command)
            is ImportWorkCommand.ExtractDraft -> extractDraftWork(command)
            is ImportWorkCommand.ApplyDraft -> applyDraftWork(command)
        }

        private fun recognizeImageWork(command: ImportWorkCommand.RecognizeImage) = flow<ImportWorkResult> {
            interactor.recognizeText(command.imageBytes).handle(
                onLeftAction = { failure ->
                    emit(EffectResult(ImportEffect.ShowError(failure)))
                },
                onRightAction = { text ->
                    emit(ActionResult(ImportAction.UpdateSourceText(text)))
                },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }

        private fun extractDraftWork(command: ImportWorkCommand.ExtractDraft) = flow<ImportWorkResult> {
            interactor.extractDraft(command.rawText, command.numberOfWeeks).handle(
                onLeftAction = { failure -> emit(EffectResult(ImportEffect.ShowError(failure))) },
                onRightAction = { draft ->
                    emit(ActionResult(ImportAction.SetupDraft(draft.mapToUi())))
                },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }

        private fun applyDraftWork(command: ImportWorkCommand.ApplyDraft) = flow<ImportWorkResult> {
            interactor.applyDraft(command.draft.mapToDomain()).handle(
                onLeftAction = { failure -> emit(EffectResult(ImportEffect.ShowError(failure))) },
                onRightAction = { emit(ActionResult(ImportAction.UpdateApplied(true))) },
            )
        }.onStart {
            emit(ActionResult(ImportAction.UpdateLoading(true)))
        }.onCompletion {
            emit(ActionResult(ImportAction.UpdateLoading(false)))
        }
    }
}

internal sealed class ImportWorkCommand : WorkCommand {
    data class RecognizeImage(val imageBytes: ByteArray) : ImportWorkCommand()
    data class ExtractDraft(val rawText: String, val numberOfWeeks: Int) : ImportWorkCommand()
    data class ApplyDraft(val draft: ScheduleImportDraftUi) : ImportWorkCommand()
}

internal typealias ImportWorkResult = WorkResult<ImportAction, ImportEffect, ImportOutput>
