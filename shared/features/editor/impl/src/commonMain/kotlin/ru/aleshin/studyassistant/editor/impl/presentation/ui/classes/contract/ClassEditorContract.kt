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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import entities.subject.EventType
import functional.TimeRange
import functional.UID
import kotlinx.datetime.Instant
import platform.InstantParceler
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 01.06.2024
 */
@Parcelize
internal data class ClassEditorViewState(
    val isLoading: Boolean = true,
    val editableClass: EditClassUi? = null,
    val schedule: ScheduleUi? = null,
    @TypeParceler<Instant, InstantParceler>
    val freeClassTimeRanges: Map<TimeRange, Boolean>? = null,
    val weekDay: DayOfNumberedWeekUi? = null,
    val organizations: List<OrganizationUi> = emptyList(),
) : BaseViewState

internal sealed class ClassEditorEvent : BaseEvent {
    data class Init(
        val classId: UID?,
        val scheduleId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorEvent()
    data class UpdateLocations(val locations: List<ContactInfoUi>) : ClassEditorEvent()
    data class UpdateOffices(val offices: List<String>) : ClassEditorEvent()
    data class SelectOrganization(val organization: OrganizationUi?) : ClassEditorEvent()
    data class SelectSubject(val type: EventType?, val subject: SubjectUi?) : ClassEditorEvent()
    data class SelectTeacher(val teacher: EmployeeUi?) : ClassEditorEvent()
    data class SelectLocation(val location: ContactInfoUi?, val office: String?) : ClassEditorEvent()
    data class SelectTime(val startTime: Instant?, val endTime: Instant?) : ClassEditorEvent()
    data class ChangeNotifyParams(val notification: Boolean) : ClassEditorEvent()
    data object NavigateToSubjectEditor : ClassEditorEvent()
    data object NavigateToEmployeeEditor : ClassEditorEvent()
    data object SaveClass : ClassEditorEvent()
    data object NavigateToBack : ClassEditorEvent()
}

internal sealed class ClassEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : ClassEditorEffect()
    data class NavigateToLocal(val pushScreen: Screen) : ClassEditorEffect()
    data object NavigateToBack : ClassEditorEffect()
}

internal sealed class ClassEditorAction : BaseAction {
    data class SetupEditModel(
        val editableClass: EditClassUi,
        val schedule: ScheduleUi,
        val freeClassTimeRanges: Map<TimeRange, Boolean>?,
        val weekDay: DayOfNumberedWeekUi,
        val organizations: List<OrganizationUi>,
    ) : ClassEditorAction()
    data class UpdateLoading(val isLoading: Boolean) : ClassEditorAction()
    data class UpdateEditModel(val model: EditClassUi?) : ClassEditorAction()
}
