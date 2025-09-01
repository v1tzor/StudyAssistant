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

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.editor.api.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi

/**
 * @author Stanislav Aleshin on 01.06.2024
 */
@Serializable
internal data class ClassState(
    val isLoading: Boolean = true,
    val editableClass: EditClassUi? = null,
    val weekDay: DayOfNumberedWeekUi? = null,
    val schedule: ScheduleUi? = null,
    val freeClassTimeRanges: Map<TimeRange, Boolean>? = null,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val subjects: List<SubjectUi> = emptyList(),
    val employees: List<EmployeeDetailsUi> = emptyList(),
) : StoreState

internal sealed class ClassEvent : StoreEvent {
    data class Started(val inputData: ClassInput, val isRestore: Boolean) : ClassEvent()
    data class UpdateOrganization(val organization: OrganizationShortUi?) : ClassEvent()
    data class UpdateSubject(val type: EventType?, val subject: SubjectUi?) : ClassEvent()
    data class UpdateTeacher(val teacher: EmployeeDetailsUi?) : ClassEvent()
    data class UpdateLocation(val location: ContactInfoUi?, val office: String?) : ClassEvent()
    data class UpdateTime(val startTime: Instant?, val endTime: Instant?) : ClassEvent()
    data class UpdateOrganizationOffices(val offices: List<String>) : ClassEvent()
    data class UpdateOrganizationLocations(val locations: List<ContactInfoUi>) : ClassEvent()
    data object SaveClass : ClassEvent()
    data class NavigateToOrganizationEditor(val organizationId: UID?) : ClassEvent()
    data class NavigateToSubjectEditor(val subjectId: UID?) : ClassEvent()
    data class NavigateToEmployeeEditor(val employeeId: UID?) : ClassEvent()
    data object NavigateToBack : ClassEvent()
}

internal sealed class ClassEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : ClassEffect()
}

internal sealed class ClassAction : StoreAction {
    data class SetupEditModel(
        val editModel: EditClassUi,
        val schedule: ScheduleUi,
        val freeClassTimeRanges: Map<TimeRange, Boolean>?,
        val weekDay: DayOfNumberedWeekUi,
    ) : ClassAction()
    data class UpdateEditModel(val editModel: EditClassUi?) : ClassAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : ClassAction()
    data class UpdateFreeClasses(val freeClassTimeRanges: Map<TimeRange, Boolean>?) : ClassAction()
    data class UpdateSubjects(val subjects: List<SubjectUi>) : ClassAction()
    data class UpdateEmployees(val employees: List<EmployeeDetailsUi>) : ClassAction()
    data class UpdateLoading(val isLoading: Boolean) : ClassAction()
}

internal data class ClassInput(
    val classId: UID?,
    val scheduleId: UID?,
    val organizationId: UID?,
    val customSchedule: Boolean,
    val weekDay: DayOfNumberedWeekUi,
) : BaseInput

internal sealed class ClassOutput : BaseOutput {
    data object NavigateToBack : ClassOutput()
    data class NavigateToEmployeeEditor(val config: EditorConfig.Employee) : ClassOutput()
    data class NavigateToSubjectEditor(val config: EditorConfig.Subject) : ClassOutput()
    data class NavigateToOrganizationEditor(val config: EditorConfig.Organization) : ClassOutput()
}