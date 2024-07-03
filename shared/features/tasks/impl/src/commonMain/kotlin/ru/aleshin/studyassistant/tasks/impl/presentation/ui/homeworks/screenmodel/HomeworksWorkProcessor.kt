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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import entities.tasks.HomeworkStatus
import extensions.extractAllItem
import extensions.startThisDay
import functional.Constants.Delay
import functional.TimeRange
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
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect

/**
 * @author Stanislav Aleshin on 27.06.2024.
 */
internal interface HomeworksWorkProcessor :
    FlowWorkProcessor<HomeworksWorkCommand, HomeworksAction, HomeworksEffect> {

    class Base(
        private val homeworksInteractor: HomeworksInteractor,
        private val scheduleInteractor: ScheduleInteractor,
        private val dateManager: DateManager,
    ) : HomeworksWorkProcessor {

        override suspend fun work(command: HomeworksWorkCommand) = when (command) {
            is HomeworksWorkCommand.LoadHomeworks -> loadHomeworksWork(command.timeRange)
            is HomeworksWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is HomeworksWorkCommand.UpdateHomework -> updateHomeworkWork(command.homework)
        }

        private fun loadHomeworksWork(timeRange: TimeRange) = flow<HomeworksWorkResult> {
            val currentTime = dateManager.fetchCurrentInstant()
            homeworksInteractor.fetchHomeworksByTimeRange(timeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { homeworkList ->
                    val homeworks = homeworkList.map {
                        val status = HomeworkStatus.calculate(it.isDone, it.completeDate, it.deadline, currentTime)
                        return@map it.mapToUi(status)
                    }
                    val groupedHomeworks = homeworks.groupBy { homework ->
                        homework.deadline.startThisDay()
                    }
                    val homeworksMap = buildMap {
                        timeRange.periodDates().forEach { date ->
                            put(date, groupedHomeworks[date] ?: emptyList<HomeworkDetailsUi>())
                        }
                    }
                    emit(ActionResult(HomeworksAction.UpdateHomeworks(homeworksMap)))
                    emitAll(cycleUpdateHomeworkStatus(homeworksMap))
                },
            )
        }.onStart {
            emit(ActionResult(HomeworksAction.UpdateLoading(true)))
        }

        private fun loadActiveScheduleWork(currentDate: Instant) = flow<HomeworksWorkResult> {
            scheduleInteractor.fetchScheduleByDate(currentDate).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { schedule ->
                    emit(ActionResult(HomeworksAction.UpdateActiveSchedule(schedule.mapToUi())))
                },
            )
        }

        private fun updateHomeworkWork(homework: HomeworkDetailsUi) = flow {
            homeworksInteractor.updateHomework(homework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateHomeworkStatus(homeworksMap: Map<Instant, List<HomeworkDetailsUi>>) = flow {
            val updatedHomeworksMap = homeworksMap.toMutableMap()
            while (currentCoroutineContext().isActive) {
                delay(Delay.UPDATE_TASK_STATUS)

                var isUpdated = false
                val currentInstant = dateManager.fetchCurrentInstant()
                val updatedHomeworks = updatedHomeworksMap.values.toList().extractAllItem()

                updatedHomeworks.forEach { homework ->
                    val status = homework.status
                    if (status == HomeworkStatus.WAIT || status == HomeworkStatus.IN_FUTURE) {
                        val newStatus = HomeworkStatus.calculate(
                            isDone = homework.isDone,
                            completeDate = homework.completeDate,
                            deadline = homework.deadline,
                            currentTime = currentInstant,
                        )
                        if (newStatus != status) {
                            isUpdated = true
                            val date = homework.deadline.startThisDay()
                            val newHomework = homework.copy(status = newStatus)
                            val newHomeworks = updatedHomeworksMap[date]!!.toMutableList().apply {
                                remove(homework)
                                add(newHomework)
                            }
                            updatedHomeworksMap[date] = newHomeworks
                        }
                    }
                }
                if (isUpdated) {
                    emit(ActionResult(HomeworksAction.UpdateHomeworks(updatedHomeworksMap)))
                }
            }
        }
    }
}

internal sealed class HomeworksWorkCommand : WorkCommand {
    data class LoadHomeworks(val timeRange: TimeRange) : HomeworksWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : HomeworksWorkCommand()
    data class UpdateHomework(val homework: HomeworkDetailsUi) : HomeworksWorkCommand()
}

internal typealias HomeworksWorkResult = WorkResult<HomeworksAction, HomeworksEffect>