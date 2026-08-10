/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.AnalysisInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewOutput

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface OverviewWorkProcessor :
    FlowWorkProcessor<OverviewWorkCommand, OverviewAction, OverviewEffect, OverviewOutput> {

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

        private fun loadScheduleWork(date: Instant) = flow<OverviewWorkResult> {
            emit(ActionResult(OverviewAction.UpdateSelectedDate(date)))
            scheduleInteractor.fetchDetailsScheduleByDate(date).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { overview ->
                    val action = OverviewAction.UpdateSchedule(
                        schedule = overview.schedule.mapToUi(),
                        activeClass = overview.activeClass?.mapToUi()
                    )
                    emit(ActionResult(action))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateScheduleLoading(true)))
        }

        private fun loadAnalysisWork(week: TimeRange) = flow<OverviewWorkResult> {
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
    }
}

internal sealed class OverviewWorkCommand : WorkCommand {
    data class LoadSchedule(val date: Instant) : OverviewWorkCommand()
    data class LoadAnalysis(val week: TimeRange) : OverviewWorkCommand()
    data class UpdateIsHomeworkDone(val homework: HomeworkDetailsUi, val isDone: Boolean) : OverviewWorkCommand()
}

internal typealias OverviewWorkResult = WorkResult<OverviewAction, OverviewEffect, OverviewOutput>
