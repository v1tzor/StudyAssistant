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
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
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

        private fun loadHomeworksWork(timeRange: TimeRange) = channelFlow<HomeworksWorkResult> {
            var cycleUpdateJob: Job? = null
            homeworksInteractor.fetchHomeworksByTimeRange(timeRange).collect { homeworkEither ->
                cycleUpdateJob?.cancelAndJoin()
                homeworkEither.handle(
                    onLeftAction = { send(EffectResult(HomeworksEffect.ShowError(it))) },
                    onRightAction = { homeworkList ->
                        val homeworks = homeworkList.map { homework ->
                            val currentTime = dateManager.fetchCurrentInstant()
                            val status = HomeworkStatus.calculate(
                                isDone = homework.isDone,
                                completeDate = homework.completeDate,
                                deadline = homework.deadline,
                                currentTime = currentTime,
                            )
                            return@map homework.mapToUi(status)
                        }
                        val groupedHomeworks = homeworks.groupBy { homework ->
                            homework.deadline.startThisDay()
                        }
                        val homeworksMap = buildMap {
                            timeRange.periodDates().forEach { date ->
                                put(date, groupedHomeworks[date] ?: emptyList<HomeworkDetailsUi>())
                            }
                        }

                        send(ActionResult(HomeworksAction.UpdateHomeworks(homeworksMap)))

                        cycleUpdateJob = cycleUpdateHomeworkStatus(homeworksMap)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    },
                )
            }
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