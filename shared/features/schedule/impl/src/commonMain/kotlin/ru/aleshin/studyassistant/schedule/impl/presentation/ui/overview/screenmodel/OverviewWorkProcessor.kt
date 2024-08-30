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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel

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
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.AnalysisInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface OverviewWorkProcessor :
    FlowWorkProcessor<OverviewWorkCommand, OverviewAction, OverviewEffect> {

    class Base(
        private val scheduleInteractor: ScheduleInteractor,
        private val analysisInteractor: AnalysisInteractor,
        private val homeworkInteractor: HomeworkInteractor,
        private val dateManager: DateManager,
    ) : OverviewWorkProcessor {

        override suspend fun work(command: OverviewWorkCommand) = when (command) {
            is OverviewWorkCommand.LoadSchedule -> loadScheduleWork(command.date)
            is OverviewWorkCommand.LoadAnalysis -> loadAnalysisWork(command.week)
            is OverviewWorkCommand.UpdateIsHomeworkDone -> updateIsHomeworkDoneWork(command.homework, command.isDone)
        }

        private fun loadScheduleWork(date: Instant) = channelFlow {
            var cycleUpdateJob: Job? = null
            send(ActionResult(OverviewAction.UpdateSelectedDate(date)))
            scheduleInteractor.fetchDetailsScheduleByDate(date).collect { scheduleEither ->
                cycleUpdateJob?.cancelAndJoin()
                scheduleEither.handle(
                    onLeftAction = { send(EffectResult(OverviewEffect.ShowError(it))) },
                    onRightAction = { scheduleDetails ->
                        val currentTime = dateManager.fetchCurrentInstant()
                        val schedule = scheduleDetails.mapToUi(currentTime)

                        send(ActionResult(OverviewAction.UpdateSchedule(schedule)))

                        cycleUpdateJob = cycleUpdateActiveClass(schedule, date)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    },
                )
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateScheduleLoading(true)))
        }

        private fun loadAnalysisWork(week: TimeRange) = flow<DomainResult<OverviewAction, OverviewEffect>> {
            analysisInteractor.fetchWeekAnalysis(week).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { weekAnalysis ->
                    val analysis = weekAnalysis.map { it.mapToUi() }
                    emit(ActionResult(OverviewAction.UpdateAnalysis(analysis)))
                }
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateAnalyticsLoading(true)))
        }

        private fun updateIsHomeworkDoneWork(homework: HomeworkDetailsUi, isDone: Boolean) = flow {
            val currentDate = dateManager.fetchCurrentInstant()
            val updatedHomework = homework.copy(
                isDone = isDone,
                completeDate = currentDate.takeIf { isDone },
            )
            homeworkInteractor.updateHomework(updatedHomework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateActiveClass(schedule: ScheduleDetailsUi, classesDate: Instant) = flow {
            val scheduleClasses = schedule.mapToValue(
                onBaseSchedule = { it?.classes },
                onCustomSchedule = { it?.classes }
            )
            while (currentCoroutineContext().isActive) {
                var activeClassData: ActiveClassUi? = null
                val currentInstant = dateManager.fetchCurrentInstant()

                if (classesDate.equalsDay(currentInstant) && scheduleClasses != null) {
                    val activeClass = scheduleClasses.find { classModel ->
                        val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                        return@find currentInstant <= endInstant
                    }
                    if (activeClass != null) {
                        val lastClass = scheduleClasses.findLast { classModel ->
                            val endInstant = classesDate.setHoursAndMinutes(classModel.timeRange.to)
                            val activeStartInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
                            return@findLast activeStartInstant > endInstant
                        }
                        val lastEndInstant = lastClass?.timeRange?.to?.let { classesDate.setHoursAndMinutes(it) }

                        val startInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.from)
                        val endInstant = classesDate.setHoursAndMinutes(activeClass.timeRange.to)
                        val isStarted = currentInstant > startInstant

                        activeClassData = ActiveClassUi(
                            uid = activeClass.uid,
                            isStarted = isStarted,
                            progress = dateManager.calculateProgress(
                                startTime = if (isStarted) startInstant else lastEndInstant ?: startInstant.shiftMinutes(-10),
                                endTime = if (isStarted) endInstant else startInstant,
                            ),
                            duration = dateManager.calculateLeftDateTime(
                                endDateTime = if (isStarted) endInstant else startInstant,
                            )
                        )
                    }
                }
                emit(ActionResult(OverviewAction.UpdateActiveClass(activeClassData)))
                delay(Delay.UPDATE_ACTIVE_CLASS)
            }
        }
    }
}

internal sealed class OverviewWorkCommand : WorkCommand {
    data class LoadSchedule(val date: Instant) : OverviewWorkCommand()
    data class LoadAnalysis(val week: TimeRange) : OverviewWorkCommand()
    data class UpdateIsHomeworkDone(val homework: HomeworkDetailsUi, val isDone: Boolean) : OverviewWorkCommand()
}