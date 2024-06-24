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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import entities.tasks.convertToBase
import extensions.isCurrentDay
import extensions.setHoursAndMinutes
import extensions.shiftMinutes
import functional.Constants.Delay.UPDATE_ACTIVE_CLASS
import functional.DomainResult
import functional.collectAndHandle
import functional.handle
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import managers.DateManager
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
            is OverviewWorkCommand.LoadAnalysis -> loadAnalysisWork()
            is OverviewWorkCommand.UpdateIsHomeworkDone -> updateIsHomeworkDoneWork(command.homework, command.isDone)
        }

        private fun loadScheduleWork(date: Instant) = flow {
            scheduleInteractor.fetchDetailsScheduleByDate(date).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { scheduleDetails ->
                    val schedule = scheduleDetails.mapToUi()
                    emit(ActionResult(OverviewAction.UpdateSchedule(schedule)))
                    emitAll(cycleUpdateActiveClass(schedule, date))
                }
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateScheduleLoading(true)))
        }

        private fun loadAnalysisWork() = flow<DomainResult<OverviewAction, OverviewEffect>> {
            val week = dateManager.fetchCurrentWeek()
            analysisInteractor.fetchWeekAnalysis(week).handle(
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
            val updatedHomework = homework.copy(isDone = isDone).mapToDomain().convertToBase()
            homeworkInteractor.updateHomework(updatedHomework).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateActiveClass(schedule: ScheduleDetailsUi, classesDate: Instant) = flow {
            while (currentCoroutineContext().isActive) {
                var activeClassData: ActiveClassUi? = null
                val currentInstant = dateManager.fetchCurrentInstant()
                val scheduleClasses = schedule.mapToValue(
                    onBaseSchedule = { it?.classes },
                    onCustomSchedule = { it?.classes }
                )
                if (classesDate.isCurrentDay(currentInstant) && scheduleClasses != null) {
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
                delay(UPDATE_ACTIVE_CLASS)
            }
        }
    }
}

internal sealed class OverviewWorkCommand : WorkCommand {
    data class LoadSchedule(val date: Instant) : OverviewWorkCommand()
    data object LoadAnalysis : OverviewWorkCommand()
    data class UpdateIsHomeworkDone(val homework: HomeworkDetailsUi, val isDone: Boolean) : OverviewWorkCommand()
}