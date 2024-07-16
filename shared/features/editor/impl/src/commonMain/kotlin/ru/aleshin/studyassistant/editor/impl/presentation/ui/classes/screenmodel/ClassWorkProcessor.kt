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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftMillis
import ru.aleshin.studyassistant.core.common.functional.Constants.Class
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.firstHandleAndGet
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.TimeOverlayManager
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.api.ui.mapToDomain
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.EmployeeInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.SubjectInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.convertToEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEffect

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface ClassWorkProcessor :
    FlowWorkProcessor<ClassWorkCommand, ClassAction, ClassEffect> {

    class Base(
        private val baseScheduleInteractor: BaseScheduleInteractor,
        private val customScheduleInteractor: CustomScheduleInteractor,
        private val baseClassInteractor: BaseClassInteractor,
        private val customClassInteractor: CustomClassInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val subjectsInteractor: SubjectInteractor,
        private val employeeInteractor: EmployeeInteractor,
        private val overlayManager: TimeOverlayManager,
    ) : ClassWorkProcessor {

        override suspend fun work(command: ClassWorkCommand) = when (command) {
            is ClassWorkCommand.LoadEditModel -> loadEditModelWork(
                classId = command.classId,
                scheduleId = command.scheduleId,
                organizationId = command.organizationId,
                isCustom = command.isCustomSchedule,
                weekDay = command.weekDay,
            )
            is ClassWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is ClassWorkCommand.LoadFreeClasses -> loadFreeClassesWork(
                organization = command.organization,
                schedule = command.schedule,
            )
            is ClassWorkCommand.LoadEmployees -> loadEmployeesWork(
                organizationId = command.organizationId,
            )
            is ClassWorkCommand.LoadSubjects -> loadSubjectsWork(
                organizationId = command.organizationId,
            )
            is ClassWorkCommand.UpdateOffices -> updateOfficesWork(
                organization = command.organization,
                offices = command.offices,
            )
            is ClassWorkCommand.UpdateLocations -> updateLocationsWork(
                organization = command.organization,
                locations = command.locations,
            )
            is ClassWorkCommand.SaveEditModel -> saveEditModelWork(
                editModel = command.editModel,
                schedule = command.schedule,
                weekDay = command.weekDay,
            )
        }

        private fun loadEditModelWork(
            classId: UID?,
            scheduleId: UID?,
            organizationId: UID?,
            isCustom: Boolean,
            weekDay: DayOfNumberedWeekUi,
        ) = flow {
            val schedule = if (scheduleId != null) {
                if (isCustom) {
                    customScheduleInteractor.fetchScheduleById(scheduleId).firstHandleAndGet(
                        onLeftAction = {
                            emit(EffectResult(ClassEffect.ShowError(it)))
                            ScheduleUi.Custom(null)
                        },
                        onRightAction = { schedule -> ScheduleUi.Custom(schedule?.mapToUi()) },
                    )
                } else {
                    baseScheduleInteractor.fetchScheduleById(scheduleId).firstHandleAndGet(
                        onLeftAction = {
                            emit(EffectResult(ClassEffect.ShowError(it)))
                            ScheduleUi.Base(null)
                        },
                        onRightAction = { schedule -> ScheduleUi.Base(schedule?.mapToUi()) },
                    )
                }
            } else {
                if (isCustom) ScheduleUi.Custom(null) else ScheduleUi.Base(null)
            }
            val classOrganization = organizationId?.let { organizationId ->
                organizationInteractor.fetchShortOrganizationById(organizationId).firstHandleAndGet(
                    onLeftAction = { error(it) },
                    onRightAction = { organization -> organization.mapToUi() }
                )
            }

            val scheduleClasses = schedule.blockMapToValue(
                onBaseSchedule = { baseSchedule -> baseSchedule?.classes },
                onCustomSchedule = { customSchedule -> customSchedule?.classes },
            )
            val classModel = scheduleClasses?.find { it.uid == classId }

            val freeTimeRanges = calculateFreeClassTimeRanges(
                timeIntervals = classOrganization?.scheduleTimeIntervals,
                existClasses = scheduleClasses?.map { it.timeRange },
            )

            val editModel = classModel?.convertToEditModel() ?: EditClassUi.createEditModel(
                uid = classId,
                scheduleId = scheduleId,
                organization = classOrganization,
                timeRange = freeTimeRanges?.toList()?.find { it.second }?.first
            )
            val action = ClassAction.SetupEditModel(
                editModel = editModel,
                schedule = schedule,
                freeClassTimeRanges = freeTimeRanges,
                weekDay = weekDay,
            )
            emit(ActionResult(action))
        }

        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                onRightAction = { organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    emit(ActionResult(ClassAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun loadFreeClassesWork(
            organization: OrganizationShortUi?,
            schedule: ScheduleUi?
        ) = flow {
            if (organization == null) {
                return@flow emit(ActionResult(ClassAction.UpdateFreeClasses(emptyMap())))
            }

            val scheduleClasses = schedule?.blockMapToValue(
                onBaseSchedule = { baseSchedule -> baseSchedule?.classes },
                onCustomSchedule = { customSchedule -> customSchedule?.classes },
            )

            val freeTimeRanges = calculateFreeClassTimeRanges(
                timeIntervals = organization.scheduleTimeIntervals,
                existClasses = scheduleClasses?.map { it.timeRange },
            )

            emit(ActionResult(ClassAction.UpdateFreeClasses(freeTimeRanges)))
        }

        private fun loadSubjectsWork(organizationId: UID?) = flow {
            if (organizationId == null) {
                return@flow emit(ActionResult(ClassAction.UpdateSubjects(emptyList())))
            }
            subjectsInteractor.fetchAllSubjectsByOrganization(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                onRightAction = { subjectsList ->
                    val subjects = subjectsList.map { it.mapToUi() }
                    emit(ActionResult(ClassAction.UpdateSubjects(subjects)))
                },
            )
        }

        private fun loadEmployeesWork(organizationId: UID?) = flow {
            if (organizationId == null) {
                return@flow emit(ActionResult(ClassAction.UpdateEmployees(emptyList())))
            }
            employeeInteractor.fetchAllDetailsEmployee(organizationId).collectAndHandle(
                onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                onRightAction = { employeeList ->
                    val employees = employeeList.map { it.mapToUi() }
                    emit(ActionResult(ClassAction.UpdateEmployees(employees)))
                },
            )
        }

        private fun updateOfficesWork(
            organization: OrganizationShortUi,
            offices: List<String>
        ) = flow {
            val updatedOrganization = organization.copy(offices = offices)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
            )
        }

        private fun updateLocationsWork(
            organization: OrganizationShortUi,
            locations: List<ContactInfoUi>
        ) = flow {
            val updatedOrganization = organization.copy(locations = locations)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
            )
        }

        private fun saveEditModelWork(
            editModel: EditClassUi,
            schedule: ScheduleUi,
            weekDay: DayOfNumberedWeekUi
        ) = flow {
            val classModel = editModel.convertToBase().mapToDomain()
            when (schedule) {
                is ScheduleUi.Base -> if (classModel.uid.isEmpty()) {
                    baseClassInteractor.addClassBySchedule(
                        classModel = classModel,
                        schedule = schedule.data?.mapToDomain(),
                        weekDay = weekDay.mapToDomain(),
                    ).handle(
                        onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEffect.NavigateToBack)) },
                    )
                } else {
                    baseClassInteractor.updateClassBySchedule(
                        classModel = classModel,
                        schedule = checkNotNull(schedule.data).mapToDomain(),
                    ).handle(
                        onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEffect.NavigateToBack)) },
                    )
                }

                is ScheduleUi.Custom -> if (classModel.uid.isEmpty()) {
                    customClassInteractor.addClassBySchedule(
                        classModel = classModel,
                        schedule = checkNotNull(schedule.data).mapToDomain(),
                    ).handle(
                        onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEffect.NavigateToBack)) },
                    )
                } else {
                    customClassInteractor.updateClassBySchedule(
                        classModel = classModel,
                        schedule = checkNotNull(schedule.data).mapToDomain(),
                    ).handle(
                        onLeftAction = { emit(EffectResult(ClassEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEffect.NavigateToBack)) },
                    )
                }
            }
        }

        private fun calculateFreeClassTimeRanges(
            timeIntervals: ScheduleTimeIntervalsUi?,
            existClasses: List<TimeRange>?,
        ): Map<TimeRange, Boolean>? {
            val firstClassTime = timeIntervals?.firstClassTime
            val classDuration = timeIntervals?.baseClassDuration
            val breakDuration = timeIntervals?.baseBreakDuration

            if (firstClassTime == null || classDuration == null || breakDuration == null) return null

            val date = existClasses?.getOrNull(0)?.from
            val startRange = date?.setHoursAndMinutes(firstClassTime) ?: firstClassTime

            return mutableMapOf<TimeRange, Boolean>().apply {
                repeat(Class.MAX_NUMBER) { number ->
                    val lastEnd = maxOfOrNull { it.key.to }
                    val startClassTime = lastEnd?.shiftMillis(
                        amount = timeIntervals.specificBreakDuration.find { numberedDuration ->
                            numberedDuration.number == number
                        }?.duration ?: breakDuration
                    ) ?: startRange
                    val endClassTime = startClassTime.shiftMillis(
                        amount = timeIntervals.specificClassDuration.find { numberedDuration ->
                            numberedDuration.number == number + 1
                        }?.duration ?: classDuration
                    )
                    if (endClassTime.equalsDay(startRange)) {
                        val classTimeRange = TimeRange(startClassTime, endClassTime)
                        val isOverlay = overlayManager.isOverlay(
                            classTimeRange,
                            existClasses ?: emptyList()
                        ).isOverlay
                        put(classTimeRange, !isOverlay)
                    }
                }
            }
        }
    }
}

internal sealed class ClassWorkCommand : WorkCommand {

    data class LoadEditModel(
        val classId: UID?,
        val scheduleId: UID?,
        val organizationId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassWorkCommand()

    data object LoadOrganizations : ClassWorkCommand()

    data class LoadFreeClasses(
        val organization: OrganizationShortUi?,
        val schedule: ScheduleUi?,
    ) : ClassWorkCommand()

    data class LoadEmployees(
        val organizationId: UID?,
    ) : ClassWorkCommand()

    data class LoadSubjects(
        val organizationId: UID?,
    ) : ClassWorkCommand()

    data class UpdateOffices(
        val organization: OrganizationShortUi,
        val offices: List<String>
    ) : ClassWorkCommand()

    data class UpdateLocations(
        val organization: OrganizationShortUi,
        val locations: List<ContactInfoUi>
    ) : ClassWorkCommand()

    data class SaveEditModel(
        val editModel: EditClassUi,
        val schedule: ScheduleUi,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassWorkCommand()
}