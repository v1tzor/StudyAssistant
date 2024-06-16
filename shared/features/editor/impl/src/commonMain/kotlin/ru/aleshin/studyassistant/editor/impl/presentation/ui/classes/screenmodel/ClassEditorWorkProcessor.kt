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

import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import extensions.isCurrentDay
import extensions.setHoursAndMinutes
import extensions.shiftMillis
import functional.Constants.Class
import functional.Either
import functional.TimeRange
import functional.UID
import functional.handle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import managers.TimeOverlayManager
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.api.ui.mapToDomain
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.EditClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.convertToBase
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.convertToEditModel
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.convertToShort
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorEffect

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface ClassEditorWorkProcessor :
    FlowWorkProcessor<ClassEditorWorkCommand, ClassEditorAction, ClassEditorEffect> {

    class Base(
        private val baseScheduleInteractor: BaseScheduleInteractor,
        private val customScheduleInteractor: CustomScheduleInteractor,
        private val baseClassInteractor: BaseClassInteractor,
        private val customClassInteractor: CustomClassInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val overlayManager: TimeOverlayManager,
    ) : ClassEditorWorkProcessor {

        override suspend fun work(command: ClassEditorWorkCommand) = when (command) {
            is ClassEditorWorkCommand.LoadEditModel -> loadEditModelWork(
                classId = command.classId,
                scheduleId = command.scheduleId,
                isCustom = command.isCustomSchedule,
                weekDay = command.weekDay,
            )
            is ClassEditorWorkCommand.SaveEditModel -> saveEditModelWork(
                editModel = command.editModel,
                command.schedule,
                weekDay = command.weekDay,
            )
            is ClassEditorWorkCommand.UpdateOffices -> updateOfficesWork(
                organization = command.organization,
                offices = command.offices,
            )
            is ClassEditorWorkCommand.UpdateLocations -> updateLocationsWork(
                organization = command.organization,
                locations = command.locations,
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadEditModelWork(
            classId: UID?,
            scheduleId: UID?,
            isCustom: Boolean,
            weekDay: DayOfNumberedWeekUi,
        ) = flow {
            val organizationsFlow = organizationInteractor.fetchAllOrganizations()
            val scheduleFlow = if (scheduleId != null) {
                if (isCustom) {
                    customScheduleInteractor.fetchScheduleById(scheduleId).map { scheduleEither ->
                        scheduleEither.mapRight { ScheduleUi.Custom(it?.mapToUi()) }
                    }
                } else {
                    baseScheduleInteractor.fetchScheduleById(scheduleId).map { scheduleEither ->
                        scheduleEither.mapRight { ScheduleUi.Base(it?.mapToUi()) }
                    }
                }
            } else {
                val scheduleEither = if (isCustom) ScheduleUi.Custom(null) else ScheduleUi.Base(null)
                flowOf(Either.Right(scheduleEither))
            }
            scheduleFlow.flatMapLatestWithResult(
                secondFlow = organizationsFlow,
                onError = { ClassEditorEffect.ShowError(it) },
                onData = { schedule, organizationList ->
                    val organizations = organizationList.map { it.mapToUi() }
                    val scheduleClasses = schedule.blockMapToValue(
                        onBaseSchedule = { baseSchedule -> baseSchedule?.classes },
                        onCustomSchedule = { customSchedule -> customSchedule?.classes },
                    )
                    val classModel = scheduleClasses?.find { it.uid == classId }
                    val selectedOrganization = classModel?.organization ?: organizations.run {
                        find { it.isMain } ?: getOrNull(0)
                    }?.convertToShort()
                    val freeTimeRanges = calculateFreeClassTimeRanges(
                        timeIntervals = selectedOrganization?.scheduleTimeIntervals,
                        existClasses = scheduleClasses?.map { it.timeRange },
                    )
                    val editModel = classModel?.convertToEditModel() ?: EditClassUi.createEditModel(
                        uid = classId,
                        scheduleId = scheduleId,
                        organization = selectedOrganization,
                        timeRange = freeTimeRanges?.toList()?.find { it.second }?.first
                    )
                    ClassEditorAction.SetupEditModel(
                        editModel,
                        schedule,
                        freeTimeRanges,
                        weekDay,
                        organizations
                    )
                },
            ).collect { result ->
                emit(result)
            }
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
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                } else {
                    baseClassInteractor.updateClassBySchedule(
                        classModel = classModel,
                        schedule = checkNotNull(schedule.data).mapToDomain(),
                    ).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                }

                is ScheduleUi.Custom -> if (classModel.uid.isEmpty()) {
                    customClassInteractor.addClass(classModel).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                } else {
                    customClassInteractor.updateClass(classModel).handle(
                        onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
                        onRightAction = { emit(EffectResult(ClassEditorEffect.NavigateToBack)) },
                    )
                }
            }
        }

        private fun updateOfficesWork(organization: OrganizationShortUi, offices: List<String>) = flow {
            val updatedOrganization = organization.copy(offices = offices)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
            )
        }

        private fun updateLocationsWork(
            organization: OrganizationShortUi,
            locations: List<ContactInfoUi>
        ) = flow {
            val updatedOrganization = organization.copy(locations = locations)
            organizationInteractor.updateShortOrganization(updatedOrganization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ClassEditorEffect.ShowError(it))) },
            )
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
                    if (endClassTime.isCurrentDay(startRange)) {
                        val classTimeRange = TimeRange(startClassTime, endClassTime)
                        val isOverlay = overlayManager.isOverlay(classTimeRange, existClasses ?: emptyList()).isOverlay
                        put(classTimeRange, !isOverlay)
                    }
                }
            }
        }
    }
}

internal sealed class ClassEditorWorkCommand : WorkCommand {
    data class LoadEditModel(
        val classId: UID?,
        val scheduleId: UID?,
        val isCustomSchedule: Boolean,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorWorkCommand()

    data class SaveEditModel(
        val editModel: EditClassUi,
        val schedule: ScheduleUi,
        val weekDay: DayOfNumberedWeekUi
    ) : ClassEditorWorkCommand()

    data class UpdateOffices(
        val organization: OrganizationShortUi,
        val offices: List<String>
    ) : ClassEditorWorkCommand()

    data class UpdateLocations(
        val organization: OrganizationShortUi,
        val locations: List<ContactInfoUi>
    ) : ClassEditorWorkCommand()
}