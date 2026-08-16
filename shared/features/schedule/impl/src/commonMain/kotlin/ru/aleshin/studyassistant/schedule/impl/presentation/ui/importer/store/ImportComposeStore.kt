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

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportInput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal class ImportComposeStore(
    private val workProcessor: ImportWorkProcessor,
    stateCommunicator: StateCommunicator<ImportState>,
    effectCommunicator: EffectCommunicator<ImportEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<ImportState, ImportEvent, ImportAction, ImportEffect, ImportInput, ImportOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: ImportInput, isRestore: Boolean) {
        dispatchEvent(ImportEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<ImportState, ImportAction, ImportEffect, ImportOutput>.handleEvent(
        event: ImportEvent,
    ) {
        when (event) {
            is ImportEvent.Started -> {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    workProcessor.work(ImportWorkCommand.LoadData).collectAndHandleWork()
                }
                if (!event.isRestore && !event.input.rawText.isNullOrBlank()) {
                    sendAction(ImportAction.UpdateSourceText(event.input.rawText))
                }
            }
            is ImportEvent.UpdateSourceText -> {
                sendAction(ImportAction.UpdateSourceText(event.text))
            }
            is ImportEvent.UpdateNumberOfWeeks -> {
                sendAction(ImportAction.UpdateNumberOfWeeks(event.value.coerceIn(1, 3)))
            }
            is ImportEvent.RecognizeImage -> {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.RecognizeImage(event.imageBytes)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.ImageSelectionFailed -> {
                sendEffect(ImportEffect.ShowError(ScheduleFailures.InvalidImage))
            }
            is ImportEvent.ExtractDraft -> with(state) {
                launchBackgroundWork(BackgroundKey.PROCESS) {
                    val command = ImportWorkCommand.ExtractDraft(sourceText, ocrDocument, numberOfWeeks)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ImportEvent.ToggleEntry -> with(state) {
                val updatedDraft = draft?.copy(
                    entries = draft.entries.map { entry ->
                        if (entry.id == event.id) entry.copy(included = !entry.included) else entry
                    },
                )
                sendAction(ImportAction.SetupDraft(updatedDraft))
            }
            is ImportEvent.UpdateEntry -> with(state) {
                val updatedDraft = draft?.copy(
                    entries = draft.entries.map { entry ->
                        if (entry.id == event.entry.id) event.entry else entry
                    },
                )
                sendAction(ImportAction.SetupDraft(updatedDraft))
            }
            is ImportEvent.ApplyDraft -> with(state) {
                if (draft != null) {
                    launchBackgroundWork(BackgroundKey.PROCESS) {
                        val command = ImportWorkCommand.ApplyDraft(draft)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is ImportEvent.EditSource -> {
                sendAction(ImportAction.SetupDraft(null))
            }
            is ImportEvent.ClickBack -> with(state) {
                if (draft != null && !isApplied) {
                    sendAction(ImportAction.SetupDraft(null))
                } else {
                    consumeOutput(ImportOutput.NavigateToBack)
                }
            }
        }
    }

    override suspend fun reduce(action: ImportAction, currentState: ImportState) = when (action) {
        is ImportAction.UpdateSourceText -> currentState.copy(sourceText = action.text)
        is ImportAction.SetupOcrDocument -> currentState.copy(ocrDocument = action.document)
        is ImportAction.UpdateNumberOfWeeks -> currentState.copy(numberOfWeeks = action.value)
        is ImportAction.SetupDraft -> currentState.copy(draft = action.draft)
        is ImportAction.UpdateLoading -> currentState.copy(isLoading = action.isLoading)
        is ImportAction.UpdateApplied -> currentState.copy(isApplied = action.isApplied)
        is ImportAction.SetupData -> currentState.copy(
            organizations = action.organizations,
            subjects = action.subjects,
            employees = action.employees,
        )
    }

    private enum class BackgroundKey : BackgroundWorkKey {
        PROCESS,
    }

    class Factory(
        private val workProcessor: ImportWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<ImportComposeStore, ImportState> {
        override fun create(savedState: ImportState) = ImportComposeStore(
            workProcessor = workProcessor,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
