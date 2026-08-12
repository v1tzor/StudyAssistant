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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportDraftUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Serializable
internal data class ImportState(
    val sourceText: String = "",
    val numberOfWeeks: Int = 1,
    val draft: ScheduleImportDraftUi? = null,
    val isLoading: Boolean = false,
    val isApplied: Boolean = false,
) : StoreState

internal sealed class ImportEvent : StoreEvent {
    data class Started(val input: ImportInput, val isRestore: Boolean) : ImportEvent()
    data class UpdateSourceText(val text: String) : ImportEvent()
    data class UpdateNumberOfWeeks(val value: Int) : ImportEvent()
    data class RecognizeImage(val imageBytes: ByteArray) : ImportEvent()
    data object ImageSelectionFailed : ImportEvent()
    data object ExtractDraft : ImportEvent()
    data class ToggleEntry(val id: Int) : ImportEvent()
    data class UpdateEntry(val entry: ScheduleImportEntryUi) : ImportEvent()
    data object ApplyDraft : ImportEvent()
    data object EditSource : ImportEvent()
    data object ClickBack : ImportEvent()
}

internal sealed class ImportEffect : StoreEffect {
    data class ShowError(val failure: ScheduleFailures) : ImportEffect()
}

internal sealed class ImportAction : StoreAction {
    data class UpdateSourceText(val text: String) : ImportAction()
    data class UpdateNumberOfWeeks(val value: Int) : ImportAction()
    data class SetupDraft(val draft: ScheduleImportDraftUi?) : ImportAction()
    data class UpdateLoading(val isLoading: Boolean) : ImportAction()
    data class UpdateApplied(val isApplied: Boolean) : ImportAction()
}

internal sealed class ImportOutput : BaseOutput {
    data object NavigateToBack : ImportOutput()
}

internal data class ImportInput(
    val rawText: String?,
) : BaseInput
