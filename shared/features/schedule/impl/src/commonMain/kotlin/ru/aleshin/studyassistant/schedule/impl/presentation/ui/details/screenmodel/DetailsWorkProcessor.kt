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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface DetailsWorkProcessor :
    FlowWorkProcessor<DetailsWorkCommand, DetailsAction, DetailsEffect> {

    class Base(
        private val scheduleInteractor: ScheduleInteractor,
        private val homeworkInteractor: HomeworkInteractor,
        private val dateManager: DateManager,
    ) : DetailsWorkProcessor {

        override suspend fun work(command: DetailsWorkCommand) = when (command) {
            is DetailsWorkCommand.LoadWeekSchedule -> loadWeekScheduleWork(command.week)
            is DetailsWorkCommand.UpdateIsHomeworkDone -> updateIsHomeworkDoneWork(command.homework, command.isDone)
        }

        private fun loadWeekScheduleWork(week: TimeRange) = channelFlow {
            var cycleUpdateJob: Job? = null
            scheduleInteractor.fetchDetailsWeekSchedule(week).collect { scheduleEither ->
                cycleUpdateJob?.cancelAndJoin()
                scheduleEither.handle(
                    onLeftAction = { send(EffectResult(DetailsEffect.ShowError(it))) },
                    onRightAction = { weekScheduleDetails ->
                        val currentTime = dateManager.fetchCurrentInstant()
                        val weekSchedule = weekScheduleDetails.mapToUi(currentTime)

                        send(ActionResult(DetailsAction.UpdateWeekSchedule(weekSchedule)))

                        cycleUpdateJob = cycleUpdateActiveClass(weekSchedule)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    }
                )
            }
        }.onStart {
            emit(ActionResult(DetailsAction.UpdateLoading(true)))
        }

        private fun updateIsHomeworkDoneWork(homework: HomeworkDetailsUi, isDone: Boolean) = flow {
            val currentDate = dateManager.fetchCurrentInstant()
            val updatedHomework = homework.copy(
                isDone = isDone,
                completeDate = currentDate.takeIf { isDone },
            )
            homeworkInteractor.updateHomework(updatedHomework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(DetailsEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateActiveClass(schedule: WeekScheduleDetailsUi) = flow {
            while (currentCoroutineContext().isActive) {
                delay(Constants.Delay.UPDATE_ACTIVE_CLASS)

                var activeClassData: ActiveClassUi? = null
                val currentInstant = dateManager.fetchCurrentInstant()
                val currentDateTime = currentInstant.toLocalDateTime(TimeZone.currentSystemDefault())

                val weekDaySchedule = schedule.weekDaySchedules[currentDateTime.dayOfWeek]
                val classesDate = currentDateTime.dayOfWeek.dateTimeByWeek(schedule.from)
                val scheduleClasses = weekDaySchedule?.mapToValue(
                    onBaseSchedule = { it?.classes },
                    onCustomSchedule = { it?.classes }
                )

                if (scheduleClasses != null) {
                    val activeClass = scheduleClasses.find { classModel ->
                        val startInstant = classesDate.setHoursAndMinutes(classModel.timeRange.from)
                        val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                        return@find currentInstant in startInstant..endInstant
                    }
                    if (activeClass != null) {
                        val startInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
                        val endInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.to)
                        val isStarted = currentInstant > startInstant
                        activeClassData = ActiveClassUi(
                            uid = activeClass.uid,
                            isStarted = isStarted,
                            progress = dateManager.calculateProgress(startInstant, endInstant),
                            duration = dateManager.calculateLeftDateTime(if (isStarted) endInstant else startInstant)
                        )
                    }
                }
                emit(ActionResult(DetailsAction.UpdateActiveClass(activeClassData)))
            }
        }
    }
}

internal sealed class DetailsWorkCommand : WorkCommand {
    data class LoadWeekSchedule(val week: TimeRange) : DetailsWorkCommand()
    data class UpdateIsHomeworkDone(val homework: HomeworkDetailsUi, val isDone: Boolean) : DetailsWorkCommand()
}