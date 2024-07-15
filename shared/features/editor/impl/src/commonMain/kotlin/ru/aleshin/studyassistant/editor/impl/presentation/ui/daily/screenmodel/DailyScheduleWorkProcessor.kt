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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.screenmodel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CustomScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.FastEditDurations
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleEffect

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
internal interface DailyScheduleWorkProcessor :
    FlowWorkProcessor<DailyScheduleWorkCommand, DailyScheduleAction, DailyScheduleEffect> {

    class Base(
        private val baseScheduleInteractor: BaseScheduleInteractor,
        private val customScheduleInteractor: CustomScheduleInteractor,
        private val classInteractor: CustomClassInteractor,
        private val settingsInteractor: CalendarSettingsInteractor,
    ) : DailyScheduleWorkProcessor {

        override suspend fun work(command: DailyScheduleWorkCommand) = when (command) {
            is DailyScheduleWorkCommand.LoadSchedules -> loadSchedulesWork(
                baseScheduleId = command.baseScheduleId,
                customScheduleId = command.customScheduleId,
            )

            is DailyScheduleWorkCommand.LoadCalendarSettings -> loadCalendarSettings()
            is DailyScheduleWorkCommand.CreateCustomSchedule -> createCustomScheduleWork(
                date = command.date,
                baseSchedule = command.baseSchedule,
            )

            is DailyScheduleWorkCommand.DeleteCustomSchedule -> deleteCustomScheduleWork(
                customScheduleId = command.customScheduleId,
            )

            is DailyScheduleWorkCommand.DeleteClass -> deleteClassWork(
                targetId = command.targetId,
                schedule = command.schedule,
            )

            is DailyScheduleWorkCommand.SwapClasses -> swapClassesWork(
                from = command.from,
                to = command.to,
                schedule = command.schedule,
            )

            is DailyScheduleWorkCommand.UpdateStartOfDay -> updateStartOfDayWork(
                time = command.time,
                schedule = command.schedule,
            )

            is DailyScheduleWorkCommand.UpdateClassesDuration -> updateClassesDurationWork(
                durations = command.durations,
                schedule = command.schedule,
            )

            is DailyScheduleWorkCommand.UpdateBreaksDuration -> updateBreaksDurationWork(
                durations = command.durations,
                schedule = command.schedule,
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadSchedulesWork(baseScheduleId: UID?, customScheduleId: UID?) = flow {
            val baseScheduleFlow = if (baseScheduleId != null) {
                baseScheduleInteractor.fetchScheduleById(baseScheduleId)
            } else {
                flowOf(Either.Right<BaseSchedule?>(null))
            }
            val customScheduleFlow = if (customScheduleId != null) {
                customScheduleInteractor.fetchScheduleById(customScheduleId)
            } else {
                flowOf(Either.Right<CustomSchedule?>(null))
            }

            customScheduleFlow.flatMapLatestWithResult(
                secondFlow = baseScheduleFlow,
                onError = { DailyScheduleEffect.ShowError(it) },
                onData = { customSchedule, baseSchedule ->
                    DailyScheduleAction.UpdateSchedules(
                        baseSchedule = baseSchedule?.mapToUi(),
                        customSchedule = customSchedule?.mapToUi(),
                    )
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(DailyScheduleAction.UpdateLoading(true)))
        }

        private fun loadCalendarSettings() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
                onRightAction = { calendarSettings ->
                    val settings = calendarSettings.mapToUi()
                    emit(ActionResult(DailyScheduleAction.UpdateCalendarSettings(settings)))
                }
            )
        }

        private fun createCustomScheduleWork(date: Instant, baseSchedule: BaseScheduleUi?) = flow {
            val customScheduleId = randomUUID()
            val customSchedule = CustomScheduleUi(
                uid = customScheduleId,
                date = date.startThisDay(),
                classes = (baseSchedule?.classes ?: emptyList()).map { classModel ->
                    classModel.copy(scheduleId = customScheduleId)
                },
            )
            customScheduleInteractor.addOrUpdateSchedule(customSchedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
                onRightAction = { emitAll(loadSchedulesWork(baseSchedule?.uid, customScheduleId)) },
            )
        }

        private fun deleteCustomScheduleWork(customScheduleId: UID) = flow {
            customScheduleInteractor.deleteScheduleById(customScheduleId).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
                onRightAction = { emit(EffectResult(DailyScheduleEffect.NavigateToBack)) },
            )
        }

        private fun deleteClassWork(targetId: UID, schedule: CustomScheduleUi) = flow {
            classInteractor.deleteClassBySchedule(targetId, schedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
            )
        }

        private fun swapClassesWork(
            from: ClassUi,
            to: ClassUi,
            schedule: CustomScheduleUi,
        ) = flow<WorkResult<DailyScheduleAction, DailyScheduleEffect>> {
            val updatedClasses = schedule.classes.toMutableList().apply {
                set(indexOf(from), from.copy(timeRange = to.timeRange))
                set(indexOf(to), to.copy(timeRange = from.timeRange))
            }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleInteractor.addOrUpdateSchedule(updatedSchedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
            )
        }

        private fun updateStartOfDayWork(
            time: Instant,
            schedule: CustomScheduleUi,
        ) = flow<WorkResult<DailyScheduleAction, DailyScheduleEffect>> {
            customScheduleInteractor.updateStartOfDay(schedule.mapToDomain(), time).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
            )
        }

        private fun updateClassesDurationWork(
            durations: FastEditDurations,
            schedule: CustomScheduleUi,
        ) = flow<WorkResult<DailyScheduleAction, DailyScheduleEffect>> {
            customScheduleInteractor.updateClassesDuration(
                schedule = schedule.mapToDomain(),
                baseDuration = durations.baseDuration,
                specificDurations = durations.specificDurations.map { it.mapToDomain() },
            ).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
            )
        }
        private fun updateBreaksDurationWork(
            durations: FastEditDurations,
            schedule: CustomScheduleUi,
        ) = flow<WorkResult<DailyScheduleAction, DailyScheduleEffect>> {
            customScheduleInteractor.updateBreaksDuration(
                schedule = schedule.mapToDomain(),
                baseDuration = durations.baseDuration,
                specificDurations = durations.specificDurations.map { it.mapToDomain() },
            ).handle(
                onLeftAction = { emit(EffectResult(DailyScheduleEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class DailyScheduleWorkCommand : WorkCommand {

    data class LoadSchedules(
        val baseScheduleId: UID?,
        val customScheduleId: UID?,
    ) : DailyScheduleWorkCommand()

    data object LoadCalendarSettings : DailyScheduleWorkCommand()

    data class CreateCustomSchedule(
        val date: Instant,
        val baseSchedule: BaseScheduleUi?,
    ) : DailyScheduleWorkCommand()

    data class DeleteCustomSchedule(
        val customScheduleId: UID,
    ) : DailyScheduleWorkCommand()

    data class DeleteClass(
        val targetId: UID,
        val schedule: CustomScheduleUi,
    ) : DailyScheduleWorkCommand()

    data class SwapClasses(
        val from: ClassUi,
        val to: ClassUi,
        val schedule: CustomScheduleUi,
    ) : DailyScheduleWorkCommand()

    data class UpdateStartOfDay(
        val time: Instant,
        val schedule: CustomScheduleUi,
    ) : DailyScheduleWorkCommand()

    data class UpdateClassesDuration(
        val durations: FastEditDurations,
        val schedule: CustomScheduleUi,
    ) : DailyScheduleWorkCommand()

    data class UpdateBreaksDuration(
        val durations: FastEditDurations,
        val schedule: CustomScheduleUi,
    ) : DailyScheduleWorkCommand()
}