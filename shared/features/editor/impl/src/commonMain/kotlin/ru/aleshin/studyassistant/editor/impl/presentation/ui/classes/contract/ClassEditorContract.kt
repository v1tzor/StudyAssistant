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
import entities.subject.EventType
import functional.TimeRange
import functional.UID
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.SubjectUi

/**
 * @author Stanislav Aleshin on 01.06.2024
 */
internal data class ClassEditorViewState(
    val editModel: EditClassUi? = null,
    val isCustomSchedule: Boolean = false,
    val classesTimeRanges: List<TimeRange> = emptyList(),
    val weekDay: DayOfNumberedWeekUi? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    val teachers: List<EmployeeUi> = emptyList(),
    val locations: List<ContactInfoUi> = emptyList(),
    val offices: List<Int> = emptyList(),
) : BaseViewState

internal sealed class ClassEditorEvent : BaseEvent {
    data class Init(
        val classId: UID?,
        val scheduleId: UID?,
        val customSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorEvent()
    data class SelectOrganization(val organization: OrganizationShortUi?) : ClassEditorEvent()
    data class SelectSubject(val type: EventType?, val subject: SubjectUi?) : ClassEditorEvent()
    data class SelectTeacher(val teacher: EmployeeUi?) : ClassEditorEvent()
    data class SelectLocation(val location: ContactInfoUi?, val office: Int?) : ClassEditorEvent()
    data class SelectTime(val startTime: Instant?, val endTime: Instant?) : ClassEditorEvent()
    data class ChangeNotifyParams(val notification: Boolean) : ClassEditorEvent()
    data object SaveClass : ClassEditorEvent()
    data object NavigateToBack : ClassEditorEvent()
}

internal sealed class ClassEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : ClassEditorEffect()
    data class LoadOrganizationData(val organization: OrganizationShortUi) : ClassEditorEffect()
    data class NavigateToLocal(val pushScreen: Screen) : ClassEditorEffect()
    data object NavigateToBack : ClassEditorEffect()
}

internal sealed class ClassEditorAction : BaseAction {
    data class SetupEditModel(
        val model: EditClassUi,
        val weekDay: DayOfNumberedWeekUi,
        val customSchedule: Boolean,
        val times: List<TimeRange>,
    ) : ClassEditorAction()

    data class UpdateOrganizationData(
        val subjects: List<SubjectUi> = emptyList(),
        val employees: List<EmployeeUi> = emptyList(),
        val locations: List<ContactInfoUi> = emptyList(),
        val offices: List<Int> = emptyList(),
    ) : ClassEditorAction()

    data class UpdateEditModel(val model: EditClassUi?) : ClassEditorAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ClassEditorAction()
}
